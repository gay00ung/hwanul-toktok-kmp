package net.ifmain.hwanultoktok.kmp.presentation.ads

import androidx.compose.runtime.Composable

interface InterstitialAdController {
    fun preload()
    fun show(onFinished: () -> Unit)
}

@Composable
expect fun rememberInterstitialAdController(): InterstitialAdController?
