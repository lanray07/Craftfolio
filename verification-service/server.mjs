import { createVerificationServer } from './http.mjs';
for (const name of ['RVS_SANDBOX', 'ALLOW_AMAZON_TEST_RECEIPTS']) {
  if (process.env[name] != null && !['true','false'].includes(process.env[name])) throw new Error(`${name} must be true or false`);
}
const sandbox = process.env.RVS_SANDBOX === 'true';
// Amazon accepts any non-empty shared-secret string in Cloud Sandbox. Never use the real secret there.
const secret = sandbox ? 'craftfolio-app-tester-only' : process.env.AMAZON_RVS_SECRET;
const allowTestReceipts = process.env.ALLOW_AMAZON_TEST_RECEIPTS === 'true';
// TLS and per-client rate limits belong at the hosting reverse proxy. HOST stays loopback by default.
createVerificationServer({secret, sandbox, allowTestReceipts})
  .listen(Number(process.env.PORT ?? 8080), process.env.HOST ?? '127.0.0.1');
