package net.ifmain.hwanultoktok.kmp.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual val isNetworkLoggingEnabled: Boolean = false

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Darwin)
}
