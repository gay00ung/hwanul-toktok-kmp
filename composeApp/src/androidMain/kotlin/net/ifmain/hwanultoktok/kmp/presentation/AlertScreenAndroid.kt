package net.ifmain.hwanultoktok.kmp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.ifmain.hwanultoktok.kmp.presentation.ui.AlertScreen
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.AlertViewModel
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.ExchangeRateViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AlertScreenWithViewModels(modifier: Modifier = Modifier) {
    val alertViewModel: AlertViewModel = koinViewModel()
    val exchangeRateViewModel: ExchangeRateViewModel = koinViewModel()

    AlertScreen(
        modifier = modifier,
        viewModel = alertViewModel,
        exchangeRateViewModel = exchangeRateViewModel,
    )
}
