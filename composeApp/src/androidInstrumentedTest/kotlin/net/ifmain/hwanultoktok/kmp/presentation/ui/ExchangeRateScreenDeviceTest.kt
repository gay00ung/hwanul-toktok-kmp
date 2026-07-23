package net.ifmain.hwanultoktok.kmp.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.ifmain.hwanultoktok.kmp.presentation.state.ExchangeRateUiState
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

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
}
