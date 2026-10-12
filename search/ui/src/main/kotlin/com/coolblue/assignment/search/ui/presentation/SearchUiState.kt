package com.coolblue.assignment.search.ui.presentation

import com.coolblue.assignment.search.domain.result.AppError
import com.coolblue.assignment.search.ui.model.ProductUiModel

/** Everything the screen shows. One object, so the query and the content can never disagree. */
data class SearchUiState(
    val query: String = "",
    val content: SearchContent = SearchContent.Idle,
)

/** What the area under the search field shows. Exactly one of these at a time. */
sealed interface SearchContent {
    /** Blank query: invite the user to type. */
    data object Idle : SearchContent

    /** First page in flight. */
    data object Loading : SearchContent

    /** The query returned nothing. */
    data object Empty : SearchContent

    /** The first page failed. */
    data class Error(val error: AppError) : SearchContent

    /** At least one page is on screen. Load-more state travels with it so the rows stay. */
    data class Loaded(
        val products: List<ProductUiModel>,
        val nextPage: Int?,
        val isLoadingMore: Boolean = false,
        val loadMoreError: AppError? = null,
        /** A refined query is loading; these rows belong to the previous query and stay until it lands. */
        val isRefreshing: Boolean = false,
    ) : SearchContent
}
