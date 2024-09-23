package io.github.domgew.kedis.commands.list

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class PushCommand private constructor(
    val operation: Operation,
    val key: String,
    val value: String,
    vararg val values: String
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
                RedisMessage.BulkStringMessage(operation.name),
                RedisMessage.BulkStringMessage(key),
                RedisMessage.BulkStringMessage(value),
            ) + values.map {
                RedisMessage.BulkStringMessage(it)
            },
        )

    enum class Operation {

        LPUSH, LPUSHX, RPUSH, RPUSHX;

        fun toCommand(
            key: String,
            value: String,
            vararg values: String
        ) = PushCommand(
            this,
            key,
            value,
            *values,
        )
    }
}
