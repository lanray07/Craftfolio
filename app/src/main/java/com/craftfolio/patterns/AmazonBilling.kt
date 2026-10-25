package com.craftfolio.patterns

import android.content.Context
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class AmazonBilling(private val context: Context) : PurchasingListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val access = VerifiedAccess(context.getSharedPreferences("verified-entitlement", Context.MODE_PRIVATE),BuildConfig.VERIFICATION_URL)
    val products = MutableStateFlow<List<Product>>(emptyList())
    val message = MutableStateFlow("")
    val premium = MutableStateFlow(false)
    val purchaseReady = MutableStateFlow(false)
    val configured = BuildConfig.VERIFICATION_URL.startsWith("https://") && runCatching { context.assets.open("AppstoreAuthenticationKey.pem").close() }.isSuccess
    @Volatile private var userId: String? = null
    init {
        refreshCache()
        if (configured) { PurchasingService.registerListener(context, this); refresh() }
        scope.launch { while (isActive) { delay(30000); refreshCache() } }
    }
    private fun refreshCache() { premium.value = access.premium() }
    private fun updatePurchaseReady() { purchaseReady.value = userId != null && products.value.isNotEmpty() }
    fun refresh() { if (configured) {
        userId = null; updatePurchaseReady()
        PurchasingService.getUserData(); PurchasingService.getProductData(BillingCatalog.catalogSkus); PurchasingService.getPurchaseUpdates(true)
    } }
    fun purchase(sku: String) {
        if (configured && purchaseReady.value && products.value.any { it.sku == sku } && sku in BillingCatalog.purchaseSkus) PurchasingService.purchase(sku)
    }
    override fun onUserDataResponse(response: UserDataResponse) {
        if (response.requestStatus == UserDataResponse.RequestStatus.SUCCESSFUL) {
            runCatching {
                access.identify(response.userData.userId)
                userId = response.userData.userId
                refreshCache(); updatePurchaseReady()
                // Initial update callbacks can precede user data; sync again once account identity is known.
                PurchasingService.getPurchaseUpdates(true)
            }.onFailure { userId = null; updatePurchaseReady(); message.value = AppLanguages.wrap(context).getString(R.string.verification_failed) }
        } else { userId = null; updatePurchaseReady(); message.value = AppLanguages.wrap(context).getString(R.string.billing_failed) }
    }
    override fun onProductDataResponse(response: ProductDataResponse) {
        if (response.requestStatus == ProductDataResponse.RequestStatus.SUCCESSFUL) products.value = response.productData.values
            .filter { it.productType == ProductType.SUBSCRIPTION && it.sku in BillingCatalog.purchaseSkus }
            .sortedBy { BillingCatalog.purchaseSkus.indexOf(it.sku) }
        else { products.value = emptyList(); message.value = AppLanguages.wrap(context).getString(R.string.billing_failed) }
        updatePurchaseReady()
    }
    override fun onPurchaseResponse(response: PurchaseResponse) {
        if (response.requestStatus == PurchaseResponse.RequestStatus.SUCCESSFUL) verify(response.userData.userId, response.receipt)
        else if (response.requestStatus == PurchaseResponse.RequestStatus.ALREADY_PURCHASED) PurchasingService.getPurchaseUpdates(true)
        else message.value = AppLanguages.wrap(context).getString(R.string.billing_failed)
    }
    override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
        if (response.requestStatus == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
            response.receipts.forEach { verify(response.userData.userId, it) }
            if (response.hasMore()) PurchasingService.getPurchaseUpdates(false)
        } else message.value = AppLanguages.wrap(context).getString(R.string.billing_failed)
    }
    private fun verify(user: String, receipt: Receipt) {
        // This only selects receipts to verify. It never grants access without server RVS validation.
        if (receipt.productType != ProductType.SUBSCRIPTION || !BillingCatalog.isPremiumReceipt(receipt.sku)) return
        if (userId != user) return
        val ticket = access.begin(user, receipt.receiptId) ?: return
        scope.launch {
            runCatching {
                val connection = URL(BuildConfig.VERIFICATION_URL).openConnection() as HttpsURLConnection
                try {
                    connection.requestMethod = "POST"; connection.doOutput = true
                    connection.connectTimeout = 15000; connection.readTimeout = 15000
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.outputStream.use { it.write(JSONObject().put("userId", user).put("receiptId", receipt.receiptId).toString().toByteArray()) }
                    check(connection.responseCode == 200)
                    val result = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                    val now = System.currentTimeMillis()
                    val until = result.getLong("validUntil")
                    check(result.getString("receiptId") == receipt.receiptId && result.getString("userId") == user)
                    check(until >= 0 && until <= now + 72 * 60 * 60 * 1000L)
                    if (!access.accept(ticket, until)) return@launch
                    refreshCache()
                    if (until > now) PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
                    message.value = AppLanguages.wrap(context).getString(R.string.purchase_checked)
                } finally { connection.disconnect() }
            }.onFailure { message.value = AppLanguages.wrap(context).getString(R.string.verification_failed) }
        }
    }
}
