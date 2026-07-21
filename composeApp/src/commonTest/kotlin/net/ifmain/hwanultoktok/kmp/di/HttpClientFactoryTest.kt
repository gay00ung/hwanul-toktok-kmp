package net.ifmain.hwanultoktok.kmp.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HttpClientFactoryTest {
    @Test
    fun logging_plugin_is_not_installed_when_network_logging_is_disabled() {
        val client = createHttpClient(
            platformClient = HttpClient(MockEngine { respondOk() }),
            enableNetworkLogging = false,
        )

        try {
            assertNull(client.pluginOrNull(Logging))
        } finally {
            client.close()
        }
    }

    @Test
    fun logging_plugin_is_installed_when_network_logging_is_enabled() {
        val client = createHttpClient(
            platformClient = HttpClient(MockEngine { respondOk() }),
            enableNetworkLogging = true,
        )

        try {
            assertNotNull(client.pluginOrNull(Logging))
        } finally {
            client.close()
        }
    }
}
