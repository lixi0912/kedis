package io.github.domgew.kedis.commands.list

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

public interface ListCommands {

    /**
     * Insert all the specified values at the head of the list stored at key.
     * If key does not exist, it is created as empty list before performing the push operations.
     * When key holds a value that is not a list, an error is returned.
     *
     * [https://redis.io/docs/latest/commands/lpush/]
     */
    public suspend fun lpush(
        key: String,
        value: String,
        vararg values: String
    ): Int

    /**
     * Inserts specified values at the head of the list stored at key, only if key already exists and holds a list.
     * In contrary to [lpush], no operation will be performed when key does not yet exist.
     *
     * [https://redis.io/docs/latest/commands/lpushx/]
     */
    public suspend fun lpushx(
        key: String,
        value: String,
        vararg values: String
    ): Int

    /**
     *
     * [https://redis.io/docs/latest/commands/rpush/]
     */
    public suspend fun rpush(
        key: String,
        value: String,
        vararg values: String
    ): Int

    /**
     *
     * [https://redis.io/docs/latest/commands/rpushx/]
     */
    public suspend fun rpushx(
        key: String,
        value: String,
        vararg values: String
    ): Int

    /**
     *
     * [https://redis.io/docs/latest/commands/llen/]
     */
    public suspend fun llen(
        key: String
    ): Int

    /**
     *
     * [https://redis.io/docs/latest/commands/lpop/]
     */
    public suspend fun lpop(
        key: String
    ): String?

    /**
     *
     * [https://redis.io/docs/latest/commands/rpop/]
     */
    public suspend fun rpop(
        key: String
    ): String?
}


internal class LLenCommands(
    val key: String
) : KedisFullCommand<Int> {

    override fun fromRedisResponse(response: RedisMessage): Int = when (response) {
        is RedisMessage.IntegerMessage ->
            response.value.toInt()

        is RedisMessage.ErrorMessage ->
            handleRedisErrorResponse(
                response = response,
            )

        else ->
            throw KedisException.WrongResponseException(
                message = "Expected Int response, was ${response::class.simpleName}",
            )
    }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage("LLEN"),
                RedisMessage.BulkStringMessage(key),
            ),
        )
}
