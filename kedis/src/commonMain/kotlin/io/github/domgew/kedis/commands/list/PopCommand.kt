package io.github.domgew.kedis.commands.list

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class PopCommand private constructor(
    val operation: Operation,
    val key: String
) : KedisFullCommand<String?> {

    override fun fromRedisResponse(response: RedisMessage): String? = when (response) {
        is RedisMessage.StringMessage ->
            response.value

        is RedisMessage.NullMessage ->
            null

        is RedisMessage.ErrorMessage ->
            handleRedisErrorResponse(
                response = response,
            )

        else ->
            throw KedisException.WrongResponseException(
                message = "Expected String response, was ${response::class.simpleName}",
            )
    }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(operation.name),
                RedisMessage.BulkStringMessage(key),
            ),
        )

    enum class Operation {

        LPOP, RPOP;

        fun toCommand(
            key: String
        ) = PopCommand(
            this,
            key,
        )
    }
}
