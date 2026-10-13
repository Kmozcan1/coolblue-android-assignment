package com.coolblue.assignment.search.ui.presentation

internal sealed interface SearchUiAction {
    data class QueryChanged(val query: String) : SearchUiAction
    data object LoadMore : SearchUiAction
    data object Retry : SearchUiAction
}
