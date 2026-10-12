package com.coolblue.assignment.search.ui.presentation

/** Everything the user can do on the screen. */
sealed interface SearchUiAction {
    data class QueryChanged(val query: String) : SearchUiAction
    data object LoadMore : SearchUiAction
    data object Retry : SearchUiAction
}
