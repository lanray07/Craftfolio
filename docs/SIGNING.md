# Release signing

A dedicated RSA 3072-bit release key was created locally on 25 October 2026, outside Git and distinct from Android's debug key.

- Keystore: `C:\Users\User\.codex\private\craftfolio-signing\craftfolio-release.keystore`
- Alias: `craftfolio`; PKCS12 format.
- Certificate SHA-256: `2d98978aca0250ca6f298cc0550f6b1816612308fd0886c2dba0a7598a54538a`.
- Public certificate and `signing-info.json` are alongside the key.
- `release-password.dpapi` is encrypted for the current Windows user; it is not a portable password backup.

Securely back up the keystore and password separately before publishing. Losing the key may prevent updates. Use owner-controlled encrypted storage; never paste passwords into chat or commit signing material. The DPAPI file alone can become unrecoverable after losing the Windows profile.

The LAT and production APKs in `release-artifacts/billing-2026-10-25/` verify with SDK `apksigner` v2/v3 and share this certificate. The LAT APK targets staging; the production APK targets production. Signature verification and console upload do not prove signed-build device or billing acceptance.

The original debug app was backed up and removed only with explicit owner approval; the currently installed test app was delivered by Amazon and is non-debuggable. Monthly/annual LAT billing and signed offline project persistence checks now pass. Do not remove personal data for future signature changes without securing it first. Build with JDK 17/SDK 35 and sign externally through Android Studio or SDK `apksigner`, entering passwords securely. Run `apksigner verify --verbose --print-certs <apk>` and compare the certificate before upload.
