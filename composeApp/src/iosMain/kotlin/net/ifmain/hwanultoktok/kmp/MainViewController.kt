package net.ifmain.hwanultoktok.kmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { 
    App(modifier = Modifier.fillMaxSize().systemBarsPadding())
}