package io.github.domgew.kedis

import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.commands.list.ListCommands
import io.github.domgew.kedis.impl.DefaultKedisClient
import io.github.domgew.kedis.results.server.BgSaveResult
import io.github.domgew.kedis.results.server.InfoSection
import io.github.domgew.kedis.results.value.ExpireTimeResult
import io.github.domgew.kedis.results.value.SetBinaryResult
import io.github.domgew.kedis.results.value.SetResult
import io.github.domgew.kedis.results.value.TtlResult

/**
 * The public interface of the client. It contains all available commands. Use [KedisClient.newClient] to create an instance.
 * @see [KedisClient.newClient]
 */
@OptIn(ExperimentalStdlibApi::class)
public interface KedisClient : AutoCloseable,
                               ListCommands {
    public companion object {
        /**
         * Creates a new client instance without connecting.
         *
         * When you connect the client, make sure to disconnect it again. Each command (method) will connect automatically, when the connection is not already open.
         */
        public fun newClient(
            configuration: KedisConfiguration,
        ): KedisClient {
            return DefaultKedisClient(
                configuration = configuration,
            )
        }

        public operator fun invoke(
            configuration: KedisConfiguration,
        ): KedisClient =
            newClient(
                configuration = configuration,
            )
    }

    /**
     * Checks whether the client has a connection to the server and the connection reports to be active.
     */
    public val isConnected: Boolean

    /**
     * Manually ensures that the client is connected. When [isConnected] is true, nothing happens, otherwise the connection is established.
     */
    public suspend fun connect()

    /**
     * Closes the connection to the server.
     */
    public suspend fun closeSuspended()

    /**
     * Sends a message ([content]) to the server which should be returned unchanged (e.g. result should equal [content]).
     *
     * [https://redis.io/commands/ping/](https://redis.io/commands/ping/)
     * @return The response from the Redis server - should be [content]
     */
    public suspend fun ping(
        content: String = "PING",
    ): String

    /**
     * Authenticates the connection to the server or throws an exception when it failed.
     *
     * [https://redis.io/commands/auth/](https://redis.io/commands/auth/)
     */
    public suspend fun auth(
        password: String,
        username: String? = null,
    )

    /**
     * Asks the Redis server for the current username.
     *
     * [https://redis.io/commands/acl-whoami/](https://redis.io/commands/acl-whoami/)
     * @return The current username
     */
    public suspend fun whoAmI(): String

    /**
     * Queries the info for the requested [section]s from the Redis server in a strictly typed form.
     *
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information
     */
    public suspend fun info(
        vararg section: InfoSectionName,
    ): List<InfoSection>

    /**
     * Queries the info for the requested [section]s from the Redis server in string map form.
     *
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information - the first key is the lowercase section name, the second the actual field
     */
    public suspend fun infoMap(
        vararg section: InfoSectionName,
    ): Map<String?, Map<String, String>>

    /**
     * Queries the info for the requested [section]s from the Redis server in string form.
     *
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information
     */
    public suspend fun infoRaw(
        vararg section: InfoSectionName,
    ): String?

    /**
     * Clears all redis DBs.
     *
     * [https://redis.io/commands/flushall/](https://redis.io/commands/flushall/)
     * @return Whether the server responded with "OK"
     */
    public suspend fun flushAll(
        sync: SyncOption = SyncOption.SYNC,
    ): Boolean

    /**
     * Clears the current redis DB.
     *
     * [https://redis.io/commands/flushdb/](https://redis.io/commands/flushdb/)
     * @return Whether the server responded with "OK"
     */
    public suspend fun flushDb(
        sync: SyncOption = SyncOption.SYNC,
    ): Boolean

    /**
     * Saves the current DB to disk in the background. When [schedule], it will only be scheduled, otherwise it will be started immediately.
     *
     * [https://redis.io/commands/bgsave/](https://redis.io/commands/bgsave/)
     */
    public suspend fun bgSave(
        schedule: Boolean = false,
    ): BgSaveResult

    /**
     * Gets the value behind the given [key].
     *
     * [https://redis.io/commands/get/](https://redis.io/commands/get/)
     * @return The value or NULL
     */
    public suspend fun get(
        key: String,
    ): String?

    /**
     * Gets the value behind the given [key].
     *
     * [https://redis.io/commands/get/](https://redis.io/commands/get/)
     * @return The value or NULL
     */
    public suspend fun getBinary(
        key: String,
    ): ByteArray?

    /**
     * Gets part ([start]..[end] - both inclusive, clamped to real bounds) of the value behind the given [key]. The range parameters may also be negative to index from the end of the string. If the [key] does not exist, the result will be empty.
     *
     * [https://redis.io/commands/getrange/](https://redis.io/commands/getrange/)
     * @param start The inclusive start of the requested range - may be negative
     * @param end The inclusive end of the requested range - may be negative
     * @return The requested part ([start]..[end]) of the value behind the [key]
     * @see [getRange]
     */
    public suspend fun getRange(
        key: String,
        start: Long,
        end: Long,
    ): String

    /**
     * Gets part ([range] - clamped to real bounds) of the value behind the given [key]. If the [key] does not exist, the result will be empty.
     *
     * [https://redis.io/commands/getrange/](https://redis.io/commands/getrange/)
     * @return The requested part ([range]) of the value behind the [key]
     * @see [getRange]
     */
    public suspend fun getRange(
        key: String,
        range: LongRange,
    ): String =
        getRange(
            key = key,
            start = range.first,
            end = range.last,
        )

    /**
     * Sets the value behind the given [key], minding the [options].
     *
     * [https://redis.io/commands/set/](https://redis.io/commands/set/)
     * @return Whether the operation was successful and the previous value if requested
     */
    public suspend fun set(
        key: String,
        value: String,
        options: SetOptions = SetOptions(),
    ): SetResult

    /**
     * Sets the value behind the given [key], minding the [options].
     *
     * [https://redis.io/commands/set/](https://redis.io/commands/set/)
     * @return Whether the operation was successful and the previous value if requested
     */
    public suspend fun setBinary(
        key: String,
        value: ByteArray,
        options: SetOptions = SetOptions(),
    ): SetBinaryResult

    /**
     * Removes the provided [key]s. If a key does not exist, no error is thrown.
     *
     * [https://redis.io/commands/del/](https://redis.io/commands/del/)
     * @return The number of removed provided [key]s
     */
    public suspend fun del(
        vararg key: String,
    ): Long

    /**
     * Checks whether the given [key]s exist.
     *
     * [https://redis.io/commands/exists/](https://redis.io/commands/exists/)
     * @return The number of provided [key]s that do exist
     */
    public suspend fun exists(
        vararg key: String,
    ): Long

    /**
     * Gets the time in UNIX seconds or milliseconds - depending on the [inMilliseconds] argument - when the given [key] expires.
     *
     * Only available for redis >=7.0.0.
     *
     * [https://redis.io/commands/expiretime/](https://redis.io/commands/expiretime/)
     *
     * [https://redis.io/commands/pexpiretime/](https://redis.io/commands/pexpiretime/)
     * @param inMilliseconds Whether the resulting time should be in milliseconds or seconds
     * @return The time UNIX timestamp ([inMilliseconds]) of expiration
     */
    public suspend fun expireTime(
        key: String,
        inMilliseconds: Boolean = true,
    ): ExpireTimeResult

    /**
     * Gets the remaining time-to-live in seconds or milliseconds - depending on the [inMilliseconds] argument.
     *
     * [https://redis.io/commands/ttl/](https://redis.io/commands/ttl/)
     *
     * [https://redis.io/commands/pttl/](https://redis.io/commands/pttl/)
     * @param inMilliseconds Whether the resulting time should be in milliseconds or seconds
     * @return The remaining time-to-live (seconds or milliseconds)
     */
    public suspend fun ttl(
        key: String,
        inMilliseconds: Boolean = true,
    ): TtlResult

    /**
     * Appends the given [value] to the current value behind the given [key]. If the [key] does not exist yet, it will be created.
     *
     * [https://redis.io/commands/append/](https://redis.io/commands/append/)
     * @return The length of the value after appending
     */
    public suspend fun append(
        key: String,
        value: String,
    ): Long

    /**
     * Retrieves the string length of the value behind the given [key]. If the [key] does not exist, it will be 0.
     *
     * Only works on string values. It may or may not work on binary data.
     *
     * [https://redis.io/commands/strlen/](https://redis.io/commands/strlen/)
     * @return The length of the value
     */
    public suspend fun strLen(
        key: String,
    ): Long

    /**
     * Decrements the value behind the given [key] by one (1). If it does not exist at the beginning, it is assumed to be 0 before decrementing.
     *
     * [https://redis.io/commands/decr/](https://redis.io/commands/decr/)
     * @return The value after decrementing
     * @see decrBy
     * @see incr
     * @see incrBy
     * @see incrByFloat
     */
    public suspend fun decr(
        key: String,
    ): Long

    /**
     * Decrements the value behind the given [key] by [by]. If it does not exist at the beginning, it is assumed to be 0 before decrementing.
     *
     * [https://redis.io/commands/decrby/](https://redis.io/commands/decrby/)
     * @return The value after decrementing
     * @see decr
     * @see incr
     * @see incrBy
     * @see incrByFloat
     */
    public suspend fun decrBy(
        key: String,
        by: Long,
    ): Long

    /**
     * Increments the value behind the given [key] by one (1). If it does not exist at the beginning, it is assumed to be 0 before incrementing.
     *
     * [https://redis.io/commands/incr/](https://redis.io/commands/incr/)
     * @return The value after incrementing
     * @see incrBy
     * @see decr
     * @see decrBy
     * @see incrByFloat
     */
    public suspend fun incr(
        key: String,
    ): Long

    /**
     * Increments the value behind the given [key] by [by]. If it does not exist at the beginning, it is assumed to be 0 before incrementing.
     *
     * [https://redis.io/commands/incrby/](https://redis.io/commands/incrby/)
     * @return The value after incrementing
     * @see incr
     * @see decr
     * @see decrBy
     * @see incrByFloat
     */
    public suspend fun incrBy(
        key: String,
        by: Long,
    ): Long

    /**
     * Increments (or decrements when [by] is negative) the value behind the given [key] by [by]. If it does not exist at the beginning, it is assumed to be 0 before incrementing / decrementing.
     *
     * [https://redis.io/commands/incrbyfloat/](https://redis.io/commands/incrbyfloat/)
     * @return The value after incrementing / decrementing
     */
    public suspend fun incrByFloat(
        key: String,
        by: Double = 1.0,
    ): Double

    /**
     * Gets the value behind the given [field] in the [key] hash map.
     *
     * [https://redis.io/commands/hget/](https://redis.io/commands/hget/)
     * @return The value or NULL
     */
    public suspend fun hashGet(
        key: String,
        field: String,
    ): String?

    /**
     * Gets the value behind the given [field] in the [key] hash map.
     *
     * [https://redis.io/commands/hget/](https://redis.io/commands/hget/)
     * @return The value or NULL
     */
    public suspend fun hashGetBinary(
        key: String,
        field: String,
    ): ByteArray?

    /**
     * Gets the hash map behind the given [key].
     *
     * [https://redis.io/commands/hgetall/](https://redis.io/commands/hgetall/)
     * @return The map or NULL
     */
    public suspend fun hashGetAll(
        key: String,
    ): Map<String, String>?

    /**
     * Gets the hash map behind the given [key].
     *
     * [https://redis.io/commands/hgetall/](https://redis.io/commands/hgetall/)
     * @return The map or NULL
     */
    public suspend fun hashGetAllBinary(
        key: String,
    ): Map<String, ByteArray>?

    /**
     * Sets the given [fieldValues] on the hash map behind [key]. If the hash map does not exist, it is created.
     *
     * If the hash map already contains other field than those provided in [fieldValues], they are not removed. If the field is already present, it is overwritten.
     *
     * [https://redis.io/commands/hset/](https://redis.io/commands/hset/)
     * @return The number of fields that were added (not just set)
     */
    public suspend fun hashSet(
        key: String,
        fieldValues: Map<String, String>,
    ): Long

    /**
     * Sets the given [fieldValues] on the hash map behind [key]. If the hash map does not exist, it is created.
     *
     * If the hash map already contains other field than those provided in [fieldValues], they are not removed. If the field is already present, it is overwritten.
     *
     * [https://redis.io/commands/hset/](https://redis.io/commands/hset/)
     * @return The number of fields that were added (not just set)
     */
    public suspend fun hashSetBinary(
        key: String,
        fieldValues: Map<String, ByteArray>,
    ): Long

    /**
     * Removes the provided [field]s from the hash map behind [key]. If a [field] does not exist, no error is thrown.
     *
     * [https://redis.io/commands/hdel/](https://redis.io/commands/hdel/)
     * @return The number of removed provided [field]s
     */
    public suspend fun hashDel(
        key: String,
        vararg field: String,
    ): Long

    /**
     * Checks whether the given [field] exists on the hash map behind [key].
     *
     * [https://redis.io/commands/hexists/](https://redis.io/commands/hexists/)
     * @return The number of provided [key]s that do exist
     */
    public suspend fun hashExists(
        key: String,
        field: String,
    ): Boolean

    /**
     * Gets the fields of the hash map behind [key].
     *
     * [https://redis.io/commands/hkeys/](https://redis.io/commands/hkeys/)
     * @return The field names
     */
    public suspend fun hashKeys(
        key: String,
    ): List<String>?

    /**
     * Gets the number of fields of the hash map behind [key].
     *
     * [https://redis.io/commands/hlen/](https://redis.io/commands/hlen/)
     * @return The number of fields
     */
    public suspend fun hashLength(
        key: String,
    ): Long

    /**
     * Reconfigure the server at run time without the need to restart Redis.
     *
     * [https://redis.io/docs/latest/commands/config-set/]
     */
    public suspend fun configSet(
        parameter: Pair<String, String>,
        vararg parameters: Pair<String, String>
    ): String
}
