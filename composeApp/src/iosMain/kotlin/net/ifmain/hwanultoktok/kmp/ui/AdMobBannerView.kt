package net.ifmain.hwanultoktok.kmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// For iOS, we'll use a placeholder box since we cannot directly access Google Mobile Ads from Kotlin/Native
// The actual ad implementation needs to be done in native iOS code
@Composable
actual fun AdMobBanner(
    modifier: Modifier,
    adUnitId: String
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surface)
    )
}