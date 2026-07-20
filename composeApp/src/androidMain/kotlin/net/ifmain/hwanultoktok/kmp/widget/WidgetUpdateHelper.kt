package net.ifmain.hwanultoktok.kmp.widget

import android.content.Context
import androidx.core.content.edit
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.number
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.util.getCurrentDateTime
import net.ifmain.hwanultoktok.kmp.util.getDataBaseDateWithoutHoliday
import kotlinx.datetime.LocalDate

/**
 *
 * @author gayoung.
 * @since 2025. 8. 14.
 *
 * 위젯 업데이트를 위한 헬퍼 클래스
 */
object WidgetUpdateHelper {
    
    /**
     * 환율 데이터를 SharedPreferences에 저장하고 위젯을 업데이트합니다.
     * ViewModel에서 환율 데이터를 받아올 때 호출합니다.
     */
    suspend fun saveExchangeRatesAndUpdateWidget(
        context: Context,
        rates: List<ExchangeRate>,
        dataDate: LocalDate? = null
    ) {
        val prefs = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)
        val calculatedDataDate = dataDate ?: run {
            val now = getCurrentDateTime()
            getDataBaseDateWithoutHoliday(now)
        }
        val newDataDateString = "${calculatedDataDate.year}-${calculatedDataDate.month.number.toString().padStart(2, '0')}-${calculatedDataDate.day.toString().padStart(2, '0')}"
        val previousDataDateString = prefs.getString("actual_data_date", null)
        val isNewOfficialDate = previousDataDateString == null || previousDataDateString != newDataDateString

        prefs.edit {
            rates.forEach { rate ->
                val snapshot = rate.toStoredWidgetRate()
                val key = "KRW_${snapshot.currencyCode}"
                val previousRate = prefs.getFloat(key, 0f)
                val storedBaseline = prefs.getFloat("${key}_prev", 0f)
                val storedCurrencyUnit = prefs.getString("${key}_unit", null)
                val shouldResetBaseline = snapshot.requiresBaselineReset(storedCurrencyUnit)

                val currentRate = snapshot.rate

                putFloat(key, currentRate)
                putString("${key}_unit", snapshot.currencyUnit)

                if (shouldResetBaseline) {
                    putFloat("${key}_prev", currentRate)
                } else if (isNewOfficialDate) {
                    val baseline = if (previousRate > 0f) previousRate else currentRate
                    putFloat("${key}_prev", baseline)
                } else if (storedBaseline == 0f && currentRate > 0f) {
                    putFloat("${key}_prev", currentRate)
                }
            }

            putLong("last_update_time", System.currentTimeMillis())
            
            // 실제 데이터 날짜 저장 (앱과 위젯 동기화용)
            putString("actual_data_date", newDataDateString)
        }

        withContext(Dispatchers.IO) {
            FavoriteExchangeRateWidget().updateAll(context)
        }
    }
    
    /**
     * 즐겨찾기가 변경되었을 때 위젯을 업데이트합니다.
     */
    suspend fun updateWidgetOnFavoriteChange(context: Context) {
        withContext(Dispatchers.IO) {
            FavoriteExchangeRateWidget().updateAll(context)
        }
    }
}

internal data class StoredWidgetRate(
    val currencyCode: String,
    val currencyUnit: String,
    val rate: Float,
)

internal fun ExchangeRate.toStoredWidgetRate(): StoredWidgetRate {
    return StoredWidgetRate(
        currencyCode = currencyCode,
        currencyUnit = currencyUnit,
        rate = baseRate.toFloat(),
    )
}

internal fun StoredWidgetRate.requiresBaselineReset(storedCurrencyUnit: String?): Boolean {
    return when {
        storedCurrencyUnit != null -> storedCurrencyUnit != currencyUnit
        else -> currencyUnit != currencyCode
    }
}
