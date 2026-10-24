# Version 1.0 release gates

Status: DEVELOPMENT — not approved for Appstore submission.

## Required acceptance journey

On each supported Fire tablet/OS, in portrait and landscape:

1. Open a fresh install; use the system file picker to import an original PDF and each supported image format.
2. Verify search/craft filtering, create/edit project, change materials, pause/resume/complete.
3. Read page 2, zoom/pan; increment, decrement and undo. Save a note.
4. Wait for saved values to appear, close normally, disable internet, reopen and Continue project. Verify original bytes, page, zoom, counters, notes and requirements.
5. Repeat after `adb shell am force-stop com.craftfolio.patterns`; reopen.
6. Repeat after a full device restart. Record device model, OS/API, build SHA-256, timestamp, expected and observed values. Do not infer reboot success from a database reopen test.
7. Rotate and use split-screen/font size 200%, TalkBack and keyboard. Test 7-, 8-, 10-inch layouts and Fire OS 7/8. Record screenshots and observed clipping/focus issues.
8. Test corrupt/password-protected PDFs, cancelled imports, low storage, interrupted import, 100 MB cap, large PDF and hundreds of library entries. Profile memory and rapid counters.
9. Run the Amazon purchase matrix in `AMAZON.md`; demonstrate no entitlement can hide/export-block an existing imported file or existing counter.
10. Sign the release APK; install and verify that exact signed artifact. Confirm no debug data or secrets. Review store screenshots against the exact version, feature list, spelling and locale.

## Known limits

- API 26+ targets Android-compatible Fire OS 7/8; Fire OS 5 and Vega OS are excluded. Minimum SDK alone is not a compatibility certification.
- No encrypted backup/restore or multi-device synchronisation. App-private data is lost on uninstall; original-file export is available.
- No PDF text search, annotations, photo journal, voice, AI, inventory, shopping list, marketplace or packs.
- Multiple counters and billing code require device and store verification as documented. A configured verifier and app public key are required to offer purchases.
- Screen reader, low-memory, damaged PDF and all device-size claims require recorded results; do not advertise untested compatibility.

## Post-MVP backlog (blocked by Phase 1 acceptance)

- 1.1: annotations, photo journal, materials inventory, shopping lists.
- 1.2: optional voice, validated additional translations, advanced analytics.
- 1.3: encrypted backup/restore, optional synchronisation.
- 2.0: licensed marketplace, one-time pattern packs.

No screens or buttons for these features belong in version 1.0.
