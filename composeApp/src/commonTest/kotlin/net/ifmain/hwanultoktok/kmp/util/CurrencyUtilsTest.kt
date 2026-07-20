package net.ifmain.hwanultoktok.kmp.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyUtilsTest {
    @Test
    fun officialCurrencyUnit_preserves_multiple_unit_for_jpy() {
        assertEquals("JPY(100)", CurrencyUtils.getOfficialCurrencyUnit("JPY"))
    }

    @Test
    fun officialCurrencyUnit_uses_currency_code_for_single_unit_currency() {
        assertEquals("USD", CurrencyUtils.getOfficialCurrencyUnit("USD"))
    }
}
