package com.coolblue.assignment.search.ui.presentation

internal sealed interface SearchUiAction {
    data class QueryChanged(val query: String) : SearchUiAction
    data object LoadMore : SearchUiAction
    data object Retry : SearchUiAction

    /** Pulled to refresh: page 1 of the rows' query again. */
    data object Refresh : SearchUiAction

    /** The screen has told the user a refresh failed. */
    data object RefreshErrorShown : SearchUiAction
}
