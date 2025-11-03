package net.ifmain.hwanultoktok.kmp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.ifmain.hwanultoktok.kmp.presentation.ui.ExchangeRateScreen
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.ExchangeRateViewModel
import net.ifmain.hwanultoktok.kmp.widget.WidgetUpdateHelper
import net.ifmain.hwanultoktok.kmp.util.getDataBaseDateWithoutHoliday
import net.ifmain.hwanultoktok.kmp.util.getCurrentDateTime
import org.koin.androidx.compose.koinViewModel

/**
 *
 * @author gayoung.
 * @since 2025. 8. 14.
 *
 * Android 플랫폼에서 ExchangeRateScreen을 사용할 때
 * 위젯 업데이트를 자동으로 연동하는 Composable
 */
@Composable
fun ExchangeRateScreenWithWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: ExchangeRateViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.exchangeRates) {
        if (uiState.exchangeRates.isNotEmpty()) {
            val updateTime = uiState.exchangeRates.firstOrNull()?.timestamp ?: getCurrentDateTime()
            val dataDate = getDataBaseDateWithoutHoliday(updateTime)
            WidgetUpdateHelper.saveExchangeRatesAndUpdateWidget(
                context,
                uiState.exchangeRates,
                dataDate
            )
        }
    }

    LaunchedEffect(uiState.favoriteIds) {
        WidgetUpdateHelper.updateWidgetOnFavoriteChange(context)
    }

    ExchangeRateScreen(modifier = modifier)
}
