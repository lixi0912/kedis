package io.github.domgew.kedis.commands.config

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/config/
internal class ConfigSetCommand(
    private val parameter: Pair<String, String>,
    private vararg val parameters: Pair<String, String>
) : KedisFullCommand<String> {

    override fun fromRedisResponse(response: RedisMessage): String {
        return when {
            response is RedisMessage.StringMessage && response.value == "OK" -> response.value
            response is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string or null response, was ${response::class.simpleName}",
                )
        }
    }

    override fun toRedisRequest(): RedisMessage = RedisMessage.ArrayMessage(
        value = listOfNotNull(
            RedisMessage.BulkStringMessage(OPERATION_NAME),
            RedisMessage.BulkStringMessage("SET"),
            RedisMessage.BulkStringMessage(parameter.first),
            RedisMessage.BulkStringMessage(parameter.second),
        ) + parameters.flatMap {
            listOf(RedisMessage.BulkStringMessage(it.first), RedisMessage.BulkStringMessage(it.second))
        },
    )

    companion object {
        private const val OPERATION_NAME = "CONFIG"
    }
}
