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
        /** Where the next page stands; the list's footer shows it. */
        val paging: Paging,
        /** Page 1 is loading, for a refined query or a refresh; these rows stay until it lands. */
        val isRefreshing: Boolean = false,
        /** The last refresh failed. The rows stay: they still answer the query. */
        val refreshFailure: RefreshFailure? = null,
    ) : SearchContent
}

/** The next page, as the list's footer shows it. Exactly one of these at a time. */
@Immutable
internal sealed interface Paging {
    /** More to come: the list asks for [nextPage] near its end. */
    data class Idle(val nextPage: Int) : Paging

    data class Loading(val nextPage: Int) : Paging

    /** The user retries [nextPage] by hand. */
    data class Failed(val nextPage: Int, val error: AppError) : Paging

    /** The last page is on screen. */
    data object End : Paging
}

/** Not a data class: two failures with the same error must not be equal, or the second gets no snackbar. */
internal class RefreshFailure(val error: AppError)
