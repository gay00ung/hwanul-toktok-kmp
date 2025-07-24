package net.ifmain.hwanultoktok.kmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.ifmain.hwanultoktok.kmp.di.commonModule
import net.ifmain.hwanultoktok.kmp.di.platformModule
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.presentation.theme.HwanulTheme
import net.ifmain.hwanultoktok.kmp.presentation.ui.AlertScreen
import net.ifmain.hwanultoktok.kmp.presentation.ui.ExchangeRateScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    HwanulTheme {
        KoinApplication(application = {
            modules(commonModule, platformModule)
        }) {
            HwanulTokTokApp(modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HwanulTokTokApp(modifier: Modifier = Modifier) {
    val notificationService: NotificationService = koinInject()

    // 앱 시작 시 알림 권한 요청
    LaunchedEffect(Unit) {
        try {
            notificationService.requestNotificationPermission()
        } catch (e: Exception) {
            // 권한 요청 실패 시 무시
        }
    }

    MaterialTheme {
        var selectedTabIndex by remember { mutableStateOf(0) }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "환율 톡톡",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.height(56.dp)
                ) {
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