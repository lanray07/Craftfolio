# Craftfolio billing artifacts — 25 October 2026

This set supersedes earlier localisation-only APKs. See `manifest.json` for exact hashes and endpoints. The production app was submitted to Amazon on 25 October 2026; Amazon approval/publication are pending.

- `Craftfolio-1.0.0-LAT.apk`: non-debuggable, release-signed, staging verifier. Uploaded and submitted to Amazon private Live App Testing as **Craftfolio 1.0 billing verification**. Amazon shows **Test in-progress**. The Amazon-installed build passes monthly LAT purchase, restore, offline force-stop/reopen and reset-to-Free. Annual purchase, Restore and offline force-stop/reopen also PASS after owner account authentication.
- `Craftfolio-1.0.0-production.apk`: non-debuggable, same release certificate, production verifier. Built and signature-verified; not submitted or physically tested.
- `Craftfolio-1.0.0-sandbox-debug.apk` and companion androidTest APK: explicit sandbox build used for the recorded physical tests. Do not publish these.

Build with JDK 17/Android SDK 35. Run `gradlew.bat assembleRelease` for production, or add `-PverificationUrl=https://craftfolio-rvs-staging.lanraybanks.workers.dev/verify` for LAT. Signing is external; see `docs/SIGNING.md` and securely back up the keystore/password before release.

Validation: 13 JVM tests, 22 Node tests, two physical billing instrumentation tests pass; lint 0 errors/16 warnings. Simulated monthly/annual purchase, restore, account switching and offline cache pass. Project data and six original-file hashes are unchanged. Refund/expiry remains unverified: App Tester showed cancelled, but Amazon sandbox still returned an active receipt with null cancelDate after restore.

Submission: Amazon confirms Successfully submitted app submission on 25 Oct 2026. The owner certified public export compliance and approved submission with the disclosed remaining coverage limits. Premium was promoted out of TEST and is Approved. Signed core project persistence now passes normal closure, forced termination and a changed-boot-ID physical restart offline; an original export after Premium ended matches source bytes. Amazon review/publication and remaining QA coverage are pending. No actual charged transaction or financial refund is claimed. See docs/RELEASE-SUMMARY.md. No Phase 2 work was started.
