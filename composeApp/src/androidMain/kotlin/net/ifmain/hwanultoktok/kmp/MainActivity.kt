package net.ifmain.hwanultoktok.kmp

import android.Manifest
import android.app.Application
import android.content.Intent
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
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ifmain.hwanultoktok.kmp.di.commonModule
import net.ifmain.hwanultoktok.kmp.di.platformModule
import net.ifmain.hwanultoktok.kmp.domain.usecase.ScheduleExchangeRateCheckUseCase
import net.ifmain.hwanultoktok.kmp.presentation.AppWithBottomAd
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class HwanulTokTokApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HwanulTokTokApplication)
            modules(commonModule, platformModule)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val scheduleUseCase: ScheduleExchangeRateCheckUseCase by inject()
             scheduleUseCase(11, 30)
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

        try {
            setContent {
                AppWithAds()
            }
        } catch (e: Exception) {
            // DI 초기화 에러 발생 시 앱 재시작
            e.printStackTrace()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
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