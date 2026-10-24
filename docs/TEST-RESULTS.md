# Craftfolio validation — 24 October 2026

Status: core offline journey verified on one physical Fire tablet. Commercial release acceptance remains incomplete.

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
- Production verifier deployment, product/term SKU mapping, app authentication public key, commercial pricing, signing and store-account configuration.
- Physical Fire OS 8, 7-inch and 8-inch models; these remain target configurations rather than certified devices.
- TalkBack, full keyboard focus traversal, large libraries/large PDFs, strict memory/performance measurements, low-storage injection, password-protected PDFs and interruption during the copy operation.
- Signed-release installation and final developer-console submission validation. Store screenshots are genuine captures of the tested debug build and require a final review against the signed release.

No emulator test is claimed. A dedicated emulator launch was attempted but did not finish booting; the process was stopped and was not used as evidence.

The normal functional instrumentation suite has seven tests. `StoreScreenshotCapture` is a separate explicit asset job, skipped unless the `storeCapture=true` instrumentation argument is supplied; both locale jobs were run successfully. These jobs operate real Compose controls, assert the expected content and foreground-window focus, and save Android display pixels. Regional metadata differs; the basic English interface is shared. Initial adb/Back-based captures failed visual review and were archived outside the deliverable assets; only the asserted native captures are accepted.

## Fixes made during validation

The system-picker journey exposed a stale library observation. Query flows now live in the ViewModel instead of being recreated during composition, and Room was updated to 2.8.5 with KSP2. Official Room release notes document related Flow race fixes: https://developer.android.com/jetpack/androidx/releases/room. The regression test and real picker journey pass after the change.

Amazon SDK 3.0.9 produces D8 stack-map warnings; its receiver cannot load in the plain JVM test environment. Database JVM tests deliberately use a plain Robolectric Application without the Android app manifest; actual Android builds and seven Fire tests exercise the packaged app. An incremental Windows D8 file lock was cleared by collecting the build daemon's mapped buffers; the final build passed. No tests or lint errors were hidden to obtain a passing result.
