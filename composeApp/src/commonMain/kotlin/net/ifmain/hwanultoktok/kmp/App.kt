package net.ifmain.hwanultoktok.kmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import net.ifmain.hwanultoktok.kmp.di.commonModule
import net.ifmain.hwanultoktok.kmp.di.platformModule
import net.ifmain.hwanultoktok.kmp.presentation.ui.ExchangeRateScreen
import net.ifmain.hwanultoktok.kmp.presentation.ui.AlertScreen

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    KoinApplication(application = {
        modules(commonModule, platformModule)
    }) {
        HwanulTokTokApp(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HwanulTokTokApp(modifier: Modifier = Modifier) {
    MaterialTheme {
        var selectedTabIndex by remember { mutableStateOf(0) }
        
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("환율 톡톡") }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        icon = { Icon(Icons.Default.AttachMoney, contentDescription = "환율") },
                        label = { Text("환율") }
                    )
                    NavigationBarItem(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "알림") },
                        label = { Text("알림") }
                    )
                }
            }
        ) { paddingValues ->
            when (selectedTabIndex) {
                0 -> ExchangeRateScreen(
                    modifier = Modifier.padding(paddingValues)
                )
                1 -> AlertScreen(
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}