# Craftfolio — Patterns & Projects

Native Kotlin / Jetpack Compose offline workspace for Android-compatible Amazon Fire tablets. Phase 1 only. See [implementation plan](docs/IMPLEMENTATION.md) and [release checklist](docs/RELEASE.md). Submitted to Amazon for review on 25 October 2026 after signed LAT billing and offline persistence checks; publication and remaining coverage limitations are recorded in the release checklist.

## Build

Install JDK 17 and Android SDK platform 35/build tools. Point `local.properties` to your SDK (`sdk.dir=C:/Users/you/AppData/Local/Android/Sdk`). No Google Play Services are used.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease lintDebug
.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.craftfolio.patterns.JourneyTest
node --test verification-service/*.test.mjs
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Unsigned release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`. Release signing is deliberately external; never commit a signing key. Android Studio's **Generate Signed Bundle / APK → APK** can sign the release with your own keystore. Preserve the signing key for updates. Verify using SDK `apksigner verify --verbose signed.apk` before uploading to Amazon.

## Design and persistence

One app module with explicit Application-owned dependencies; ViewModel serialises user mutations and keeps stable observable queries; Room transactions protect counters/history and project creation. Reading transforms are coalesced per document so pointer events cannot flood counter writes. Five entities: Pattern, Project, Counter, CounterChange, Note. Schema snapshots in `app/schemas`. Migration 1→2 adds standalone reading positions without discarding data. Future schema changes require explicit migrations; destructive fallback is never enabled.

Imports use Storage Access Framework with no broad storage permissions. PDF/JPEG/PNG/WebP originals are copied into app-private storage, capped at 100 MB, validated, fsynced and renamed before database registration. A crash before registration can leave a recoverable orphan file; no automatic cleanup deletes final originals. Failed `.part` files can remain after process death and need a future recovery/cleanup policy. No project operation removes an imported original. Export original is always available without Premium. Deleting the app removes private storage; this release does not offer backup/restore.

PdfRenderer renders one page at a time at a bounded size; image decode is sampled. Zoom and normalised pan are stored per project and separately for standalone library reading. The main dashboard presents Continue project; offline resume uses stored state. The viewer does not implement annotations, text extraction or search inside PDFs.

## Entitlements

Free: 20 imports, 3 unfinished projects, 1 counter per project. Premium: unlimited imports/projects and additional counters. All existing patterns, projects, counters and notes remain usable after downgrade. Split view is free.

See `verification-service/` and [Amazon setup](docs/AMAZON.md). No price or trial is invented: purchase prices come from Amazon. Builds without the public authentication key and HTTPS verifier show an honest unavailable message and expose no purchase button. Do not publish such a build as subscription-ready.

Purchasable monthly/annual SKUs map to the single `craftfolio_premium` receipt parent. Catalogue queries include the parent and both terms. See the [hosting and billing-test runbook](verification-service/HOSTING.md) for the deployed free Cloudflare sandbox, LAT and production verifiers. The default endpoint is production; build with `-PverificationUrl=https://craftfolio-rvs-sandbox.lanraybanks.workers.dev/verify` for explicit App Tester testing. Sandbox and Amazon-distributed LAT monthly/annual purchase, Restore, offline restart and reset-to-Free pass on Fire HD 10. The production app is submitted to Amazon; review/publication and remaining coverage limits are documented in docs/RELEASE-SUMMARY.md. Latest signed APKs are in `release-artifacts/billing-2026-10-25/`; secure the [release signing key backup](docs/SIGNING.md) before publishing.

## Testing and assets

The app supports all Amazon localisation options: 16 regional locales across English, German, French, Spanish, Italian, Japanese, Dutch, Polish, Portuguese, Swedish and Simplified Chinese. Use the header's **Language** button or follow the tablet. Personal documents and project text retain their original contents. All 15 main-description, 11 image and 12 Premium locales offered by Amazon are configured; see [localisation results](docs/LOCALIZATION.md) for the surface matrix, physical Fire tests and 64 native screenshots. Latest APKs are in `release-artifacts/billing-2026-10-25/`; the production app is submitted to Amazon, and review/publication remain pending.

```powershell
python scripts/validate-localization.py
python scripts/validate-assets.py
.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.craftfolio.patterns.LocalizationTest
```

`JourneyTest` validates file copying, original preservation after source removal, corrupt import rejection, PDF rendering and database reopen persistence. JVM tests cover concurrent counters, undo after reopen, entitlement expiry and free limits. These tests do not substitute for UI, process-death and reboot checks.

`StoreFixtureTest` explicitly seeds original CC0 sample PDFs into a debug installation without clearing existing data. It is instrumentation-only and is not part of the release APK. Real UI captures and metadata are in `store-assets`. See the asset manifest for capture provenance and validation status.

See [test results](docs/TEST-RESULTS.md) for verified Fire hardware evidence and remaining release gates. [Privacy disclosure draft](docs/PRIVACY.md) needs developer contact and hosting details before publication.
