package io.github.domgew.kedis

import io.github.domgew.kedis.utils.TestConfigUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class ConfigTest {

    @Test
    fun testConfigSetShouldOk() = runTest {
        withContext(Dispatchers.Default) {
            val client = KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeoutMillis = 2_000L,
                ),
            )

            try {
                client.connect()
                val result = client.configSet("notify-keyspace-events" to "KEA")
                assertEquals("OK", result)
            } finally {
                client.close()
            }
        }
    }
}
