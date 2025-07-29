package net.ifmain.hwanultoktok.kmp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.presentation.theme.HwanulTheme
import net.ifmain.hwanultoktok.kmp.presentation.ui.AlertScreen
import net.ifmain.hwanultoktok.kmp.presentation.ui.ExchangeRateScreen
import net.ifmain.hwanultoktok.kmp.di.commonModule
import net.ifmain.hwanultoktok.kmp.di.platformModule
import net.ifmain.hwanultoktok.kmp.ui.AdMobBanner
import net.ifmain.hwanultoktok.kmp.platform.ApiKeyProvider
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun AppWithAds(modifier: Modifier) {
    HwanulTheme {
        KoinApplication(application = {
            modules(commonModule, platformModule)
        }) {
            val notificationService: NotificationService = koinInject()
            
            // 앱 시작 시 알림 권한 요청
            LaunchedEffect(Unit) {
                try {
                    notificationService.requestNotificationPermission()
                } catch (e: Exception) {
                    // 권한 요청 실패 시 무시
                }
            }
            
            var selectedTabIndex by remember { mutableStateOf(0) }
            
            Scaffold(
                modifier = modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
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
                    Column {
                        AdMobBanner(
                            modifier = Modifier.fillMaxWidth(),
                            adUnitId = ApiKeyProvider.getAdMobBannerId()
                        )
                        
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
}