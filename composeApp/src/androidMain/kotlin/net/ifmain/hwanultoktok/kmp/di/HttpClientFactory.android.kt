package net.ifmain.hwanultoktok.kmp.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Android)
}