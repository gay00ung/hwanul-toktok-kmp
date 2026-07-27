package net.ifmain.hwanultoktok.kmp.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.ifmain.hwanultoktok.kmp.presentation.state.ExchangeRateUiState
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.EXCHANGE_RATE_LOAD_ERROR_MESSAGE
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ExchangeRateScreenDeviceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun refreshingStateShowsBlockingStatusAndDisablesRefresh() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = ExchangeRateUiState(isRefreshing = true),
                    onRefreshClick = {},
                    onRetryClick = {},
                    onClearErrorClick = {},
                    onToggleFavoritesFilter = {},
                    onFavoriteClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("최신 환율로 새로고침 중이에요")
            .assertIsDisplayed()
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("새로고침")
            .assertIsNotEnabled()
    }

    @Test
    fun initialLoadErrorShowsRetryActionAndInvokesCallback() {
        var retryCount = 0

        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = ExchangeRateUiState(
                        errorMessage = EXCHANGE_RATE_LOAD_ERROR_MESSAGE,
                    ),
                    onRefreshClick = {},
                    onRetryClick = { retryCount += 1 },
                    onClearErrorClick = {},
                    onToggleFavoritesFilter = {},
                    onFavoriteClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("환율 정보를 불러오지 못했어요")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(EXCHANGE_RATE_LOAD_ERROR_MESSAGE)
            .assertIsDisplayed()
        composeTestRule
            .onAllNodesWithText("환율 정보를 불러오는 중이에요")
            .assertCountEquals(0)
        composeTestRule
            .onNodeWithText("다시 시도")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, retryCount)
        }
    }
}
