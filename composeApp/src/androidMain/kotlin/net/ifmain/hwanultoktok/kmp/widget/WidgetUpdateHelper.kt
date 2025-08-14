package net.ifmain.hwanultoktok.kmp.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import androidx.core.content.edit

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
    fun saveExchangeRatesAndUpdateWidget(
        context: Context,
        rates: List<ExchangeRate>
    ) {
        val prefs = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)
        prefs.edit {
            rates.forEach { rate ->
                val key = "KRW_${rate.currencyCode}"
                val previousRate = prefs.getFloat(key, 0f)

                val currentRate = when (rate.currencyCode) {
                    "JPY(100)", "JPY" -> (rate.baseRate / 100.0).toFloat()
                    else -> rate.baseRate.toFloat()
                }

                putFloat(key, currentRate)
                putFloat("${key}_prev", previousRate)
            }

            putLong("last_update_time", System.currentTimeMillis())
        }

        CoroutineScope(Dispatchers.IO).launch {
            FavoriteExchangeRateWidget().updateAll(context)
        }
    }
    
    /**
     * 즐겨찾기가 변경되었을 때 위젯을 업데이트합니다.
     */
    fun updateWidgetOnFavoriteChange(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            FavoriteExchangeRateWidget().updateAll(context)
        }
    }
}