// Reads an explicit App Tester replay from stdin; never prints user or receipt identifiers.
import {entitlement} from '../verification-service/entitlement.mjs';
let input='';
for await (const chunk of process.stdin) input += chunk;
const {userId,receiptId}=JSON.parse(input);
if(!userId || !receiptId) throw Error('A sandbox replay is required');
try {
  const result=await fetch('https://craftfolio-rvs-sandbox.lanraybanks.workers.dev/verify',{
    method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({userId,receiptId})
  });
  const lease=await result.json();
  const granted=result.status===200 && lease.validUntil>Date.now();
  console.log(JSON.stringify({probe:'hosted-worker',status:result.status,premiumGranted:granted}));
  if(!granted) process.exitCode=1;
  const production=await fetch('https://craftfolio-rvs-production.lanraybanks.workers.dev/verify',{
    method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({userId,receiptId,sandbox:true,allowTestReceipts:true})
  });
  const denied=await production.json();
  const productionDenied=production.status!==200 || denied.validUntil===0;
  console.log(JSON.stringify({probe:'production-rejects-sandbox',status:production.status,denied:productionDenied}));
  if(!productionDenied) process.exitCode=1;
  const upstream=await fetch(`https://appstore-sdk.amazon.com/sandbox/version/1.0/verifyReceiptId/developer/craftfolio-app-tester-only/user/${encodeURIComponent(userId)}/receiptId/${encodeURIComponent(receiptId)}`,{redirect:'error'});
  let receipt; try { receipt=await upstream.json(); } catch { receipt={}; }
  let verdict; try {verdict=entitlement(receipt,receiptId,Date.now(),true)>Date.now()?'active':'inactive';}catch{verdict='rejected';}
  console.log(JSON.stringify({probe:'direct-amazon-sandbox',status:upstream.status,productId:receipt.productId,productType:receipt.productType,testTransaction:receipt.testTransaction,purchaseDate:receipt.purchaseDate,cancelDate:receipt.cancelDate,now:Date.now(),policy:verdict}));
} catch { console.log(JSON.stringify({probe:'network',failure:'Request failed; details withheld to protect identifiers'})); process.exitCode=1; }
