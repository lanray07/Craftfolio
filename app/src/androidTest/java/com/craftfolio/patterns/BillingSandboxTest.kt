package com.craftfolio.patterns

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import org.json.JSONObject
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import org.junit.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith

/** Explicit sandbox gate. It never initiates a purchase or changes personal project data. */
@RunWith(AndroidJUnit4::class)
class BillingSandboxTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun realAmazonSandboxCatalogueAndAccountAreReady() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("billingSandbox") == "true")
        assertTrue(BuildConfig.DEBUG)
        assertEquals("https://craftfolio-rvs-sandbox.lanraybanks.workers.dev/verify",BuildConfig.VERIFICATION_URL)
        // Amazon's mode probe reports UNKNOWN until its documented licensing initialisation runs.
        // This test does not gate personal files or projects on DRM licensing.
        ui.runOnUiThread { LicensingService.verifyLicense(ui.activity.applicationContext) { } }
        ui.waitUntil(30000) { LicensingService.getAppstoreSDKMode() != "UNKNOWN" }
        val mode = LicensingService.getAppstoreSDKMode()
        assertTrue("Expected Amazon sandbox SDK mode, received $mode", mode.contains("sandbox",ignoreCase=true) || mode.equals("test",ignoreCase=true))
        val billing = (ui.activity.application as CraftfolioApp).billing
        assertTrue(billing.configured)
        ui.waitUntil(30000) { billing.purchaseReady.value && billing.products.value.size == 2 }
        assertEquals(BillingCatalog.purchaseSkus,billing.products.value.map { it.sku }.toSet())
        assertTrue(billing.products.value.all { it.price.isNotBlank() })
    }

    @Test fun captureExistingSandboxReceiptForVerification() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("billingSandbox") == "true")
        assertTrue(BuildConfig.DEBUG)
        assertEquals("https://craftfolio-rvs-sandbox.lanraybanks.workers.dev/verify",BuildConfig.VERIFICATION_URL)
        ui.runOnUiThread { LicensingService.verifyLicense(ui.activity.applicationContext) { } }
        ui.waitUntil(30000) { LicensingService.getAppstoreSDKMode() != "UNKNOWN" }
        assertEquals("SANDBOX",LicensingService.getAppstoreSDKMode())
        val billing = (ui.activity.application as CraftfolioApp).billing
        val received = AtomicReference<PurchaseUpdatesResponse>()
        try {
            ui.runOnUiThread {
                PurchasingService.registerListener(ui.activity.applicationContext, object : PurchasingListener by billing {
                    override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
                        if (response.requestStatus == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL && response.receipts.isNotEmpty()) received.set(response)
                        billing.onPurchaseUpdatesResponse(response)
                    }
                })
                PurchasingService.getPurchaseUpdates(true)
            }
            ui.waitUntil(30000) { received.get() != null }
            val response = received.get()
            val receipt = response.receipts.first { BillingCatalog.isPremiumReceipt(it.sku) }
            // Explicit test-only private replay input. Never print IDs or ship this in the release APK.
            File(ui.activity.filesDir,"billing-sandbox-request.json").writeText(JSONObject()
                .put("userId",response.userData.userId).put("receiptId",receipt.receiptId).toString())
        } finally { ui.runOnUiThread { PurchasingService.registerListener(ui.activity.applicationContext,billing) } }
    }
}
