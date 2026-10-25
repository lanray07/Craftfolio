package com.craftfolio.patterns

/** Amazon sells a term SKU, but purchase/restoration receipts identify its parent. */
object BillingCatalog {
    const val PREMIUM_SUBSCRIPTION = "craftfolio_premium"
    const val MONTHLY = "craftfolio_premium_monthly"
    const val ANNUAL = "craftfolio_premium_annual"
    val purchaseSkus: Set<String> = linkedSetOf(MONTHLY, ANNUAL)
    val catalogSkus: Set<String> = purchaseSkus + PREMIUM_SUBSCRIPTION

    fun isPremiumReceipt(sku: String) = sku == PREMIUM_SUBSCRIPTION
}
