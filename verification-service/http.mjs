import { createServer } from 'node:http';
import { entitlement } from './entitlement.mjs';

/** Dependency injection keeps HTTP/RVS failure tests independent of Amazon and credentials. */
export function createVerificationServer({secret, sandbox = false, allowTestReceipts = false,
  fetchImpl = fetch, now = Date.now, maxRequestsPerMinute = 120}) {
  if (!secret) throw new Error('Set AMAZON_RVS_SECRET on the server only');
  const base = sandbox ? 'https://appstore-sdk.amazon.com/sandbox' : 'https://appstore-sdk.amazon.com';
  let active = 0;
  let windowStart = now();
  let requests = 0;
  const server = createServer(async (req, res) => {
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Cache-Control', 'no-store');
    const reply = (status, body = {}) => res.writeHead(status).end(JSON.stringify(body));
    if (req.method === 'GET' && req.url === '/health') { reply(200, {status:'ok'}); return; }
    if (req.method !== 'POST' || req.url !== '/verify') { reply(404); return; }
    const timestamp = now();
    if (timestamp - windowStart >= 60000) { windowStart = timestamp; requests = 0; }
    if (++requests > maxRequestsPerMinute || active >= 32) { res.setHeader('Retry-After','60'); reply(429); return; }
    if (req.headers['content-type']?.split(';')[0].trim().toLowerCase() !== 'application/json') { reply(415); return; }
    active++;
    try {
      let bytes = 0;
      const chunks = [];
      for await (const chunk of req) {
        bytes += chunk.length;
        if (bytes > 8192) { reply(413); return; }
        chunks.push(chunk);
      }
      let input;
      try { input = JSON.parse(Buffer.concat(chunks).toString('utf8')); } catch { reply(400); return; }
      const {userId, receiptId} = input ?? {};
      if (![userId, receiptId].every(v => typeof v === 'string' && v.trim().length > 0 && v.length <= 2048)) { reply(400); return; }
      const url = `${base}/version/1.0/verifyReceiptId/developer/${encodeURIComponent(secret)}/user/${encodeURIComponent(userId)}/receiptId/${encodeURIComponent(receiptId)}`;
      const upstream = await fetchImpl(url, {signal: AbortSignal.timeout(10000), redirect: 'error'});
      if ([400, 410, 497].includes(upstream.status)) { reply(200, {userId, receiptId, validUntil: 0}); return; }
      if (!upstream.ok) { reply(503); return; }
      const receipt = await upstream.json();
      reply(200, {userId, receiptId, validUntil: entitlement(receipt, receiptId, now(), sandbox || allowTestReceipts)});
    } catch {
      // Never log exceptions: upstream request URLs include the developer shared secret.
      reply(503);
    } finally { active--; }
  });
  server.requestTimeout = 15000;
  server.headersTimeout = 10000;
  return server;
}
