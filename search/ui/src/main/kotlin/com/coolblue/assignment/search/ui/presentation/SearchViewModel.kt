package com.coolblue.assignment.search.ui.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.result.AppResult
import com.coolblue.assignment.search.domain.usecase.SearchProductsUseCase
import com.coolblue.assignment.search.ui.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProducts: SearchProductsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // The query lives in SavedStateHandle so it survives process death; the pipeline below
    // observes it, so after a restore the search simply runs again.
    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    // Page numbers asked for by Retry and LoadMore.
    private val pageRequests = MutableSharedFlow<Int>(extraBufferCapacity = 1)

    private val _uiState = MutableStateFlow(SearchUiState(query = query.value))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // Every request goes through this one pipeline. collectLatest cancels the previous
        // load when a new request arrives, so at most one request is in flight and a stale
        // response can never overwrite a newer state.
        viewModelScope.launch {
            merge(query.debounce(DEBOUNCE_MS).map { FIRST_PAGE }, pageRequests)
                .collectLatest { page -> load(query.value, page) }
        }
    }

    fun onAction(action: SearchUiAction) {
        when (action) {
            is SearchUiAction.QueryChanged -> onQueryChanged(action.query)
            SearchUiAction.LoadMore -> Unit
            SearchUiAction.Retry -> retry()
        }
    }

    private fun onQueryChanged(newQuery: String) {
        savedStateHandle[KEY_QUERY] = newQuery
        // Updated synchronously so the text field never lags behind the keyboard.
        _uiState.update { state ->
            // A cleared field shows the prompt at once instead of after the debounce.
            val content = if (newQuery.isBlank()) SearchContent.Idle else state.content
            state.copy(query = newQuery, content = content)
        }
    }

    private fun retry() {
        when (_uiState.value.content) {
            is SearchContent.Error -> pageRequests.tryEmit(FIRST_PAGE)
            else -> Unit
        }
    }

    private suspend fun load(query: String, page: Int) {
        if (query.isBlank()) {
            _uiState.update { it.copy(content = SearchContent.Idle) }
            return
        }
        loadFirstPage(query)
    }

    private suspend fun loadFirstPage(query: String) {
        _uiState.update { it.copy(content = SearchContent.Loading) }
        val content = when (val result = searchProducts(query, FIRST_PAGE)) {
            is AppResult.Success -> result.data.toContent()
            is AppResult.Failure -> SearchContent.Error(result.error)
        }
        _uiState.update { it.copy(content = content) }
    }

    private fun ProductPage.toContent(): SearchContent =
        if (products.isEmpty()) {
            SearchContent.Empty
        } else {
            SearchContent.Loaded(products = products.map { it.toUiModel() }, nextPage = nextPage)
        }

    private companion object {
        const val KEY_QUERY = "query"
        const val FIRST_PAGE = 1
        const val DEBOUNCE_MS = 300L
    }
}
