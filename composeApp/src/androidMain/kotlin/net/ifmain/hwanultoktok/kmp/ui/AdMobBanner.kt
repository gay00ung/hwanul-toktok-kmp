package net.ifmain.hwanultoktok.kmp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.ifmain.hwanultoktok.kmp.presentation.components.AdBanner

@Composable
actual fun AdMobBanner(
    modifier: Modifier,
    adUnitId: String
) {
    AdBanner(modifier = modifier)
}