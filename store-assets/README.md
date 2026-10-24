# Amazon Appstore assets

Only English UK/US are in scope for 1.0. Metadata is draft until the configured release passes acceptance. Keyword research entries are hypotheses; no search-volume or ranking data is claimed.

Official requirements checked 24 October 2026:
https://developer.amazon.com/docs/app-submission/appstore-details.html

- Tablet screenshots: 3–10 genuine app images, PNG/JPEG, supported dimensions including 1200×1920 or 1920×1200.
- Icons: 114×114 and 512×512 PNG; review transparency and exact submission slot requirements in the console.
- Long description: maximum 4,000 characters; short description: maximum 2,000 bytes; 3–5 feature bullets.

Run the `StoreFixtureTest` instrumentation class explicitly to install original CC0 samples into a debug app. It does not erase existing data. Build and install the debug app and androidTest APKs, then run `scripts/capture-store.py` with the connected device serial. The script invokes the explicit `StoreScreenshotCapture` job, which uses Compose actions and checks expected content and foreground-window focus before taking native display screenshots. The job is skipped during normal tests unless `storeCapture=true` is supplied. PNG files, hashes, dimensions and suggested keyword-rich caption copy are saved locally. Captions are separate copy; they are not fake UI or image overlays.

UK and US interface resources currently share the same English wording; both capture jobs show that shared English UI rather than claiming that the device's system locale was changed. Metadata and caption copy use regional spelling. Captures of the tested debug build receive a visual review; a final capture against the signed release build and developer-console validation remain required before submission. Do not advertise excluded inventory, photos, annotations, voice, backup or marketplace features.
