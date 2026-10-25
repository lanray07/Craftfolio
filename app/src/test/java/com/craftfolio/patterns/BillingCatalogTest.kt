package com.craftfolio.patterns

import org.junit.Assert.*
import org.junit.Test

class BillingCatalogTest {
    @Test fun onlyTermsAreBuyableAndParentReceiptIsRecognized() {
        assertEquals(setOf("craftfolio_premium_monthly", "craftfolio_premium_annual"), BillingCatalog.purchaseSkus)
        assertFalse(BillingCatalog.PREMIUM_SUBSCRIPTION in BillingCatalog.purchaseSkus)
        assertEquals(BillingCatalog.purchaseSkus + BillingCatalog.PREMIUM_SUBSCRIPTION, BillingCatalog.catalogSkus)
        assertTrue(BillingCatalog.isPremiumReceipt("craftfolio_premium"))
        BillingCatalog.purchaseSkus.forEach { assertFalse(BillingCatalog.isPremiumReceipt(it)) }
        assertFalse(BillingCatalog.isPremiumReceipt("unrelated_subscription"))
    }
}
