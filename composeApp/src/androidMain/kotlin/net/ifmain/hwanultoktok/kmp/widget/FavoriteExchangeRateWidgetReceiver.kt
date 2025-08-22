package net.ifmain.hwanultoktok.kmp.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.ifmain.hwanultoktok.kmp.BuildConfig
import net.ifmain.hwanultoktok.kmp.data.mapper.toDomain
import net.ifmain.hwanultoktok.kmp.data.remote.KoreaExImBankApi
import net.ifmain.hwanultoktok.kmp.util.getCurrentDateTime
import net.ifmain.hwanultoktok.kmp.util.getDataBaseDateWithoutHoliday

/**
 *
 * @author gayoung.
 * @since 2025. 8. 14.
 */
class FavoriteExchangeRateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavoriteExchangeRateWidget()

    companion object {
        const val ACTION_REFRESH = "ACTION_REFRESH_WIDGET"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH) {
            refreshWidgetData(context)
        }
    }

    private fun refreshWidgetData(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val httpClient = HttpClient {
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        })
                    }
                }

                val api = KoreaExImBankApi(httpClient)
                val apiKey = BuildConfig.KOREAEXIM_API_KEY

                val now = getCurrentDateTime()
                val dataDate = getDataBaseDateWithoutHoliday(now)
                val searchDate = dataDate.toString().replace("-", "")

                val response = api.getExchangeRates(apiKey, searchDate)
                val exchangeRates = response.map { it.toDomain() }

                WidgetUpdateHelper.saveExchangeRatesAndUpdateWidget(context, exchangeRates)

            } catch (e: Exception) {
                e.printStackTrace()
                // 실패해도 위젯 UI는 업데이트
                FavoriteExchangeRateWidget().updateAll(context)
            }
        }
    }
}