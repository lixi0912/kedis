package com.github.domgew.kredis

import com.github.domgew.kredis.arguments.SyncOptions
import com.github.domgew.kredis.utils.TestConfigUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class SimpleE2ETest {
    @Test
    fun ping() = runTest {
        withContext(Dispatchers.Default) {
            val pingContent = "_TEST_"

            val client = KredisClient.newClient(
                KredisConfiguration(
                    host = "127.0.0.1",
                    port = TestConfigUtil.getPort(),
                    connectionTimeoutMillis = 2_000L,
                ),
            )
            val pongMessage = client.ping(pingContent)

            assertEquals(pingContent, pongMessage)
        }
    }

    @Test
    fun flushGetSetGet() = runTest {
        withContext(Dispatchers.Default) {
            val testKey1 = "test1"
            val testValue = "testValue1"
            val testKey2 = "test2"

            val client = KredisClient.newClient(
                KredisConfiguration(
                    host = "127.0.0.1",
                    port = TestConfigUtil.getPort(),
                    connectionTimeoutMillis = 2_000L,
                ),
            )

            client.flushAll(sync = SyncOptions.SYNC)
            assertNull(client.get(testKey1))
            assertNull(client.get(testKey2))
            client.set(testKey1, testValue)
            assertEquals(testValue, client.get(testKey1))
            assertNull(client.get(testKey2))
        }
    }
}
