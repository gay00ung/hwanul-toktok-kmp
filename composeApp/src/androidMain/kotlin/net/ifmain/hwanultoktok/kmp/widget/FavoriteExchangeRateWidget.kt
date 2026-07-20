package net.ifmain.hwanultoktok.kmp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ifmain.hwanultoktok.kmp.MainActivity
import net.ifmain.hwanultoktok.kmp.database.HwanulDatabase
import net.ifmain.hwanultoktok.kmp.di.DatabaseDriverFactory
import net.ifmain.hwanultoktok.kmp.util.CurrencyUtils

/**
 *
 * @author gayoung.
 * @since 2025. 8. 14.
 */
class FavoriteExchangeRateWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val contentState = loadFavoriteExchangeRates(context)

        provideContent {
            GlanceTheme {
                FavoriteExchangeRateContent(
                    context = context,
                    contentState = contentState,
                )
            }
        }
    }

    override val sizeMode = SizeMode.Exact
}

@SuppressLint("DefaultLocale")
@Composable
fun FavoriteExchangeRateContent(
    context: Context,
    contentState: WidgetContentState,
) {
    val prefs = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)

    // 실제 데이터 날짜 사용 (앱과 동기화)
    val actualDataDate = prefs.getString("actual_data_date", null)
    val updateTimeText = if (actualDataDate != null) {
        try {
            val parts = actualDataDate.split("-")
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            "${year}년 ${month}월 ${day}일 고시환율"
        } catch (e: Exception) {
            "-- 고시환율"
        }
    } else {
        "-- 고시환율"
    }

    val favoriteRates = contentState.rates

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(20.dp)
            .padding(0.dp)
            .clickable(actionStartActivity(createAppIntent(context)))
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.primary)
                .cornerRadius(16.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    Text(
                        text = "💰 환율 톡톡",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onPrimary
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Text(
                        text = updateTimeText,
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimary
                        )
                    )
                }

                Text(
                    text = "⟳",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier
                        .size(32.dp)
                        .clickable(actionRunCallback<RefreshWidgetCallback>())
                        .padding(4.dp)
                )
            }
        }

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            favoriteRates.forEachIndexed { index, rate ->
                ExchangeRateCard(rate)
                if (index < favoriteRates.size - 1) {
                    Spacer(modifier = GlanceModifier.height(12.dp))
                }
            }

            if (contentState.loadFailed) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "환율 정보를 불러올 수 없습니다",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "잠시 후 새로고침해 주세요",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            } else if (favoriteRates.isEmpty()) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊",
                        style = TextStyle()
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "즐겨찾기한 환율이 없습니다",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "앱에서 환율을 즐겨찾기에 추가해보세요",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ExchangeRateCard(data: ExchangeRateData) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(12.dp)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = CurrencyUtils.getCurrencyEmoji(data.currencyCode) + " " + data.currencyUnit,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                Text(
                    text = CurrencyUtils.getCurrencyName(data.currencyCode),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = data.rate,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                if (data.change.isNotEmpty()) {
                    Text(
                        text = data.change,
                        style = TextStyle(
                            color = if (data.change.startsWith("▲"))
                                GlanceTheme.colors.error
                            else
                                GlanceTheme.colors.primary
                        )
                    )
                }
            }
        }
    }
}

data class ExchangeRateData(
    val currencyCode: String,
    val currencyUnit: String,
    val rate: String,
    val change: String
)

data class WidgetContentState(
    val rates: List<ExchangeRateData>,
    val loadFailed: Boolean = false,
)

@SuppressLint("DefaultLocale")
private suspend fun loadFavoriteExchangeRates(context: Context): WidgetContentState =
    withContext(Dispatchers.IO) {
        val driver = DatabaseDriverFactory(context).createDriver()
        val database = HwanulDatabase(driver)
        val prefs = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)

        try {
            val favorites = database.exchangeRateAlertQueries.getAllFavorites().executeAsList()
            val rates = favorites.take(3).map { favorite ->
                val key = "${favorite.fromCurrencyCode}_${favorite.toCurrencyCode}"
                val rate = prefs.getFloat(key, 0f)
                val previousRate = prefs.getFloat("${key}_prev", 0f)
                val currencyUnit = prefs.getString("${key}_unit", null)
                    ?: CurrencyUtils.getOfficialCurrencyUnit(favorite.toCurrencyCode)

                val change = when {
                    previousRate == 0f -> ""
                    rate > previousRate -> "▲ ${String.format("%.2f", rate - previousRate)}"
                    rate < previousRate -> "▼ ${String.format("%.2f", previousRate - rate)}"
                    else -> "- 0.00"
                }

                ExchangeRateData(
                    currencyCode = favorite.toCurrencyCode,
                    currencyUnit = currencyUnit,
                    rate = if (rate > 0) String.format("%.2f", rate) else "---",
                    change = change,
                )
            }

            WidgetContentState(rates = rates)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            WidgetContentState(
                rates = emptyList(),
                loadFailed = true,
            )
        } finally {
            driver.close()
        }
    }

/**
 * 앱을 안전하게 시작하기 위한 Intent 생성
 */
private fun createAppIntent(context: Context): Intent {
    return Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("from_widget", true)
    }
}

/**
 * 위젯 새로고침 콜백
 */
class RefreshWidgetCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, FavoriteExchangeRateWidgetReceiver::class.java).apply {
            action = FavoriteExchangeRateWidgetReceiver.ACTION_REFRESH
        }
        context.sendBroadcast(intent)
    }
}
