// Public app disclosure. No scripts, tracking pixels, cookies or receipt data.
export const privacyHtml = `<!doctype html>
<html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Craftfolio privacy policy</title>
<style>body{font:18px/1.65 system-ui,sans-serif;max-width:760px;margin:48px auto;padding:0 24px;color:#233d30;background:#f8f5ef}h1,h2{line-height:1.2}a{color:#315d50}</style>
<h1>Craftfolio privacy policy</h1><p>Updated 25 October 2026.</p>
<h2>Your patterns and projects</h2>
<p>Craftfolio stores imported patterns, projects, notes, materials, counters, undo history and reading positions in private storage on your device. The local workspace requires no Craftfolio account. The app has no advertising or custom usage analytics. It does not upload your pattern files, photos or project notes.</p>
<h2>Amazon subscriptions and verification</h2>
<p>Amazon manages purchases, payment information, subscription renewals and cancellation. To verify a purchase, Craftfolio sends the app-specific Amazon user identifier and receipt identifier over HTTPS to its receipt verifier hosted on Cloudflare Workers. The verifier checks the receipt with Amazon and returns a limited period of Premium access. No payment-card details are sent to Craftfolio.</p>
<p>The verifier has no customer or receipt database and does not intentionally log request bodies, receipt identifiers or Amazon account identifiers. Application request logs and tracing are disabled. Cloudflare may process network metadata to operate and protect its hosting service. Amazon processes purchase data under its own privacy policy. These providers' policies apply to their processing: <a href="https://www.cloudflare.com/privacypolicy/">Cloudflare privacy policy</a> and <a href="https://www.amazon.com/privacy">Amazon privacy notice</a>.</p>
<p>Verified entitlement dates, account identifiers and receipt identifiers are cached locally, scoped to the verifier used by the build. Verified Premium can work offline for up to 72 hours before another online check is required. Imported personal files remain accessible without Premium.</p>
<h2>Files, permissions and deletion</h2>
<p>Craftfolio uses Android's system file picker to access the files you select. It does not need microphone, camera or broad storage permission. Internet access supports Amazon billing and verification; local reading and project progress work offline.</p>
<p>The selected source file is not modified. Imported originals can be opened and exported after Premium ends. Clearing app data or uninstalling Craftfolio removes its local pattern copies and project database. Keep your source files or export originals beforehand. This version has no project backup, restore or cloud synchronisation.</p>
<h2>Contact and choices</h2>
<p>Contact Craftfolio support about privacy at <a href="mailto:banksmi@mail.com">banksmi@mail.com</a>. No cloud workspace account is created by Craftfolio. Manage or cancel subscriptions in your Amazon account; uninstalling the app does not cancel a subscription. Amazon retains its own purchase records under its policies.</p>
</html>`;
