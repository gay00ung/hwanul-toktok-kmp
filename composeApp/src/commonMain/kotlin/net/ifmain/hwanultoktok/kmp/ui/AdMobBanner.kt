package net.ifmain.hwanultoktok.kmp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String
)