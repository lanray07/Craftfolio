import {test} from 'node:test';
import assert from 'node:assert/strict';
import {once} from 'node:events';
import {createVerificationServer} from './http.mjs';

const parentReceipt = {receiptId:'receipt',productId:'craftfolio_premium',productType:'SUBSCRIPTION',purchaseDate:500,cancelDate:null};
async function fixture(t, options = {}) {
  const calls = [];
  const server = createVerificationServer({secret:'test-secret',now:()=>1000,
    fetchImpl:async (...args)=> {calls.push(args); return Response.json(parentReceipt);},...options});
  server.listen(0,'127.0.0.1'); await once(server,'listening');
  t.after(()=> new Promise(resolve=>{server.close(resolve); server.closeAllConnections();}));
  const base = `http://127.0.0.1:${server.address().port}`;
  return {calls, get:path=>fetch(base+path), post:(body,headers={'Content-Type':'application/json'})=>fetch(base+'/verify',{method:'POST',headers,body})};
}
const request = JSON.stringify({userId:'user',receiptId:'receipt'});
test('HTTP verifies the parent SKU through production RVS, never client product claims',async t=>{
  const api=await fixture(t);
  const response=await api.post(JSON.stringify({userId:'user',receiptId:'receipt',productId:'forged',sandbox:true}));
  assert.equal(response.status,200);
  assert.equal(response.headers.get('cache-control'),'no-store');
  assert.equal((await response.json()).validUntil,1000+72*3600000);
  assert.match(api.calls[0][0],/^https:\/\/appstore-sdk.amazon.com\/version\/1.0\//);
  assert.equal(api.calls[0][1].redirect,'error');
});
test('HTTP denies an unrelated verified product even when it names a Premium term',async t=>{
  const api=await fixture(t,{fetchImpl:async()=>Response.json({...parentReceipt,productId:'other',termSku:'craftfolio_premium_annual'})});
  assert.equal((await api.post(request)).status,503);
});
test('HTTP rejects malformed, missing and oversized input before calling RVS',async t=>{
  const api=await fixture(t);
  for(const body of ['{','null','{}',JSON.stringify({userId:' ',receiptId:'receipt'})]) assert.equal((await api.post(body)).status,400);
  assert.equal((await api.post('x'.repeat(8193))).status,413);
  assert.equal((await api.post(request,{'Content-Type':'text/plain'})).status,415);
  assert.equal(api.calls.length,0);
});
test('HTTP invalid receipt statuses revoke access; Amazon outages never grant it',async t=>{
  for(const status of [400,410,497,429,500]) {
    const api=await fixture(t,{fetchImpl:async()=>new Response('{}',{status})});
    const response=await api.post(request);
    if([400,410,497].includes(status)) {assert.equal(response.status,200); assert.equal((await response.json()).validUntil,0);}
    else {assert.equal(response.status,503); assert.deepEqual(await response.json(),{});}
  }
});
test('HTTP sandbox uses the sandbox URL and accepts its generated term; production rejects it',async t=>{
  const sandboxReceipt={...parentReceipt,termSku:'craftfolio_premium_term',testTransaction:true};
  let requested;
  const fetchImpl=async url=>{requested=url;return Response.json(sandboxReceipt);};
  const sandbox=await fixture(t,{sandbox:true,fetchImpl});
  assert.equal((await sandbox.post(request)).status,200);
  assert.match(requested,/\/sandbox\/version\/1.0\//);
  const production=await fixture(t,{fetchImpl});
  assert.equal((await production.post(request)).status,503);
  const lat=await fixture(t,{allowTestReceipts:true,fetchImpl});
  assert.equal((await lat.post(request)).status,200);
  assert.doesNotMatch(requested,/\/sandbox\//);
});
test('HTTP upstream exceptions are sanitized; health contains no account data',async t=>{
  const api=await fixture(t,{fetchImpl:async()=>{throw Error('sensitive upstream URL');}});
  const response=await api.post(request);
  assert.equal(response.status,503); assert.equal(await response.text(),'{}');
  assert.deepEqual(await (await api.get('/health')).json(),{status:'ok'});
  assert.equal((await api.get('/verify')).status,404);
});
test('HTTP bounds verification traffic without rate-limiting host health checks',async t=>{
  const api=await fixture(t,{maxRequestsPerMinute:1});
  assert.equal((await api.post(request)).status,200);
  const limited=await api.post(request);
  assert.equal(limited.status,429); assert.equal(limited.headers.get('retry-after'),'60');
  assert.equal(api.calls.length,1);
  assert.equal((await api.get('/health')).status,200);
});
