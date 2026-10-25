import { entitlement } from './entitlement.mjs';
import { privacyHtml } from './privacy.mjs';

const headers = {'Content-Type':'application/json', 'Cache-Control':'no-store'};
const reply = (status, body = {}, extra = {}) => new Response(JSON.stringify(body), {status, headers:{...headers,...extra}});

async function boundedJson(stream, limit) {
  if (!stream) throw new SyntaxError('Missing body');
  const reader = stream.getReader();
  const parts = []; let size = 0;
  try {
    while (true) {
      const {value, done} = await reader.read();
      if (done) break;
      size += value.byteLength;
      if (size > limit) { await reader.cancel(); throw new RangeError('Body too large'); }
      parts.push(value);
    }
  } finally { reader.releaseLock(); }
  const bytes = new Uint8Array(size); let offset = 0;
  for (const part of parts) { bytes.set(part,offset); offset += part.byteLength; }
  return JSON.parse(new TextDecoder().decode(bytes));
}

/** Edge adapter shares the receipt policy with the Node server; no customer data is logged. */
export function createWorker({fetchImpl = fetch, now = Date.now} = {}) {
  return {
    async fetch(request, env) {
      try {
        const path = new URL(request.url).pathname;
        if (request.method === 'GET' && path === '/privacy') return new Response(privacyHtml,{headers:{
          'Content-Type':'text/html; charset=utf-8','Cache-Control':'public, max-age=3600',
          'Content-Security-Policy':"default-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; frame-ancestors 'none'; form-action 'none'",
          'X-Content-Type-Options':'nosniff'
        }});
        if (!['true','false'].includes(env.RVS_SANDBOX) || !['true','false'].includes(env.ALLOW_AMAZON_TEST_RECEIPTS)) return reply(503);
        const sandbox = env.RVS_SANDBOX === 'true';
        const secret = sandbox ? 'craftfolio-app-tester-only' : env.AMAZON_RVS_SECRET;
        if (!secret || !env.VERIFY_LIMIT?.limit) return reply(503);
        if (request.method === 'GET' && path === '/health') return reply(200,{status:'ok'});
        if (request.method !== 'POST' || path !== '/verify') return reply(404);
        // Shared per-location limiter: does not store IPs, account IDs or receipts as keys.
        if (!(await env.VERIFY_LIMIT.limit({key:'verify'})).success) return reply(429,{}, {'Retry-After':'60'});
        if (request.headers.get('content-type')?.split(';')[0].trim().toLowerCase() !== 'application/json') return reply(415);
        let input;
        try { input = await boundedJson(request.body,8192); }
        catch (error) { return reply(error instanceof RangeError ? 413 : 400); }
        const {userId,receiptId} = input ?? {};
        if (![userId,receiptId].every(value=>typeof value==='string' && value.trim().length>0 && value.length<=2048)) return reply(400);
        const origin = sandbox ? 'https://appstore-sdk.amazon.com/sandbox' : 'https://appstore-sdk.amazon.com';
        // Workerd does not implement redirect:error. Manual mode rejects 3xx below without following it.
        const upstream = await fetchImpl(`${origin}/version/1.0/verifyReceiptId/developer/${encodeURIComponent(secret)}/user/${encodeURIComponent(userId)}/receiptId/${encodeURIComponent(receiptId)}`, {signal:AbortSignal.timeout(10000),redirect:'manual'});
        if ([400,410,497].includes(upstream.status)) return reply(200,{userId,receiptId,validUntil:0});
        if (!upstream.ok) return reply(503);
        const receipt = await boundedJson(upstream.body,32768);
        return reply(200,{userId,receiptId,validUntil:entitlement(receipt,receiptId,now(),sandbox || env.ALLOW_AMAZON_TEST_RECEIPTS==='true')});
      } catch {
        // In particular, never surface an upstream URL containing the Amazon shared secret.
        return reply(503);
      }
    }
  };
}

export default createWorker();
