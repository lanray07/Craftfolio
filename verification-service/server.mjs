import { createServer } from 'node:http';
import { entitlement } from './entitlement.mjs';
const secret = process.env.AMAZON_RVS_SECRET;
if (!secret) throw new Error('Set AMAZON_RVS_SECRET on the server only');
const sandbox = process.env.RVS_SANDBOX === 'true';
const allowTestReceipts = sandbox || process.env.ALLOW_AMAZON_TEST_RECEIPTS === 'true';
// Deploy behind an HTTPS reverse proxy with request rate limits. Never log the RVS URL or receipt IDs.
createServer(async (req, res) => {
  res.setHeader('Content-Type', 'application/json'); res.setHeader('Cache-Control', 'no-store');
  if (req.method !== 'POST' || req.url !== '/verify') { res.writeHead(404).end('{}'); return; }
  try {
    let body = ''; for await (const chunk of req) { body += chunk; if (body.length > 8192) throw new Error('Too large'); }
    const {userId, receiptId} = JSON.parse(body);
    if (![userId, receiptId].every(v => typeof v === 'string' && v.length > 0 && v.length <= 2048)) throw new Error('Invalid input');
    const base = sandbox ? 'https://appstore-sdk.amazon.com/sandbox' : 'https://appstore-sdk.amazon.com';
    const url = `${base}/version/1.0/verifyReceiptId/developer/${encodeURIComponent(secret)}/user/${encodeURIComponent(userId)}/receiptId/${encodeURIComponent(receiptId)}`;
    const upstream = await fetch(url, {signal: AbortSignal.timeout(10000), redirect: 'error'});
    if ([400, 410, 497].includes(upstream.status)) { res.end(JSON.stringify({userId, receiptId, validUntil: 0})); return; }
    if (!upstream.ok) { res.writeHead(503).end('{}'); return; }
    const receipt = await upstream.json();
    res.end(JSON.stringify({userId, receiptId, validUntil: entitlement(receipt, receiptId, Date.now(), allowTestReceipts)}));
  } catch { res.writeHead(503).end('{}'); }
}).listen(Number(process.env.PORT ?? 8080), '127.0.0.1');
