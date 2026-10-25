# Receipt verification hosting and billing test runbook

Updated 25 October 2026. The verified deployment and results below supersede the original preparation notes further down this file.

## Current deployment and results

Cloudflare Workers **Free** is configured, with no paid plan purchased. Its 100,000 requests/day quota is shared by the account; Free CPU is limited to 10 ms/request. See https://developers.cloudflare.com/workers/platform/limits/. Capacity and availability remain operational release considerations.

| Environment | Base URL | Configuration |
| --- | --- | --- |
| App Tester | https://craftfolio-rvs-sandbox.lanraybanks.workers.dev | Sandbox RVS, dummy secret |
| LAT | https://craftfolio-rvs-staging.lanraybanks.workers.dev | Production RVS, test receipts allowed |
| Customer production | https://craftfolio-rvs-production.lanraybanks.workers.dev | Production RVS, test receipts rejected |

Append `/verify` for Android. Real secrets are encrypted Worker settings, configured with explicit authorization, outside Git and APKs. All health checks pass. Production `/privacy` returns the public HTML policy (HTTP 200). A successful monthly LAT receipt now validates the staging real shared secret against Amazon production RVS. Customer production transactions remain untested.

`worker.mjs` uses shared receipt policy, 8192-byte input/32768-byte upstream bounds, 10-second timeout and a 120/minute/location rate-limit binding. Logs/traces are disabled. Never log upstream URLs or bodies. Workerd rejects `redirect: 'error'`; a failing regression and real sandbox replay identified this, and `redirect: 'manual'` now rejects upstream redirects without following them.

Deploy with `npm ci --ignore-scripts`, `npm test`, then `npx wrangler deploy --env sandbox` (or staging/production), using authorized Cloudflare credentials. Never put `AMAZON_RVS_SECRET` in a command line or source file. The Node server below remains an alternative, not the selected host.

Default Android builds now use production. Use `-PverificationUrl=https://craftfolio-rvs-sandbox.lanraybanks.workers.dev/verify` for debug App Tester, or the staging URL for signed LAT. Signed artifacts are in `release-artifacts/billing-2026-10-25/`. Do not overlap Gradle builds on Windows.

Physical Fire HD 10/API 28 App Tester results: monthly purchase/restore, annual purchase, purchase-dialog cancellation, account switching, and offline force-stop/reopen PASS. Deployed production rejects a real sandbox receipt despite client override flags. All six original-file hashes and project database state are unchanged after testing. There are 22 passing Node tests, 13 JVM tests and two physical billing instrumentation checks; lint reports 0 errors/16 warnings.

Transaction cancellation/refund remains INCONCLUSIVE: App Tester displayed CANCELED and Restore was executed, but direct Amazon sandbox RVS still returned `cancelDate: null`, so Premium remained active. Later signed LAT reset plus Restore did remove Premium successfully; this is not a financial refund test. Pending purchases are N/A for subscriptions per https://developer.amazon.com/docs/in-app-purchasing/implement-pending-purchases.html.

A signed staging APK is uploaded to private LAT release `amzn1.devportal.apprelease.926d96656b134e5f80d11a68e418fa02`. Amazon shows Test in-progress. Signed LAT monthly purchase, restore, offline restart and reset-to-Free pass. Annual purchase, Restore and offline force-stop/reopen also PASS after owner account authentication. Customer refund/expiry and production billing are NOT verified. Premium was promoted out of TEST and the dashboard reports Approved; public app review/publication remain pending. The billing-identifier collection disclosure was answered Yes; the console nevertheless accepted the private test submission. The selected existing group was inspected and contains only the authorized tester. Invitations are sent when the test becomes live, not before.

## Historical preparation notes (superseded by the results above)

## Hosting

This service needs Node 22+, an HTTPS reverse proxy, outbound HTTPS to `appstore-sdk.amazon.com`, and server-only environment variables. No database, npm dependencies, persistent disk or imported craft files are needed. The process binds to loopback by default: set `HOST=0.0.0.0` when a container/PaaS routes to it. `PORT` is supplied by the host (8080 by default).

Render is one option, not a deployed dependency. After the user selects a provider and cost:

1. Push the reviewed changes to the Craftfolio repository and connect that repository in the hosting dashboard. Root directory: `verification-service`. Node runtime: 22. Build command: `npm test`. Start command: `node server.mjs`. Disable automatic deploys initially so the sandbox and release configuration cannot change unexpectedly.
2. Set `HOST=0.0.0.0`. Use the provider-supplied `PORT`. Health check: `/health`. The Android endpoint is the actual assigned HTTPS service URL plus `/verify`; do not use a made-up domain. Render terminates TLS before forwarding HTTP internally.
3. Create a dedicated **App Tester sandbox** service with `RVS_SANDBOX=true`, `ALLOW_AMAZON_TEST_RECEIPTS=false`. No Amazon credential is needed; Amazon accepts a non-empty dummy secret in Cloud Sandbox, supplied automatically by this server.
4. For **LAT**, use a separate staging service with `RVS_SANDBOX=false`, `ALLOW_AMAZON_TEST_RECEIPTS=true`. The user must enter `AMAZON_RVS_SECRET` directly into the provider's secret environment setting. Retrieve it from https://developer.amazon.com/sdk/shared-key.html. Do not paste it into chat, a build command, Git, an APK, or a screenshot. Do not generate/rotate it unnecessarily.
5. For **customer production**, use a separate service with both flags `false` and the real secret in its secret store. Check these settings immediately before publishing. Never turn the sandbox or LAT service into the customer's endpoint.
6. Configure monitoring of `/health`, error counts, traffic and provider spend. Do not capture HTTP bodies or outbound RVS URLs: URLs include the shared secret. The service does not log receipts or exceptions. `/health` only confirms that the configured process is serving; it does not prove Amazon credentials or billing work.

The server caps request bodies at 8192 bytes, outgoing RVS calls at 10 seconds, incoming headers/body timing, verification concurrency at 32, and traffic at 120 verification requests/minute per process. This global in-memory limit is basic abuse protection, not a distributed per-customer quota. Set additional per-client limits at the edge if available; do not trust arbitrary forwarded-IP headers. Keep credentials out of provider request tracing. The Dockerfile offers a non-root alternative; it has not been built here because Docker is unavailable.

Render docs: https://render.com/docs/deploy-node-express-app and https://render.com/docs/health-checks. Pricing currently shows a $7/month always-on entry instance, plus applicable usage/taxes: https://render.com/pricing. Confirm the actual order in the dashboard. A sleeping free instance is unsuitable for reliable production purchases with the app's 15-second connection/read timeouts.

## Build and smoke checks

Run `npm test` in `verification-service`. These are local tests with simulated RVS responses, not Amazon purchase evidence.

After deployment, GET the actual HTTPS `/health` URL: expect 200 and `{"status":"ok"}`. POST invalid JSON to `/verify`: expect 400, no entitlement. Test a real App Tester receipt through the sandbox before claiming the integration works. A synthetic or invalid receipt must never produce Premium in production.

Build Android with `.\gradlew.bat assembleDebug -PverificationUrl=https://ACTUAL-SANDBOX-HOST/verify`. The public PEM is bundled automatically. Never embed the Amazon shared secret. The committed build configuration has no real verifier URL until hosting exists.

## App Tester on a dedicated Fire tablet

The attached KFTRWI tablet has `com.amazon.sdktestclient` installed. Device use is pending the user's reply. Preserve Craftfolio's imported files and project database: use `adb install -r`; never uninstall or clear Craftfolio data to reset an entitlement. Snapshot its current test configuration before changing the shared device's App Tester catalogue or sandbox mode, and restore it after testing.

`testing/amazon.sdktester.json` is a manually authored US-price fixture matching the saved console SKUs. It is not a console export or proof of product approval. Back up any existing `/sdcard/amazon.sdktester.json` before replacing it. Push the file to that location, set the SDK sandbox mode following Amazon's guide, and verify both items in App Tester's IAP-items screen. Only grant App Tester's overlay permission if needed for this dedicated test and record/restore its original setting.

Use https://developer.amazon.com/docs/in-app-purchasing/iap-install-and-configure-app-tester.html and https://developer.amazon.com/docs/in-app-purchasing/iap-app-tester-user-guide.html. Both purchasable term entries point to `subscriptionParent: craftfolio_premium`; RVS sandbox may report `termSku: craftfolio_premium_term`. The parent is the entitlement identity; termSku is not the receipt product allowlist.

For each case, record the app APK SHA-256, endpoint environment, tablet model/OS, time, observed result and redacted evidence. Never record the real shared secret or customer receipt/account identifiers in deliverable logs.

| Case | Expected result | Current evidence |
| --- | --- | --- |
| Monthly successful purchase | RVS confirms parent; Premium unlocks | Not run |
| Annual successful purchase | Same parent entitlement, annual term purchased | Not run |
| User cancellation / failed purchase | No new Premium entitlement | Not run |
| Pending / deferred purchase | No unlock until a later successful, verified receipt | Not run |
| Restore after closing app | Amazon parent receipt verified again | Not run |
| Unrelated/forged receipt | No entitlement | Local HTTP/policy tests pass; Amazon device case not run |
| Expiry/refund | No active entitlement after verified loss-of-access date | Local policy tests pass; Amazon device case not run |
| Offline within/after lease | Existing verified lease works up to 72 hours; expiry does not hide personal files | Local policy tests pass; billing device case not run |
| Account change / historical receipts | Previous account cannot retain Premium; old receipts do not overwrite a newer active receipt | Not run |
| Production test-receipt rejection | Sandbox/LAT receipt cannot grant customer Premium | Local HTTP/policy tests pass; deployed check not run |

## DevTest / LAT and release

App Tester is a simulation. Separately test the exact signed APK through Amazon DevTest/LAT with the console's real products, configured staging RVS, and an explicitly selected tester account. Do not invite people or send notifications without user authorization. Do not initiate a chargeable transaction without the user's approval of the merchant, plan and spending limit. Promoting products, submitting a release and signing are still separate release steps.

Amazon references: https://developer.amazon.com/docs/in-app-purchasing/iap-faqs.html, https://developer.amazon.com/docs/in-app-purchasing/rvs-cloud-sandbox.html and https://developer.amazon.com/docs/in-app-purchasing/iap-testing-overview.html.
