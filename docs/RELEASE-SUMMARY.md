# Craftfolio 1.0 release record — 25 October 2026

Amazon confirms **Successfully submitted app submission on 25 Oct 2026**. This confirms submission, not approval or public availability. The owner explicitly approved public export certification and submission after reviewing the testing limits. Premium was promoted out of the TEST catalogue; its dashboard status is **Approved**.

## Implemented scope

The native Kotlin/Jetpack Compose app provides a private PDF/image pattern library, search and craft filters, project creation/editing/status changes, materials and notes, persistent row counters with undo, multiple named Premium counters, a zoomable/page-based PDF reader, saved reading position and a responsive split workspace. Room and app-private original-file copies support offline use. Original-file export and access to existing projects/counters remain available after Premium ends.

Free includes 20 imports, three unfinished projects and one counter per project. Premium adds unlimited imports/projects and additional counters, subject to device storage. Amazon supplies prices and manages renewals. Receipt verification is hosted on separate free Cloudflare sandbox, staging and production Workers; secrets stay server-only. Verified Premium has a maximum 72-hour offline lease. No Google Play Services or custom advertising/analytics service is used.

The app has 16 regional locales across 11 languages. Amazon's available main-description, image and Premium localisation surfaces are populated; see LOCALIZATION.md for the exact matrix and untranslated user-content boundaries. The public release targets adults 18+, all available countries and 12 eligible Fire tablets, with no Fire TV/automotive distribution. Physical testing covered KFTRWI Fire HD 10 (2021), Fire OS 7/API 28.

## Verified results

- 13 JVM tests, 22 Node verifier tests and two explicit physical sandbox billing instrumentation checks pass. Final build/lint reports 0 errors and 16 warnings. Production and LAT APK signatures verify with apksigner v2/v3.
- The Amazon-installed, non-debuggable LAT build passes monthly and annual purchase/receipt verification, Restore, offline forced restart, and test-subscription reset followed by a verified downgrade to Free.
- The signed LAT project journey passes PDF import, project creation, page navigation/zoom, increment/decrement/undo, named second counter, materials and note entry. Page 2, Rows 3, Ribbing 1, materials and note survive normal closure, forced termination and physical reboot offline. The changed boot ID confirms the reboot; Wi-Fi was restored.
- After Premium ended, the existing project, counters, materials and note remained accessible. An original-file export matched all 20,831 source bytes and SHA-256 `ec14a3fa13d7a4373e39b9a8774bd5aa729d95c76a5fad3ac967ee4e80344a8c`.
- The production verifier rejects an actual sandbox receipt despite client environment-override attempts. Account/environment binding and delayed/multiple-receipt races have regression coverage; sandbox account switching passes.

The old active-profile debug installation's six patterns, two projects, counters and two notes were privately backed up and verified before owner-authorized replacement. SQLite integrity, every recorded database row and original-file hashes matched. This engineering backup is outside Git; the application has no backup-import feature. The installed LAT app now contains the new test project and shows Free after the final disposable subscription reset.

## Build and artifacts

Use JDK 17 and Android SDK platform/build tools 35 with local.properties pointing to the SDK. Run Gradle builds sequentially on Windows:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease lintDebug
cd verification-service
npm ci --ignore-scripts
npm test
```

The default verifier endpoint is customer production. Add `-PverificationUrl=https://craftfolio-rvs-staging.lanraybanks.workers.dev/verify` to a release build for LAT, or the sandbox URL from HOSTING.md for an explicit debug App Tester build. Sign externally and verify with SDK apksigner; never commit signing keys or passwords.

`release-artifacts/billing-2026-10-25/` contains the signed production APK, signed LAT APK, sandbox debug/test APKs and a manifest with exact hashes. Production APK SHA-256: `72a95ece3b06f2fef0a1a2074534406dea23955d16c40e09113c7d5784d9cf6e`. The Amazon-installed LAT package's binary hash was not extracted; installer and non-debuggable metadata were verified. See SIGNING.md for the dedicated release key and owner backup instructions.

## Remaining coverage and Amazon configuration

Amazon review/publication is pending. App ID: `amzn1.devportal.mobileapp.4c9637fe3bfe4621b51d702fc7ebaa59`; public release: `amzn1.devportal.apprelease.e681623ce1824ef99066bf2bd1d37339`. Public authentication key, support email, privacy URL, catalogue, regional prices, locales, draft content/privacy disclosures and export certification are configured. No additional developer configuration blocker was reported by the final submission validation. Preserve and securely back up the release keystore and password; its local DPAPI password file alone is tied to the Windows profile.

Other physical tablet models/Fire OS 8, full screen-reader/keyboard QA, large-library performance, low-storage/interrupted-import injection and native-speaker translation review remain unverified. Signed LAT account switching and actual customer refund/natural-expiry processing were not run. LAT reset-to-Free is not a financial refund test; no actual charged transaction is claimed. The 72-hour limit is bounded caching rather than tamper-proof DRM. Uninstalling removes app-private files, so users must retain source files or export originals.

Evidence is retained locally under ignored test-evidence, including submission/promotion screenshots, signed workspace closure/reboot captures, redacted billing results and original-export hash proof. Do not publish raw receipt/account identifiers or private tester contacts.

## Post-MVP backlog

Phase 2 remains blocked until version 1.0 is stable: 1.1 annotations/photo journal/materials inventory/shopping lists; 1.2 optional voice, validated translations and analytics; 1.3 encrypted backup/restore and optional sync; 2.0 licensed marketplace and pattern packs. No unfinished screens/buttons for these features were added.
