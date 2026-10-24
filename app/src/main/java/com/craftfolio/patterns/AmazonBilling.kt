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
    private val prefs = context.getSharedPreferences("verified-entitlement", Context.MODE_PRIVATE)
    val products = MutableStateFlow<List<Product>>(emptyList())
    val message = MutableStateFlow("")
    val premium = MutableStateFlow(false)
    val configured = BuildConfig.VERIFICATION_URL.startsWith("https://") && runCatching { context.assets.open("AppstoreAuthenticationKey.pem").close() }.isSuccess
    @Volatile private var userId: String? = null
    private val skus = setOf("craftfolio_premium_monthly", "craftfolio_premium_annual")
    init {
        refreshCache()
        if (configured) { PurchasingService.registerListener(context, this); refresh() }
        scope.launch { while (isActive) { delay(30000); refreshCache() } }
    }
    private fun refreshCache() { premium.value = Rules.premium(prefs.getLong("until", 0), prefs.getLong("checked", 0), System.currentTimeMillis()) }
    fun refresh() { if (configured) { PurchasingService.getUserData(); PurchasingService.getProductData(skus); PurchasingService.getPurchaseUpdates(true) } }
    fun purchase(sku: String) { if (configured && sku in skus) PurchasingService.purchase(sku) }
    override fun onUserDataResponse(response: UserDataResponse) {
        if (response.requestStatus == UserDataResponse.RequestStatus.SUCCESSFUL) {
            userId = response.userData.userId
            if (prefs.getString("user", null) != userId) { prefs.edit().clear().commit(); premium.value = false }
        }
    }
    override fun onProductDataResponse(response: ProductDataResponse) {
        if (response.requestStatus == ProductDataResponse.RequestStatus.SUCCESSFUL) products.value = response.productData.values.filter { it.productType == ProductType.SUBSCRIPTION }.toList()
        else message.value = context.getString(R.string.billing_failed)
    }
    override fun onPurchaseResponse(response: PurchaseResponse) {
        if (response.requestStatus == PurchaseResponse.RequestStatus.SUCCESSFUL) verify(response.userData.userId, response.receipt)
        else message.value = context.getString(R.string.billing_failed)
    }
    override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
        if (response.requestStatus == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
            response.receipts.forEach { verify(response.userData.userId, it) }
            if (response.hasMore()) PurchasingService.getPurchaseUpdates(false)
        } else message.value = context.getString(R.string.billing_failed)
    }
    private fun verify(user: String, receipt: Receipt) {
        if (receipt.productType != ProductType.SUBSCRIPTION) return
        scope.launch {
            runCatching {
                check(userId == null || userId == user) { "Amazon account changed" }
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
                    check(userId == null || userId == user) { "Amazon account changed" }
                    check(until >= 0 && until <= now + 72 * 60 * 60 * 1000L)
                    if (until > now || prefs.getString("receipt", null) == receipt.receiptId) {
                        check(prefs.edit().putString("user", user).putString("receipt", receipt.receiptId).putLong("until", until).putLong("checked", now).commit())
                    }
                    refreshCache()
                    if (until > now) PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
                    message.value = context.getString(R.string.purchase_checked)
                } finally { connection.disconnect() }
            }.onFailure { message.value = context.getString(R.string.verification_failed) }
        }
    }
}
