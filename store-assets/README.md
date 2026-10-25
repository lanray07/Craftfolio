# Amazon Appstore assets

Version 1.0 supports all Amazon localisation options following the user's explicit scope extension: 16 app locales across 11 languages. All 15 main descriptions, 11 image galleries and 12 Premium descriptions offered in this console are saved and reload-verified. The exact surface matrix is in locales.json; unsupported slots use Amazon defaults. Production release acceptance remains incomplete. Keyword research entries are hypotheses; no search-volume or ranking data is claimed.

Official requirements checked 24 October 2026:
https://developer.amazon.com/docs/app-submission/appstore-details.html

- Tablet screenshots: 3–10 genuine app images, PNG/JPEG, supported dimensions including 1200×1920 or 1920×1200.
- Icons: 114×114 and 512×512 PNG; review transparency and exact submission slot requirements in the console.
- Main and Premium icons use colourful coral/gold/teal yarn and a gold crochet hook on deep teal. See `brand/premium-icon-provenance.md`. The Android launcher uses the same artwork; APKs must be rebuilt and installed to change an existing device's icon.
- Long description: maximum 4,000 characters; short description: maximum 2,000 bytes; 3–5 feature bullets.

Run the `StoreFixtureTest` instrumentation class explicitly to install original CC0 samples into a debug app. It does not erase existing data. Build and install the debug app and androidTest APKs, then run `scripts/capture-store.py` with the connected device serial. The script invokes the explicit `StoreScreenshotCapture` job, which uses Compose actions and checks expected content and foreground-window focus before taking native display screenshots. The job is skipped during normal tests unless `storeCapture=true` is supplied. PNG files, hashes, dimensions and suggested keyword-rich caption copy are saved locally. Captions are separate copy; they are not fake UI or image overlays.

Each of the 16 locale folders contains four native screenshots, regional metadata and a hash/provenance manifest: 64 images locally. All 44 images in the 11 offered image locales are uploaded; inherited default galleries were replaced and the saved asset IDs are distinct. The other five sets are local QA evidence because Amazon offers no corresponding image slot here. The capture job uses the real app language selector, waits for PDF rendering and restores the prior preference without changing the system locale. Sample PDF/project contents remain in their original language. All new sets received visual review. Signed-release capture review and native-speaker editorial review remain outstanding. See docs/LOCALIZATION.md for evidence. Do not advertise excluded inventory, photos, annotations, voice, backup or marketplace features.
