package com.coolblue.assignment.search.ui.mapper

import androidx.annotation.StringRes
import com.coolblue.assignment.search.domain.result.AppError
import com.coolblue.assignment.search.ui.R

@StringRes
internal fun AppError.messageRes(): Int = when (this) {
    AppError.Network -> R.string.error_network
    is AppError.Server -> R.string.error_server
    AppError.Unknown -> R.string.error_unknown
}
