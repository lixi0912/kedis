package io.github.domgew.kedis

import io.github.domgew.kedis.utils.TestConfigUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class ListTest {

    @Test
    fun testLPush() = runTest {
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
                val result = client.lpush("test-list", "a")
                assertEquals(1, result)
                val result2 = client.lpush("test-list", "b")
                assertEquals(2, result2)
            } finally {
                client.flushAll()
                client.close()
            }
        }
    }

    @Test
    fun testLPushAndLPop() = runTest {
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
                val result = client.lpush("test-list", "a", "b")
                assertEquals(2, result)
                val result2 = client.lpop("test-list")
                assertEquals("b", result2)
            } finally {
                client.flushAll()
                client.close()
            }
        }
    }

    @Test
    fun testLPushAndRPop() = runTest {
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
                val result = client.lpush("test-list", "a", "b")
                assertEquals(2, result)
                val result2 = client.rpop("test-list")
                assertEquals("a", result2)
            } finally {
                client.flushAll()
                client.close()
            }
        }
    }


    @Test
    fun testRPushAndLPop() = runTest {
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
                val result = client.rpush("test-list", "a", "b")
                assertEquals(2, result)
                val result2 = client.lpop("test-list")
                assertEquals("a", result2)
            } finally {
                client.flushAll()
                client.close()
            }
        }
    }

    @Test
    fun testRPushAndRPop() = runTest {
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
                val result = client.rpush("test-list", "a", "b")
                assertEquals(2, result)
                val result2 = client.rpop("test-list")
                assertEquals("b", result2)
            } finally {
                client.flushAll()
                client.close()
            }
        }
    }

    @Test
    fun testLLen() = runTest {
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
                val result = client.rpush("test-list", "a", "b")
                assertEquals(2, result)
                val result2 = client.llen("test-list")
                assertEquals(2, result2)
            } finally {
                client.flushAll()
                client.close()
            }
        }
    }
}
