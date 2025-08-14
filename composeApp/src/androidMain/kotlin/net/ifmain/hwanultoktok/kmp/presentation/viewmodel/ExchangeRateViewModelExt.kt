package net.ifmain.hwanultoktok.kmp.presentation.viewmodel

import android.content.Context
import net.ifmain.hwanultoktok.kmp.widget.WidgetUpdateHelper

/**
 *
 * @author gayoung.
 * @since 2025. 8. 14.
 *
 * Android 플랫폼별 ViewModel 확장
 *
 * MainActivity나 Application 클래스에서 ViewModel을 초기화할 때
 * 이 함수를 사용하여 위젯 업데이트를 연동합니다.
 */
fun ExchangeRateViewModel.setupWidgetUpdate(context: Context) {
    uiState.value.exchangeRates.let { rates ->
        if (rates.isNotEmpty()) {
            WidgetUpdateHelper.saveExchangeRatesAndUpdateWidget(context, rates)
        }
    }
}

/**
 * 환율 데이터를 새로고침하고 위젯도 함께 업데이트합니다.
 */
fun ExchangeRateViewModel.refreshWithWidget(context: Context) {
    refreshExchangeRates()

    uiState.value.exchangeRates.let { rates ->
        if (rates.isNotEmpty()) {
            WidgetUpdateHelper.saveExchangeRatesAndUpdateWidget(context, rates)
        }
    }
}

/**
 * 즐겨찾기를 토글하고 위젯도 함께 업데이트합니다.
 */
fun ExchangeRateViewModel.toggleFavoriteWithWidget(
    context: Context,
    currencyCode: String
) {
    toggleFavorite(currencyCode)

    WidgetUpdateHelper.updateWidgetOnFavoriteChange(context)
}