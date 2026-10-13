package com.coolblue.assignment.search.ui.presentation

import androidx.compose.runtime.Immutable
import com.coolblue.assignment.core.domain.result.AppError
import com.coolblue.assignment.search.ui.model.ProductUiModel

@Immutable
internal data class SearchUiState(
    val query: String = "",
    val content: SearchContent = SearchContent.Loading,
) {
    val isRefining: Boolean get() = content is SearchContent.Loaded && content.searchedQuery != query
}

@Immutable
internal sealed interface SearchContent {
    data object Loading : SearchContent

    data class Empty(val searchedQuery: String) : SearchContent

    data class Error(val error: AppError) : SearchContent

    data class Loaded(
        val searchedQuery: String,
        val products: List<ProductUiModel>,
        val paging: Paging,
        val isRefreshing: Boolean = false,
        val refreshFailure: RefreshFailure? = null,
    ) : SearchContent
}

@Immutable
internal sealed interface Paging {
    data class Idle(val nextPage: Int) : Paging

    data class Loading(val nextPage: Int) : Paging

    data class Failed(val nextPage: Int, val error: AppError) : Paging

    data object End : Paging
}

// Not a data class: two failures with the same error must not be equal, or the second gets no snackbar.
internal class RefreshFailure(val error: AppError)
