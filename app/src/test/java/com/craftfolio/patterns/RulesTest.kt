package com.craftfolio.patterns
import org.junit.Assert.*
import org.junit.Test
class RulesTest {
    @Test fun countersNeverOverflowOrGoNegative() { assertEquals(0, Rules.next(0, -1)); assertEquals(Int.MAX_VALUE, Rules.next(Int.MAX_VALUE, 1)) }
    @Test fun entitlementExpiresAndRejectsClockRollback() { assertTrue(Rules.premium(5000, 1000, 2000)); assertFalse(Rules.premium(5000, 1000, 5000)); assertFalse(Rules.premium(5000, 1000, 999)); assertFalse(Rules.premium(Long.MAX_VALUE, 1000, 1000 + 72 * 3600000L)) }
}
