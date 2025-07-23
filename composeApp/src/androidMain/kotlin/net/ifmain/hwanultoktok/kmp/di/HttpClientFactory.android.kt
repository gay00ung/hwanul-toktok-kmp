package net.ifmain.hwanultoktok.kmp.di

import android.annotation.SuppressLint
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HttpsURLConnection

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Android)
}