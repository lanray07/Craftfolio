# Localisation — completed 25 October 2026

The user explicitly requested every Amazon localisation option for both the app and store listings. Current coverage supersedes the earlier five-locale scope below. The rest of Phase 2 remains excluded.

## Current coverage: every option offered by this console

- **App: 16 locales / 11 languages**, with all 73 strings in every resource set: English (US, UK, Australia, India), German, French, Spanish (Spain, Mexico), Italian, Japanese, Dutch, Polish, Portuguese (Brazil, Portugal), Swedish and Simplified Chinese. The language chooser persists its app-only preference.
- **Main descriptions: all 15 offered locales**, including titles, short/long copy, feature bullets and keywords. All saved title/description/bullet fields matched source after reload. Amazon tokenises the supplied keywords.
- **Main images: all 11 offered locales**, four native screenshots plus two brand icons per locale. The six new galleries' inherited US screenshots were replaced with their own captures. Reload checks found 44 distinct screenshot asset IDs, not duplicated default galleries.
- **Premium: all 12 offered locales**, including Chinese. Every saved title/description matched source after reload and all retained both icons. The updated product was submitted to Test only and returned TEST / Approved on 25 October. No production submission or purchase was performed.
- Amazon does not offer main-description/image slots for Chinese here. Dutch, Polish, European Portuguese and Swedish have main-description slots but no image or Premium slots. Those surfaces use Amazon's default copy/assets; do not claim native billing dialogs in unavailable languages. The app itself supports all 16 locales.
- The app-details Language Support form offers flags for Chinese, Dutch, English, French, German, Italian, Japanese, Polish, Portuguese and Spanish; those implemented languages are selected. It has no Swedish checkbox. Swedish remains implemented in the app and its available main-description slot; unrelated unsupported language flags are not selected.
- `store-assets/locales.json` records the exact per-surface matrix. This is every localisation option offered for Craftfolio, not a claim to support every unrelated language checkbox in Amazon's app-details form.

## Latest verification

- Final debug, androidTest and unsigned release build: PASS in 2m 46s; five JVM tests; lint 0 errors, 18 warnings. Evidence: `test-evidence/all-locales-final-build.log`.
- Physical KFTRWI Fire HD 10, API 28: two localisation tests PASS in 26.207s, exercising all 16 real chooser options and recreation persistence, translated messages and regional English. Evidence: `test-evidence/all-locales-final-fire-tests.log`.
- Eleven additional native capture jobs PASS, 44 new images; the earlier five sets contribute 20 images, for **64 local screenshots**. All new sets were visually reviewed in `test-evidence/all-locales-contact-{1,2,3}.png`, with an individual Japanese workspace check. The final Chinese set and language tests use the final 16-locale build; the other new sets use the preceding 15-locale build whose relevant UI/resources are identical. All are debug captures and require signed-release review.
- Resource coverage, selector/matrix consistency, formatting contracts, metadata limits and all 64 screenshot hashes/dimensions passed. Evidence: `test-evidence/all-locales-final-contracts.log`, `all-locales-assets.log` and `store-assets/validation.json`.
- Console evidence: `test-evidence/amazon-console/all-locales-description-verification.json`, `all-locales-image-verification.json`, `all-locales-premium-verification.json`, `all-locales-premium-test-approved.png`, `all-locales-japanese-gallery.png`. The available-locale AX snapshots document the differing Amazon surfaces.
- Latest packaged builds/hashes: `release-artifacts/all-amazon-locales-2026-10-25/manifest.json`.

Personal document contents, project names, notes and existing counter names are preserved, not translated. Native-speaker editorial review has not been performed. Production verifier hosting, actual billing tests, developer signing and the remaining release gates are still outstanding. This localisation run does not rerun the earlier offline reboot acceptance suite.

## Earlier five-locale baseline

The following records the first UK/US, German, French and Spanish implementation, before the all-Amazon extension above.

## Implemented and verified

- Five app locales: en-GB, en-US, de-DE, fr-FR, es-ES, with English fallback. Each resource set contains 73 strings. Controls, errors, billing disclosures and new default counter names are localised.
- The header's Language button offers native language names and Follow tablet language. Its app-only preference persists Activity recreation and does not change the tablet's system settings.
- Search accepts translated craft labels while stored craft identifiers remain stable. Errors resolve in the current UI language without exposing raw platform exception text.
- Imported documents and personal project titles, counters, notes and materials retain their original contents. They are not automatically translated.
- Main Amazon descriptions, keywords, icons and four genuine screenshots per locale are saved. Reload checks matched titles, short/long descriptions and feature bullets against store-assets/<locale>/metadata/listing.json; each image set contains four screenshots and both icon sizes.
- Premium descriptions and icons support the same five locales. The updated subscription was submitted to Test only; Amazon returned TEST / Approved, last submitted 25 October 2026. No purchase or production submission was performed.
- Amazon's English, French, German and Spanish language-support flags remained checked after reload. English covers both regional variants.

## Validation

- Final debug, androidTest and unsigned release builds succeeded. Five JVM tests passed; lint reports 0 errors and 18 warnings. Evidence: test-evidence/localization-release-checks.log.
- Two LocalizationTest cases passed on the physical Fire HD 10 (KFTRWI, API 28), including five-language UI switching and recreation persistence. Evidence: test-evidence/localization-final-fire-tests.log.
- Five native capture jobs passed, generating 20 screenshots. The job selects language using the real app controls, waits for PDF rendering, and restores the previous preference. All 20 were visually reviewed in a contact sheet, with individual full-size checks. Final evidence: test-evidence/localized-final-<locale>.log and test-evidence/localization-contact-sheet.png.
- Resource coverage/placeholder contracts, metadata limits, screenshot dimensions, provenance and hashes passed scripts/validate-localization.py and scripts/validate-assets.py. See store-assets/validation.json.
- Console reload evidence is under test-evidence/amazon-console/: localized-description-verification.json, localized-image-verification.json, premium-localization-verification.json, supported-languages.txt and localized-premium-test-approved.png.

The capture check was strengthened after rejecting an initial French screenshot taken before the PDF finished rendering. All five final sets were regenerated and only those final images uploaded. An earlier overlapping Gradle run hit a Windows JAR lock; the final sequential build succeeded. No lint errors were suppressed.

## Remaining release limits

Native-speaker editorial review has not been performed. These screenshots and physical language tests use the debug build; signed-release checks remain required. The baseline seven-test functional suite and offline reboot acceptance are earlier evidence, not rerun as part of this localisation follow-up.

Production remains blocked by verifier hosting/configuration, actual Amazon purchase tests, developer release signing and the remaining device/accessibility/store gates. The current build has an empty verification URL and must not be advertised as subscription-ready. See AMAZON.md, TEST-RESULTS.md and RELEASE.md.
