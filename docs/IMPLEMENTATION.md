# Craftfolio 1.0 implementation plan

Repository inspection: empty directory, no existing source or repository instructions. Latest scope overrides supplied broad briefs. Phase 2 is not started.

1. Durable storage: Room schema, foreign keys, transactional counter history; bounded imports copied, synced and atomically renamed before DB registration. No destructive migrations or subscription-triggered deletion.
2. Library and projects: local search/craft filter, create/edit/status transitions, notes and requirements. Free: 20 imports, 3 unfinished projects, one counter; Premium adds capacity and counters. Existing content stays usable.
3. Viewer: platform PdfRenderer, bounded page rendering, image sampling, zoom/pan, page and transform saved transactionally; wide split workspace, compact stacked layout.
4. Commerce: Amazon Appstore SDK, product prices from Amazon, purchase/restore callbacks, server RVS verification and bounded offline cache. Never embed developer secret.
5. Quality: unit/database tests, device persistence harness, screenshots captured from running app, UK/US metadata, release checklist. Physical Fire and live billing evidence required before release.

Target: Fire OS 7/8 Android-compatible tablets (API 26+), excluding older Fire OS 5 devices and non-Android devices. Compatibility is a target until tested. Manual dependency injection keeps this small app explicit. No background service, analytics, account, WorkManager or cloud storage is needed.
