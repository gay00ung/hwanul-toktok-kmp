package net.ifmain.hwanultoktok.kmp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.ifmain.hwanultoktok.kmp.presentation.state.ExchangeRateUiState
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.ExchangeRateViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreen(
    modifier: Modifier = Modifier,
    viewModel: ExchangeRateViewModel = koinInject(),
    onFavoriteClick: (String) -> Unit = { currencyCode ->
        viewModel.toggleFavorite(currencyCode)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.initialize()
    }

    ExchangeRateScreen(
        uiState = uiState,
        onRefreshClick = viewModel::refreshExchangeRates,
        onRetryClick = viewModel::retryExchangeRates,
        onClearErrorClick = viewModel::clearError,
        onToggleFavoritesFilter = viewModel::toggleFavoritesFilter,
        onFavoriteClick = onFavoriteClick,
        modifier = modifier,
    )
}

@Composable
internal fun ExchangeRateScreen(
    uiState: ExchangeRateUiState,
    onRefreshClick: () -> Unit,
    onRetryClick: () -> Unit,
    onClearErrorClick: () -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (uiState.errorMessage != null && uiState.exchangeRates.isNotEmpty()) {
                ExchangeRateErrorCard(
                    message = uiState.errorMessage,
                    retryEnabled = uiState.canRefresh,
                    onRetryClick = onRetryClick,
                    onDismissClick = onClearErrorClick,
                )
            }

            // Header with refresh button and favorites toggle
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "실시간 환율",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp),
                        )

                        uiState.formattedDataDate?.let { formattedDate ->
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        enabled = uiState.canRefresh,
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "새로고침",
                        )
                    }
                }

                // Favorites filter toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "즐겨찾기만 보기",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Switch(
                        checked = uiState.showFavoritesOnly,
                        onCheckedChange = { onToggleFavoritesFilter() },
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingStatus(message = "환율 정보를 불러오는 중이에요")
                    }
                }

                uiState.errorMessage != null -> {
                    ExchangeRateErrorContent(
                        message = uiState.errorMessage,
                        retryEnabled = uiState.canRefresh,
                        onRetryClick = onRetryClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                uiState.filteredExchangeRates.isEmpty() -> {
                    EmptyExchangeRateContent(
                        retryEnabled = uiState.canRefresh,
                        onRetryClick = onRetryClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 4.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.filteredExchangeRates) { exchangeRate ->
                            SimpleExchangeRateCard(
                                exchangeRate = exchangeRate,
                                isFavorite = uiState.favoriteIds.contains(exchangeRate.currencyCode),
                                onFavoriteClick = {
                                    onFavoriteClick(exchangeRate.currencyCode)
                                },
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isRefreshing) {
            RefreshLoadingOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ExchangeRateErrorCard(
    message: String,
    retryEnabled: Boolean,
    onRetryClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "환율 정보를 업데이트하지 못했어요",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onRetryClick,
                    enabled = retryEnabled,
                ) {
                    Text("다시 시도")
                }
                TextButton(
                    onClick = onDismissClick,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text("닫기")
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateErrorContent(
    message: String,
    retryEnabled: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "환율 정보를 불러오지 못했어요",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = onRetryClick,
            enabled = retryEnabled,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("다시 시도")
        }
    }
}

@Composable
private fun EmptyExchangeRateContent(
    retryEnabled: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "표시할 환율 정보가 없습니다",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetryClick,
            enabled = retryEnabled,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("다시 시도")
        }
    }
}

@Composable
private fun LoadingStatus(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(44.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun RefreshLoadingOverlay(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                            .changes
                            .forEach { it.consume() }
                    }
                }
            }
            .semantics {
                liveRegion = LiveRegionMode.Polite
        },
        contentAlignment = Alignment.Center,
    ) {
        LoadingStatus(
            message = "최신 환율로 새로고침 중이에요",
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}
