# Amazon setup and verification

Reviewed 24 October 2026. Official sources:

- https://developer.amazon.com/docs/appstore-sdk/integrate-appstore-sdk.html (pinned SDK 3.0.9)
- https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html
- https://developer.amazon.com/docs/in-app-purchasing/iap-rvs-for-android-apps.html
- https://developer.amazon.com/docs/device-specs/ft-identify-tablet-devices.html

## Developer configuration still required

1. Create the app `com.craftfolio.patterns` in your Amazon developer account. Confirm package identity before submission.
2. Download its public `AppstoreAuthenticationKey.pem` into `app/src/main/assets/`. This is a public app key, not the RVS shared secret.
3. Configure and publish monthly/annual subscription products matching `craftfolio_premium_monthly` and `craftfolio_premium_annual`. Confirm term SKU vs parent SKU mapping in App Tester and adjust both client catalogue and verifier allowlist together. Pricing, tax, renewal disclosures and trial policy remain commercial decisions. No trial is claimed.
4. Deploy `verification-service/server.mjs` under HTTPS at `/verify`, behind a reverse proxy with body limits and rate limiting. Node 22+, no npm dependencies. Set `AMAZON_RVS_SECRET` only in the host secret store. `PORT` defaults to 8080, loopback binding. Never log upstream URLs (they contain the secret), account IDs or receipts. Configure operational monitoring without these values.
5. Set `-PverificationUrl=https://your-host/verify` when building. APK includes only the public endpoint. The service calls Amazon RVS, verifies product/receipt/type/purchase date and rejects sandbox receipts in production. `RVS_SANDBOX=true` is for a separate test deployment only.
6. Test App Tester with a separate `RVS_SANDBOX=true` deployment. For Live App Testing receipts, use a separate staging deployment with production RVS (`RVS_SANDBOX=false`) and `ALLOW_AMAZON_TEST_RECEIPTS=true`; LAT receipts are marked as beta/test by Amazon. Keep both flags false on the customer production verifier. Never enable a test-receipt override from a client request. Test purchase, decline, pending, restored purchase, expired subscription, refund, account switch, offline cache, reconnect and multiple historical receipts. Pending purchases never unlock until a successful verified receipt arrives.
7. Configure signing, support email, hosted privacy policy, marketplace availability, age rating and distribution exclusions. Validate current store asset requirements in the console.

RVS returns `cancelDate` as loss of access, including when a cancelled auto-renew term runs out. A past `renewalDate` alone is not treated as loss of access. The server returns at most a 72-hour entitlement lease (or earlier cancel date), and the client rejects clock rollback relative to last verification. This is bounded offline caching, not tamper-proof DRM. Imported personal files never require verification. Local purchase callbacks alone never grant Premium.

Known commerce gate: service deployment, account/product mapping, Amazon credentials and supported Amazon purchase tests are not available from source code alone. Subscription support must remain unverified until actual evidence is recorded. No live transaction has been performed by this build task.
