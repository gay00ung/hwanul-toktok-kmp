package net.ifmain.hwanultoktok.kmp.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(): HttpClient
expect val isNetworkLoggingEnabled: Boolean

internal const val HTTP_CONNECT_TIMEOUT_MILLIS = 10_000L
internal const val HTTP_REQUEST_TIMEOUT_MILLIS = 15_000L
internal const val HTTP_SOCKET_TIMEOUT_MILLIS = 15_000L

fun createHttpClient(
    platformClient: HttpClient = createPlatformHttpClient(),
    enableNetworkLogging: Boolean = isNetworkLoggingEnabled,
): HttpClient {
    return platformClient.config {
        install(HttpTimeout) {
            connectTimeoutMillis = HTTP_CONNECT_TIMEOUT_MILLIS
            requestTimeoutMillis = HTTP_REQUEST_TIMEOUT_MILLIS
            socketTimeoutMillis = HTTP_SOCKET_TIMEOUT_MILLIS
        }

        install(ContentNegotiation) {
            json(Json {
                coerceInputValues = true
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
        
        if (enableNetworkLogging) {
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.BODY
            }
        }
    }
}
