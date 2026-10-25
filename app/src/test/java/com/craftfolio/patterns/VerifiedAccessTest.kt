package com.craftfolio.patterns

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class, manifest = Config.NONE)
class VerifiedAccessTest {
    private val prefs get() = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("billing-state-test",Context.MODE_PRIVATE)
    private var clock = 1000L
    private fun store() = VerifiedAccess(prefs,"https://sandbox.example/verify") { clock }
    @Before fun reset() { prefs.edit().clear().commit(); clock = 1000L }

    @Test fun historicalReceiptCannotShortenAnotherVerifiedLeaseInEitherCompletionOrder() {
        for (reverse in listOf(false,true)) {
            prefs.edit().clear().commit()
            val state = store(); state.identify("account")
            val old = state.begin("account","old")!!; val newer = state.begin("account","new")!!
            if (reverse) { state.accept(newer,8000); state.accept(old,2000) }
            else { state.accept(old,2000); state.accept(newer,8000) }
            clock = 3000; assertTrue(state.premium()); assertTrue(store().premium()); clock = 1000
        }
    }
    @Test fun revokingOneReceiptPreservesOtherAccessAndRevokingBothPersists() {
        val state=store(); state.identify("account")
        state.accept(state.begin("account","one")!!,5000); state.accept(state.begin("account","two")!!,6000)
        state.accept(state.begin("account","two")!!,0); assertTrue(store().premium())
        state.accept(state.begin("account","one")!!,0); assertFalse(store().premium())
    }
    @Test fun aNewerCheckForTheSameReceiptInvalidatesTheOlderInFlightResult() {
        val state=store(); state.identify("account")
        val older=state.begin("account","receipt")!!; val newer=state.begin("account","receipt")!!
        state.accept(newer,0)
        assertFalse(state.accept(older,5000)); assertFalse(state.premium())
    }
    @Test fun switchingAccountsRejectsOldResultsEvenWhenSwitchingBack() {
        val state=store(); state.identify("first")
        val old=state.begin("first","receipt")!!; state.accept(old,5000)
        state.identify("second"); assertFalse(state.premium()); assertNull(state.begin("first","other"))
        state.identify("first"); assertFalse(state.accept(old,5000)); assertFalse(store().premium())
    }
    @Test fun matchingVerifierKeepsLegacyLeaseButRevocationDoesNotResurrectIt() {
        prefs.edit().putString("environment","https://sandbox.example/verify").putString("user","account").putString("receipt","legacy").putLong("until",5000).putLong("checked",1000).commit()
        val state=store(); assertTrue(state.premium()); state.identify("account")
        state.accept(state.begin("account","legacy")!!,0); assertFalse(store().premium())
    }
    @Test fun expiryClockRollbackAndExcessiveServerLeaseNeverGrantAccess() {
        val state=store(); state.identify("account"); state.accept(state.begin("account","receipt")!!,5000)
        clock=999; assertFalse(state.premium()); clock=5000; assertFalse(state.premium())
        assertTrue(runCatching { state.accept(state.begin("account","receipt")!!,clock+72*3600000L+1) }.isFailure)
    }
    @Test fun changingVerifierCannotCarryTestAccessIntoProduction() {
        val sandbox=store(); sandbox.identify("account"); sandbox.accept(sandbox.begin("account","receipt")!!,5000)
        val production=VerifiedAccess(prefs,"https://production.example/verify") { clock }
        assertFalse(production.premium())
        production.identify("account"); assertFalse(production.premium())
    }
    @Test fun legacyCacheWithoutVerifierIdentityRequiresReverification() {
        prefs.edit().putString("user","account").putString("receipt","legacy").putLong("until",5000).putLong("checked",1000).commit()
        assertFalse(store().premium())
    }
}
