# Amazon setup and verification

## Current billing status — 25 October 2026

Follow-up: private LAT is **Test in-progress**. The Amazon-installed non-debuggable release build passes monthly purchase, restore, offline force-stop/reopen, and LAT subscription reset followed by Restore to Free. Staging receipt verification used the real shared secret and Amazon production RVS. The owner completed Amazon account authentication; annual purchase, receipt verification, Restore and offline force-stop/reopen now PASS. LAT account switching and real customer refund/expiry remain incomplete. The old debug app was removed only after verified private backup and explicit approval; its six patterns, two projects, counters and two notes remain backed up. The release installation starts empty.

This update supersedes historical unconfigured-hosting and untested-App-Tester statements below. Free Cloudflare Workers sandbox, staging and production services are deployed; encrypted real secrets are configured in staging/production. See [hosting/results](../verification-service/HOSTING.md). The default app endpoint is production; sandbox/staging are explicit build overrides. Public privacy policy: https://craftfolio-rvs-production.lanraybanks.workers.dev/privacy (HTTP 200 verified).

Catalogue lookup includes parent plus two terms; only terms are purchasable, while RVS validates the parent. Simulated monthly/annual purchases, restore, account switching, cancelled purchase and offline entitlement pass on the Fire tablet. Real sandbox receipts are denied by production. Cancellation-to-revocation remains inconclusive because Amazon sandbox returned no cancelDate after App Tester cancellation. Monthly and annual LAT cases pass; remaining production acceptance is still required.

A release-signed staging APK was uploaded to private test **Craftfolio 1.0 billing verification**, draft release `amzn1.devportal.apprelease.926d96656b134e5f80d11a68e418fa02`. Amazon accepted the private test submission and now shows **Test in-progress**. The production app was subsequently submitted to Amazon on 25 October 2026; review/publication are pending. Content disclosures report billing identifier collection truthfully. The personal-information question was answered Yes. Amazon accepted the submission after the owner explicitly confirmed export certification. Only the authorized tester was selected through a verified one-person existing group. Invitations go out when the test is live; Amazon notes first publication may take up to 24 hours. Premium was subsequently promoted out of TEST with owner approval; the dashboard now reports Approved.

Signed LAT and production APKs are in `release-artifacts/billing-2026-10-25/`; see [signing and backup](SIGNING.md). Amazon installation and monthly signed-build billing checks pass; remaining QA is incomplete. Original debug data is in the verified private backup.

Reviewed 24 October 2026. Official sources:

- https://developer.amazon.com/docs/appstore-sdk/integrate-appstore-sdk.html (pinned SDK 3.0.9)
- https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html
- https://developer.amazon.com/docs/in-app-purchasing/iap-rvs-for-android-apps.html
- https://developer.amazon.com/docs/device-specs/ft-identify-tablet-devices.html

## Completed account setup

Latest localisation update, 25 October 2026: the user requested all Amazon languages for the app and listings. The app now supports 16 locales / 11 languages; all 15 main-description, 11 image and 12 Premium locales offered in this console are configured and reload-verified. Image galleries contain four native screenshots and both brand icons each, with 44 distinct screenshot assets. Chinese is offered for Premium but not main descriptions/images; Dutch, Polish, European Portuguese and Swedish have main-description slots but no Premium/image slots. See LOCALIZATION.md and `store-assets/locales.json` for the exact matrix and limitations. The fully localised Premium revision was submitted with **Submit to Test**, returning **TEST / Approved**, Oct 25, 2026. No production submission or billing purchase was performed. Latest binaries are under `release-artifacts/all-amazon-locales-2026-10-25/`; their verification URL remains empty.

On 24 October 2026, created the draft **Craftfolio: Patterns & Projects** in the signed-in Amazon Developer Console:

- App ID: `amzn1.devportal.mobileapp.4c9637fe3bfe4621b51d702fc7ebaa59`
- Initial release ID: `amzn1.devportal.apprelease.e681623ce1824ef99066bf2bd1d37339`
- Internal App SKU: `com.craftfolio.patterns` (the Android package still requires confirmation when the APK is uploaded).
- Category: Productivity; English language support selected. Optional subcategory left unset because Amazon's duplicate Organizers & Assistants entry remapped the category to Business after saving. Verified Productivity and English after reloading.
- Public customer support email: `banksmi@mail.com`.
- Downloaded Amazon's public authentication PEM into `app/src/main/assets/AppstoreAuthenticationKey.pem`. Parsed successfully as an RSA 2048-bit public key; downloaded and installed file SHA-256: `fa60aa14b323b38230d01faa1133290f72b5d9547d86d56e570e7bee6108c454`.

The entry remains a draft; no APK was uploaded or submitted during this setup. The original root release artifacts predate the key; the newer localised builds include it. This setup does not establish working billing or verify a purchase.

## Premium subscription test catalogue (24 October 2026)

Created one subscription, **Craftfolio Premium**, with parent SKU `craftfolio_premium` and two auto-renewing terms:

- Monthly: `craftfolio_premium_monthly`, term ID `amzn1.dex.subs.term.9182906f5022493cae055ce111c8c82b`.
- Annual: `craftfolio_premium_annual`, term ID `amzn1.dex.subs.term.d2aaafac3d83426b9fcf39f52c490cbb`.
- Subscription ID: `amzn1.dex.title.4c72317a97fe4c668beb38fc5b4d3f02`; tier ID: `amzn1.dex.subs.tier.ac5ac8467f814a30b25151f7ea6c5577`.
- Paid subscription: Yes. Free trial: No. Promotional introductory pricing: No, on both terms, verified after reload.
- UK/US English titles and descriptions describe only implemented Premium features. The user's requested colourful yarn-and-gold-crochet-hook icons (`store-assets/brand/craftfolio-premium-512.png` and `craftfolio-premium-114.png`) use coral, apricot and teal on a luminous deep teal background. The same artwork is used in the Android launcher drawable and main store icon files; a rebuilt APK is required to update an installed app. See the icon provenance document for the latest console-upload evidence.

User delegated competitive pricing. Launch prices are below knitCompanion's published US $30 annual base price (https://www.knitcompanion.com/), reflecting Craftfolio's narrower offline MVP. Annual savings against 12 monthly payments are approximately 37%. Other regional prices retain Amazon's USD conversion; UK prices were overridden explicitly. These are the console's list prices, not a guarantee of every customer's final charge or developer royalties.

| Currency / marketplace | Monthly | Annual |
| --- | ---: | ---: |
| GBP / UK | 1.99 | 14.99 |
| USD / US | 1.99 | 14.99 |
| CAD / Canada | 2.79 | 21.00 |
| EUR / Germany, Spain, Italy, France | 1.78 | 13.38 |
| INR / India | 188.95 | 1423.30 |
| JPY / Japan | 312 | 2351 |
| AUD / Australia | 2.83 | 21.30 |
| MXN / Mexico | 37.13 | 279.69 |
| BRL / Brazil | 9.93 | 74.76 |

Prices persisted after reloading. On the user's conditional submission request, selected **Submit to Test**, because production release checks are incomplete. Amazon's IAP dashboard now lists **TEST Craftfolio Premium**, status **Approved**, last submitted **Oct 24, 2026**, price range **USD 1.99–USD 14.99**. Evidence: `test-evidence/amazon-console/premium-test-approved.png`. This is test-catalogue approval for Live App Testing, not production approval, app submission, or proof of a successful purchase. Regional prices do not establish app distribution availability.

The receipt SKU mapping is now fixed locally: `BillingCatalog` limits product lookup/purchases to the two term SKUs, while selecting `craftfolio_premium` parent receipts for server verification. The verifier validates the RVS `productId` against that parent. It does not treat `termSku` as an entitlement identity, including App Tester's generated `_term` suffix. Local regression tests cover both terms, the parent identity, unrelated products and sandbox/production separation. Monthly/annual labels are explicit in the purchase UI; prices still come from Amazon. This does not constitute an actual Amazon purchase test.

See [verification hosting and billing test runbook](../verification-service/HOSTING.md). Hosting provider selection remains pending. The user authorised dedicated tablet use and reboot for testing; that persistence check passed. App Tester is installed on the connected KFTRWI tablet, but its catalogue and billing configuration have not been changed during this work.

## Developer configuration still required

1. Upload the correctly signed APK to the existing draft and confirm package identity `com.craftfolio.patterns` before submission.
2. Keep the downloaded public `AppstoreAuthenticationKey.pem` in `app/src/main/assets/`. This is a public app key, not the RVS shared secret; it does not replace server receipt verification.
3. Premium is approved in Amazon's test catalogue. Confirm the corrected term SKU vs parent SKU mapping and actual purchases/restores in App Tester and LAT. Submit/promote to live only after the production release checks pass. Use the recorded launch prices and no trial/promotion; verify renewal disclosures and applicable taxes before release.
4. Deploy `verification-service/server.mjs` under HTTPS at `/verify`, behind a reverse proxy with body limits and rate limiting. Node 22+, no npm dependencies. Set `AMAZON_RVS_SECRET` only in the host secret store. `PORT` defaults to 8080, loopback binding. Never log upstream URLs (they contain the secret), account IDs or receipts. Configure operational monitoring without these values.
5. Set `-PverificationUrl=https://your-host/verify` when building. APK includes only the public endpoint. The service calls Amazon RVS, verifies product/receipt/type/purchase date and rejects sandbox receipts in production. `RVS_SANDBOX=true` is for a separate test deployment only.
6. Test App Tester with a separate `RVS_SANDBOX=true` deployment. For Live App Testing receipts, use a separate staging deployment with production RVS (`RVS_SANDBOX=false`) and `ALLOW_AMAZON_TEST_RECEIPTS=true`; LAT receipts are marked as beta/test by Amazon. Keep both flags false on the customer production verifier. Never enable a test-receipt override from a client request. Test purchase, decline, pending, restored purchase, expired subscription, refund, account switch, offline cache, reconnect and multiple historical receipts. Pending purchases never unlock until a successful verified receipt arrives.
7. Configure signing, support email, hosted privacy policy, marketplace availability, age rating and distribution exclusions. Validate current store asset requirements in the console.

RVS returns `cancelDate` as loss of access, including when a cancelled auto-renew term runs out. A past `renewalDate` alone is not treated as loss of access. The server returns at most a 72-hour entitlement lease (or earlier cancel date), and the client rejects clock rollback relative to last verification. This is bounded offline caching, not tamper-proof DRM. Imported personal files never require verification. Local purchase callbacks alone never grant Premium.

Known commerce gate: service deployment, account/product mapping, Amazon credentials and supported Amazon purchase tests are not available from source code alone. Subscription support must remain unverified until actual evidence is recorded. No live transaction has been performed by this build task.

## Public release preparation and signed-build persistence follow-up

The signed production APK (SHA-256 in the artifact manifest) was submitted to Amazon on 25 October 2026 after the owner explicitly certified public export compliance and approved submission. Amazon confirms Successfully submitted app submission on 25 Oct 2026. Review now shows valid Productivity category, complete content rating, public privacy URL and existing localized descriptions/assets. Privacy questionnaire declares User IDs, purchase history and network/device identifiers for functionality and fraud prevention; provider processing uses Amazon's stated service-provider exception for third-party transfer. No payment-card data or imported documents are sent to the verifier. Target audience is set to adults (18+), with 12 eligible Fire tablets selected, no Fire TV/automotive. Reviewer instructions were entered. Public export certification was checked with owner approval. Premium was promoted out of TEST and the dashboard reports Approved. App review/publication remain pending; the app is not claimed live.

A new project was created on the Amazon-installed LAT build using the system PDF picker. After normal closure and forced termination offline, page 2 of 3, zoomed reading view, Rows 3, second counter Ribbing 1, materials 200 g wool and 5 mm needles, and note Resume moss stitch after row 3 all remained visible. Screenshots are retained under billing-lat-workspace-* and billing-lat-notes-* in test-evidence. These are UI observations, not a release-private-database extraction.

The physical signed-build restart check PASSes: boot ID changed, Android reported no active network, and page 2, Rows 3, Ribbing 1, materials and note remained intact. Wi-Fi was restored. After the annual LAT subscription was reset, the project and existing counters remained usable under Free; exported PDF bytes exactly matched the original SHA-256 ec14a3fa13d7a4373e39b9a8774bd5aa729d95c76a5fad3ac967ee4e80344a8c. This is observed UI/export evidence, not a release-private-database extraction.
