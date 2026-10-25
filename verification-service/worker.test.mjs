import {test} from 'node:test';
import assert from 'node:assert/strict';
import {createWorker} from './worker.mjs';

const goodReceipt = {receiptId:'receipt',productId:'craftfolio_premium',productType:'SUBSCRIPTION',purchaseDate:500};
const production = {RVS_SANDBOX:'false',ALLOW_AMAZON_TEST_RECEIPTS:'false',AMAZON_RVS_SECRET:'unit-test-only',VERIFY_LIMIT:{limit:async()=>({success:true})}};
const request = body => new Request('https://example.invalid/verify',{method:'POST',headers:{'Content-Type':'application/json'},body:typeof body==='string'?body:JSON.stringify(body)});
const ids = {userId:'user',receiptId:'receipt'};

test('Public privacy policy works independently of billing credentials and never calls Amazon',async()=>{
  const worker=createWorker({fetchImpl:()=>assert.fail('Policy must not call upstream')});
  const result=await worker.fetch(new Request('https://example.invalid/privacy'),{});
  assert.equal(result.status,200);
  assert.match(result.headers.get('content-security-policy'),/default-src 'none'/);
  const text=await result.text();
  assert.match(text,/banksmi@mail.com/); assert.match(text,/Cloudflare Workers/); assert.doesNotMatch(text,/<script/i);
});

test('Worker verifies only Amazon parent receipts and preserves bounded lease',async()=>{
  let url;
  const worker=createWorker({now:()=>1000,fetchImpl:async value=>{url=value;return Response.json(goodReceipt);}});
  const result=await worker.fetch(request({...ids,sandbox:true,productId:'forged'}),production);
  assert.equal(result.status,200); assert.equal((await result.json()).validUntil,1000+72*3600000);
  assert.doesNotMatch(url,/\/sandbox\//); assert.equal(result.headers.get('cache-control'),'no-store');
});
test('Worker uses edge-supported manual redirects and refuses a redirect response',async()=>{
  let calls=0;
  const edgeFetch=async (url,options)=>{
    // Workerd accepts manual/follow, unlike Node which also accepts error.
    if(options.redirect==='error') throw new TypeError('Unsupported redirect mode');
    assert.equal(options.redirect,'manual'); calls++;
    return Response.json(goodReceipt);
  };
  assert.equal((await createWorker({now:()=>1000,fetchImpl:edgeFetch}).fetch(request(ids),production)).status,200);
  assert.equal(calls,1);
  const redirected=createWorker({fetchImpl:async (url,options)=>{
    assert.equal(options.redirect,'manual');
    return new Response('',{status:302,headers:{Location:'https://example.invalid/never-follow'}});
  }});
  assert.equal((await redirected.fetch(request(ids),production)).status,503);
});
test('Worker fails closed for missing secrets, limiter or ambiguous environment flags',async()=>{
  const worker=createWorker({fetchImpl:()=>assert.fail('Upstream must not be called')});
  for(const env of [{...production,AMAZON_RVS_SECRET:''},{...production,VERIFY_LIMIT:undefined},{...production,RVS_SANDBOX:'TRUE'},{...production,ALLOW_AMAZON_TEST_RECEIPTS:undefined}]) assert.equal((await worker.fetch(request(ids),env)).status,503);
});
test('Worker bounds input and upstream traffic without requiring health clients to verify',async()=>{
  const worker=createWorker({fetchImpl:()=>assert.fail('Upstream must not be called')});
  for(const body of ['{','null','{}',JSON.stringify({...ids,userId:' '})]) assert.equal((await worker.fetch(request(body),production)).status,400);
  assert.equal((await worker.fetch(request('x'.repeat(8193)),production)).status,413);
  assert.equal((await worker.fetch(new Request('https://example.invalid/verify',{method:'POST',body:'{}'}),production)).status,415);
  const blocked={...production,VERIFY_LIMIT:{limit:async()=>({success:false})}};
  assert.equal((await worker.fetch(request(ids),blocked)).status,429);
  assert.equal((await worker.fetch(new Request('https://example.invalid/health'),blocked)).status,200);
});
test('Worker separates sandbox, LAT and production receipt acceptance',async()=>{
  let url;
  const worker=createWorker({now:()=>1000,fetchImpl:async value=>{url=value;return Response.json({...goodReceipt,testTransaction:true});}});
  assert.equal((await worker.fetch(request(ids),production)).status,503);
  assert.equal((await worker.fetch(request(ids),{...production,ALLOW_AMAZON_TEST_RECEIPTS:'true'})).status,200);
  assert.doesNotMatch(url,/\/sandbox\//);
  assert.equal((await worker.fetch(request(ids),{...production,RVS_SANDBOX:'true',AMAZON_RVS_SECRET:undefined})).status,200);
  assert.match(url,/\/sandbox\/version\/1.0\/verifyReceiptId\/developer\/craftfolio-app-tester-only\//);
});
test('Worker revokes invalid receipts and sanitises all upstream failure paths',async()=>{
  for(const status of [400,410,497,429,500]) {
    const worker=createWorker({fetchImpl:async()=>new Response('{}',{status})});
    const result=await worker.fetch(request(ids),production);
    if([400,410,497].includes(status)) assert.equal((await result.json()).validUntil,0);
    else { assert.equal(result.status,503); assert.deepEqual(await result.json(),{}); }
  }
  for(const fetchImpl of [async()=>{throw Error('secret URL');},async()=>Response.json({...goodReceipt,productId:'other'}),async()=>new Response('x'.repeat(32769))]) {
    const result=await createWorker({fetchImpl}).fetch(request(ids),production);
    assert.equal(result.status,503); assert.deepEqual(await result.json(),{});
  }
});
