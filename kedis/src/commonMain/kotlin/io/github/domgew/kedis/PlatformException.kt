package io.github.domgew.kedis

internal expect suspend fun <T> commoniseConnectException(
    block: suspend () -> T,
): T
