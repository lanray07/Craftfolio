# Privacy disclosure draft

This draft describes the implemented application. Add the developer's business/contact details and the production verification-service host, review Amazon SDK disclosures, and publish an accessible policy URL before store submission.

Craftfolio stores imported patterns, projects, notes, materials requirements, counters, undo history and reading positions in private storage on your device. It does not require an account for the local workspace. The application has no advertising or custom analytics and does not upload pattern files or project notes.

If subscriptions are configured, Amazon manages purchases and renewals. Craftfolio sends Amazon user and receipt identifiers to the configured HTTPS verification service so that it can validate the purchase with Amazon. Verified entitlement dates and the current receipt are cached locally. The reference service does not create a receipt database; production host/proxy logging and retention must be configured and disclosed by the developer. Amazon processes purchase information under its own terms and privacy policy.

Imported originals can be opened and exported without Premium, including after a subscription expires. The source file chosen in the system picker is not modified. The app's private copy and project database are removed when Android app data is cleared or Craftfolio is uninstalled. Keep your source files or export originals before uninstalling. This version does not provide project backup/restore or cloud synchronisation.

Core operation requires no microphone, camera, account or broad storage permission. The system file picker grants access to files the user explicitly selects. Internet permission supports purchase verification; local reading and progress updates work without connectivity.

Support, privacy requests and developer contact: configure before publishing. No cloud account is created by the local workspace, and the reference verification service has no user account to delete. Subscription cancellation is managed through the user's Amazon account.
