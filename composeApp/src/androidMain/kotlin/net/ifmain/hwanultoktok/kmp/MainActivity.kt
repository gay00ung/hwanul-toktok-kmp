package net.ifmain.hwanultoktok.kmp

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import net.ifmain.hwanultoktok.kmp.di.commonModule
import net.ifmain.hwanultoktok.kmp.di.platformModule
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ifmain.hwanultoktok.kmp.presentation.AppWithBottomAd
import net.ifmain.hwanultoktok.kmp.domain.usecase.ScheduleExchangeRateCheckUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HwanulTokTokApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HwanulTokTokApplication)
            modules(commonModule, platformModule)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val scheduleUseCase: ScheduleExchangeRateCheckUseCase by inject()
            scheduleUseCase(15)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // AdMob 초기화
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
        }
        
        // Android 13+ 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        setContent {
            AppWithAds()
        }
    }
}

@Composable
fun AppWithAds() {
    AppWithBottomAd()
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}