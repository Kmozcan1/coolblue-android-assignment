package com.coolblue.assignment.core.domain.result

/** Success or failure of a repository call. Used instead of exceptions so every caller handles both branches. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
