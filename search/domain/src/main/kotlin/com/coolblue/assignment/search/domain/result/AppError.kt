package com.coolblue.assignment.search.domain.result

/** Why a call failed, in terms the UI can act on. Never carries the exception: it would break equality. */
sealed interface AppError {
    /** No connection, DNS failure, timeout. */
    data object Network : AppError

    /** The server answered with a non-2xx status. */
    data class Server(val code: Int) : AppError

    /** Anything else, e.g. a response we could not parse. */
    data object Unknown : AppError
}
