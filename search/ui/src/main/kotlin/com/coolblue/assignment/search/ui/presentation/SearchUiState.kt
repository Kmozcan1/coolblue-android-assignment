package com.coolblue.assignment.search.ui.presentation

import androidx.compose.runtime.Immutable
import com.coolblue.assignment.core.domain.result.AppError
import com.coolblue.assignment.search.ui.model.ProductUiModel

/** Everything the screen shows. One object, so the query and the content can never disagree. */
@Immutable
internal data class SearchUiState(
    val query: String = "",
    val content: SearchContent = SearchContent.Loading,
)

/** What the area under the search field shows. Exactly one of these at a time. */
@Immutable
internal sealed interface SearchContent {
    data object Loading : SearchContent

    /** The query returned nothing. Carries that query: the field may already hold a newer one. */
    data class Empty(val query: String) : SearchContent

    data class Error(val error: AppError) : SearchContent

    /** At least one page is on screen. Load-more state travels with it so the rows stay. */
    data class Loaded(
        /** The query these rows answer; the field may already hold a newer one. */
        val query: String,
        val products: List<ProductUiModel>,
        val nextPage: Int?,
        val isLoadingMore: Boolean = false,
        val loadMoreError: AppError? = null,
        /**
         * Page 1 is loading and these rows stay until it lands: either a refined query (the rows belong to
         * the previous one) or a pull to refresh (the same query, fetched again).
         */
        val isRefreshing: Boolean = false,
        /** The last pull to refresh failed; the rows stay, since they still answer the query. */
        val refreshError: AppError? = null,
    ) : SearchContent
}
