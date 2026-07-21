package net.ifmain.hwanultoktok.kmp.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import net.ifmain.hwanultoktok.kmp.BuildConfig

actual val isNetworkLoggingEnabled: Boolean = BuildConfig.DEBUG

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Android)
}
