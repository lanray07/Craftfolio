# Billing follow-up — 25 October 2026

Private LAT is **Test in-progress**. The owner authorized replacing the debug app after verified backup of six patterns, two projects, counters, two notes, preferences and original APK. All file hashes and database rows matched; SQLite integrity passed. Backup remains outside Git. The release installation starts empty and has no backup importer.

Amazon-installed Craftfolio 1.0.0/code 1 is non-debuggable, installer `com.amazon.venezia`, with App Tester sandbox mode `none`, on KFTRWI Fire HD 10/API 28. Narrow launch and metadata checks succeeded after an earlier combined inspection/launch command was blocked. No installed APK binary/hash inspection is claimed.

| Signed LAT case | Result |
| --- | --- |
| Catalogue | PASS: UK monthly £1.99, annual £14.99 |
| Monthly purchase | PASS: Amazon order complete, Premium, receipt checked |
| Restore | PASS: Premium retained |
| Offline force-stop/reopen | PASS: Premium retained, active default network none, Wi-Fi restored |
| LAT subscription reset then Restore | PASS: console success; app changed Premium to Free and receipt checked |
| Annual purchase | PASS: annual order complete, Premium, receipt checked; Restore and offline force-stop/reopen pass |
| LAT account switch | NOT RUN; sandbox account-switch passed |
| Real customer refund or natural expiry | NOT RUN; LAT reset is not a financial refund |

The user authorized LAT terms with a £0 spending limit. Amazon says LAT IAPs are free (https://developer.amazon.com/apps-and-games/blogs/2025/10/how-to-choose-the-right-tool-for-IAP-testing). Native dialogs display regular renewal prices. No actual charged transaction is claimed or authorized. Monthly LAT verification validates the staging real secret against Amazon production RVS.

Evidence under ignored test-evidence/: billing-lat-monthly-verified.png, billing-lat-offline-premium.png, billing-lat-reset-free.png, billing-lat-annual-dialog.png, amazon-console/craftfolio-lat-subscription-reset.png, billing-lat-installed-provenance.json, billing-lat-results.json. Do not publish private tester contact details or raw account/receipt identifiers.

Earlier sandbox monthly/annual purchase, restore, cancelled confirmation, account switch and offline tests passed, as did production rejection of a real sandbox receipt with client override flags. Sandbox transaction cancellation itself remained inconclusive (Amazon returned null cancelDate). The later LAT reset verifies access removal, but does not prove customer refund/expiry processing. Environment/account/generation-bound caching, multiple receipts and stale-response handling have JVM coverage. Workerd's redirect incompatibility was fixed with a failing-then-passing regression and hosted sandbox replay.

Build checks: 13 JVM, 22 Node and two physical sandbox billing instrumentation tests pass. Lint 0 errors/16 warnings. Signed staging/production builds and apksigner v2/v3 verify. Production customer transactions, signed LAT account-switch and remaining release/device/accessibility gates remain outstanding. The production app was submitted to Amazon on 25 October 2026 after explicit owner certification/approval; review/publication are pending.

# Craftfolio validation — 24 October 2026

Status: core offline journey verified on one physical Fire tablet. Commercial release acceptance remains incomplete.

## All Amazon locales — 25 October 2026

The user extended scope to all Amazon localisation options for app and store. Final 16-locale build passed (2m 46s), five JVM tests passed, lint has 0 errors and 18 warnings. Two physical Fire tests passed in 26.207s across all 16 chooser options, persistence after recreation, messages and regional English. Eleven additional native asset jobs passed; together with the earlier five sets, 64 genuine screenshots pass hash/dimension checks. The 44 new images received visual review, including Japanese/Chinese glyph rendering and PDF readiness. All 15 main descriptions, all 11 image galleries (44 unique screenshot assets) and all 12 Premium localisations offered by this console were saved and reload-verified. Premium returned TEST / Approved after Test-only resubmission. Details/evidence and surface restrictions are in LOCALIZATION.md. Latest binaries/hashes: `release-artifacts/all-amazon-locales-2026-10-25/manifest.json`.

Native-speaker review has not been performed. Signed-release checks, verifier deployment and real Amazon purchase tests remain outstanding. No new offline reboot or full baseline functional-suite run is claimed.

## Localisation follow-up — 25 October 2026

UK/US English, German, French and Spanish are implemented in the app and saved main/Premium test listings. The latest sequential debug/androidTest/unsigned release build succeeded; five JVM tests passed and lint reports 0 errors, 18 warnings. Two localisation instrumentation tests passed on the physical KFTRWI (language switching across all five choices, recreation persistence and resource formatting). Five separate native screenshot jobs passed, yielding 20 visually reviewed and uploaded screenshots. Resource/placeholder, metadata and asset/provenance validators passed. Evidence and limitations are in [LOCALIZATION.md](LOCALIZATION.md). Latest packaged binaries and hashes are under `release-artifacts/localized-2026-10-25/`.

Amazon returned TEST / Approved for the localised Premium revision on 25 October. This is test-catalogue approval, not purchase testing or production approval. Native-speaker review, signed-release checks, verifier hosting and actual Amazon billing tests remain outstanding. The seven-test functional suite and offline restart results below are earlier baseline evidence; they were not rerun during this localisation follow-up.

## Billing preparation follow-up — 24 October 2026

- Fixed the Amazon subscription mapping: purchases use the monthly/annual term SKUs; the app and RVS verifier recognize parent `craftfolio_premium` receipts. Purchase buttons identify their monthly/annual billing period and use prices from Amazon.
- Five JVM tests pass, including a new parent/term catalogue regression, alongside the existing persistence and entitlement tests.
- Fifteen Node tests pass: eight receipt-policy cases and seven HTTP integration cases with simulated Amazon RVS responses. Coverage includes both terms under the parent, rejection of child/unrelated product IDs, App Tester's generated term suffix, sandbox/LAT separation from production, invalid requests, revoked receipts, upstream errors and traffic limits. Evidence: `test-evidence/billing-verifier-tests.log`. These are local automated tests, not Amazon purchase tests.
- Debug and unsigned release APKs build successfully with the downloaded public PEM. No real verification endpoint is configured in this build. Final build and lint evidence is in `test-evidence/billing-mapping-build-final.log` (BUILD SUCCESSFUL; lint 0 errors, 16 warnings). The initial combined build hit a Windows D8 file lock; stopping the Gradle daemon released it. A subsequent lint run caught two missing English resource entries for the term labels, which were added before the final successful run. No lint errors were suppressed.
- The physical KFTRWI tablet is connected and has Amazon App Tester installed. No billing purchases, device catalogue replacement, account changes or sandbox-mode changes have been performed in this follow-up. Hosting choice and dedicated tablet use are awaiting the user's reply. The prepared catalogue and deployment/test instructions are in `verification-service/testing/` and `verification-service/HOSTING.md`.
- Console setup now includes the public app key and saved Premium monthly/annual prices. This removes those setup omissions but does not establish working billing, product approval, verifier deployment or release signing.

Later on 24 October 2026, Premium was submitted with **Submit to Test** and Amazon's IAP dashboard returned **TEST / Approved**. This establishes test-catalogue approval only. No purchase was executed and no production app/IAP submission was made. Evidence: `test-evidence/amazon-console/premium-test-approved.png`. The current debug and release BuildConfig files still contain an empty `VERIFICATION_URL`; production verification hosting, actual billing tests and release signing remain blockers.

## Earlier baseline validation — 24 October 2026

| Check | Result | Evidence / scope |
|---|---|---|
| Debug and unsigned release APK build | PASS | `test-evidence/validated-build.log`; packaged APK hashes in `release-artifacts/manifest.json` |
| JVM tests | 4 PASS | Counter boundary/overflow, concurrent writes, undo after database reopen, entitlement expiry/clock rollback, Free creation limits and retained counters |
| Android instrumentation | 7 PASS | `test-evidence/instrumentation-final-fire.log` on KFTRWI, API 28 |
| PDF / JPEG / PNG / WebP import | PASS | Real copy/decode/render checks on Fire, byte equality after source removal |
| Invalid-file rejection | PASS | No library record added for invalid data |
| Schema migration 1→2 | PASS | Every table/history preserved in migration test; real installed-app upgrade also preserved all old fields and file hashes |
| Import refresh across picker lifecycle | PASS | Added regression test; repeated actual system-file-picker import of Autumn moss scarf appeared without restarting |
| Counter UI / undo | PASS | Instrumentation + actual plus/minus/undo journey |
| Notes and requirements | PASS | Entered through actual UI, restored offline after all three closure cases |
| Normal application closure | PASS | Back out of workspace, close Activity, reopen offline and Continue project; screenshot retained |
| Forced termination | PASS | `am force-stop`, reopen offline; database values and all six original-file hashes identical |
| Physical device restart | PASS | Changed Linux boot ID; Wi-Fi still disabled; all records and file hashes identical after reopen |
| Receipt-verifier policy | 6 PASS | Node tests: bounded lease, cancellation, future cancellation, past-renewal handling, invalid SKU/receipt/test data, explicit staging acceptance of Amazon test receipts |
| Android lint | 0 errors, 16 warnings | Mostly available dependency/target updates, deliberate synchronous entitlement persistence; no warnings suppressed |
| Store capture jobs | 2 PASS | Four asserted native UI captures per UK/US set; UK images visually inspected, US screen bodies matched within 0.2% |
| Asset validation | PASS | Eight genuine app PNGs at supported dimensions, 114/512px PNG icons, metadata lengths, file hashes; final signed-release capture still required |
| 200% font visual checks | Observed | Landscape library and stacked workspace; portrait capture in test-evidence. Large text requires scrolling. This is not a screen-reader certification. |

The physical device reports KFTRWI (Fire HD 10, 2021), Fire OS family 7, Android API 28, 1200×1920 native resolution. The test APK is the debug artifact in the release manifest; the unsigned release artifact has not been installed with a production signing key.

The persistence acceptance case restored PDF page 2 of 3, zoom 1.5, non-zero normalised pan, row count 3, materials “200 g forest wool and 5 mm needles”, and note “Resume moss stitch after row 3”. See `test-evidence/persistence-acceptance.json` and the before/after JSON snapshots. Wi-Fi was disabled and Android reported no active default network. Original-source bytes match the imported file. Wi-Fi was restored after testing. Fire OS required USB debugging approval again after reboot; verification resumed only after the user approved it.

## Not verified / release blockers

- Amazon App Tester, RVS sandbox integration, real subscription purchases/restoration/refunds/account switching, pending purchase delivery and Live App Testing. Client and server code exist; no live billing test or successful purchase is claimed.
- Production verifier deployment, Amazon validation of the corrected product/term mapping, signing and remaining store-account configuration. The public authentication key and draft commercial prices have now been set up.
- Physical Fire OS 8, 7-inch and 8-inch models; these remain target configurations rather than certified devices.
- TalkBack, full keyboard focus traversal, large libraries/large PDFs, strict memory/performance measurements, low-storage injection, password-protected PDFs and interruption during the copy operation.
- Signed-release installation and final developer-console submission validation. Store screenshots are genuine captures of the tested debug build and require a final review against the signed release.

No emulator test is claimed. A dedicated emulator launch was attempted but did not finish booting; the process was stopped and was not used as evidence.

The baseline functional instrumentation suite had seven tests. `StoreScreenshotCapture` is a separate explicit asset job, skipped unless the `storeCapture=true` instrumentation argument is supplied; the two original English locale jobs passed. These jobs operate real Compose controls, assert the expected content and foreground-window focus, and save Android display pixels. Initial adb/Back-based captures failed visual review and were archived outside the deliverable assets. The five-language follow-up above supersedes those original screenshot sets.

## Fixes made during validation

The system-picker journey exposed a stale library observation. Query flows now live in the ViewModel instead of being recreated during composition, and Room was updated to 2.8.5 with KSP2. Official Room release notes document related Flow race fixes: https://developer.android.com/jetpack/androidx/releases/room. The regression test and real picker journey pass after the change.

Amazon SDK 3.0.9 produces D8 stack-map warnings; its receiver cannot load in the plain JVM test environment. Database JVM tests deliberately use a plain Robolectric Application without the Android app manifest; actual Android builds and seven Fire tests exercise the packaged app. An incremental Windows D8 file lock was cleared by collecting the build daemon's mapped buffers; the final build passed. No tests or lint errors were hidden to obtain a passing result.

## Public release preparation and signed-build persistence follow-up

The signed production APK (SHA-256 in the artifact manifest) was submitted to Amazon on 25 October 2026 after the owner explicitly certified public export compliance and approved submission. Amazon confirms Successfully submitted app submission on 25 Oct 2026. Review now shows valid Productivity category, complete content rating, public privacy URL and existing localized descriptions/assets. Privacy questionnaire declares User IDs, purchase history and network/device identifiers for functionality and fraud prevention; provider processing uses Amazon's stated service-provider exception for third-party transfer. No payment-card data or imported documents are sent to the verifier. Target audience is set to adults (18+), with 12 eligible Fire tablets selected, no Fire TV/automotive. Reviewer instructions were entered. Public export certification was checked with owner approval. Premium was promoted out of TEST and the dashboard reports Approved. App review/publication remain pending; the app is not claimed live.

A new project was created on the Amazon-installed LAT build using the system PDF picker. After normal closure and forced termination offline, page 2 of 3, zoomed reading view, Rows 3, second counter Ribbing 1, materials 200 g wool and 5 mm needles, and note Resume moss stitch after row 3 all remained visible. Screenshots are retained under billing-lat-workspace-* and billing-lat-notes-* in test-evidence. These are UI observations, not a release-private-database extraction.

The physical signed-build restart check PASSes: boot ID changed, Android reported no active network, and page 2, Rows 3, Ribbing 1, materials and note remained intact. Wi-Fi was restored. After the annual LAT subscription was reset, the project and existing counters remained usable under Free; exported PDF bytes exactly matched the original SHA-256 ec14a3fa13d7a4373e39b9a8774bd5aa729d95c76a5fad3ac967ee4e80344a8c. This is observed UI/export evidence, not a release-private-database extraction.
