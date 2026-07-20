package net.ifmain.hwanultoktok.kmp.widget

import kotlinx.datetime.LocalDateTime
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WidgetRateSnapshotTest {
    @Test
    fun jpy_snapshot_keeps_official_hundred_yen_rate() {
        val exchangeRate = ExchangeRate(
            currencyCode = "JPY",
            currencyName = "일본 옌",
            currencyUnit = "JPY(100)",
            buyingRate = 900.0,
            sellingRate = 910.0,
            baseRate = 905.0,
            bookPrice = 905.0,
            timestamp = LocalDateTime(2026, 7, 20, 12, 0),
        )

        val snapshot = exchangeRate.toStoredWidgetRate()

        assertEquals("JPY", snapshot.currencyCode)
        assertEquals("JPY(100)", snapshot.currencyUnit)
        assertEquals(905.0f, snapshot.rate)
    }

    @Test
    fun legacy_jpy_value_resets_comparison_baseline_when_official_unit_is_introduced() {
        val jpySnapshot = StoredWidgetRate(
            currencyCode = "JPY",
            currencyUnit = "JPY(100)",
            rate = 905.0f,
        )
        val usdSnapshot = StoredWidgetRate(
            currencyCode = "USD",
            currencyUnit = "USD",
            rate = 1_350.0f,
        )

        assertTrue(jpySnapshot.requiresBaselineReset(storedCurrencyUnit = null))
        assertFalse(usdSnapshot.requiresBaselineReset(storedCurrencyUnit = null))
    }
}
