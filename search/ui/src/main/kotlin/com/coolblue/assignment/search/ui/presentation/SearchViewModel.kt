package com.coolblue.assignment.search.ui.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.usecase.SearchProductsUseCase
import com.coolblue.assignment.search.ui.mapper.toUiModel
import com.coolblue.assignment.search.ui.model.ProductUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val searchProducts: SearchProductsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val productPageRequests = MutableSharedFlow<Int>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private var refreshPending = false

    private val _uiState = MutableStateFlow(SearchUiState(query = query.value, content = SearchContent.Loading))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        searchProductsForEachQueryAndPage()
    }

    private fun searchProductsForEachQueryAndPage() {
        val pagesToLoad = merge(firstPageOfEachSearch(), productPageRequests)
        viewModelScope.launch {
            pagesToLoad.collectLatest { page -> loadProducts(query.value, page) }
        }
    }

    private fun firstPageOfEachSearch(): Flow<Int> {
        val openingQuery = query.value
        return query.withIndex()
            .debounce { (index, value) ->
                val isOpeningQuery = index == 0 && value == openingQuery
                if (isOpeningQuery) Duration.ZERO else TYPING_PAUSE_MS.milliseconds
            }
            .map { FIRST_PAGE }
    }

    fun onAction(action: SearchUiAction) {
        when (action) {
            is SearchUiAction.QueryChanged -> onQueryChanged(action.query)
            SearchUiAction.LoadMore -> loadMoreProducts()
            SearchUiAction.Retry -> retryFailedLoad()
            SearchUiAction.Refresh -> refreshResults()
            SearchUiAction.RefreshFailureShown -> updateShownResults { it.copy(refreshFailure = null) }
        }
    }

    private fun onQueryChanged(newQuery: String) {
        if (newQuery == _uiState.value.query) return
        savedStateHandle[KEY_QUERY] = newQuery
        _uiState.update { state -> state.copy(query = newQuery, content = state.content.awaitingResultsFor(newQuery)) }
    }

    private fun SearchContent.awaitingResultsFor(newQuery: String): SearchContent =
        if (this is SearchContent.Loaded) copy(isRefreshing = searchedQuery != newQuery || refreshPending) else this

    private fun loadMoreProducts() {
        val shownResults = _uiState.value.content as? SearchContent.Loaded ?: return
        val paging = shownResults.paging as? Paging.Idle ?: return
        if (shownResults.isRefreshing) return
        productPageRequests.tryEmit(paging.nextPage)
    }

    private fun retryFailedLoad() {
        when (val content = _uiState.value.content) {
            is SearchContent.Error -> productPageRequests.tryEmit(FIRST_PAGE)
            is SearchContent.Loaded -> if (content.paging is Paging.Failed && !content.isRefreshing) {
                productPageRequests.tryEmit(content.paging.nextPage)
            }

            SearchContent.Loading, is SearchContent.Empty -> Unit
        }
    }

    private fun refreshResults() {
        val shownResults = _uiState.value.content as? SearchContent.Loaded ?: return
        if (shownResults.isRefreshing) return
        refreshPending = true
        updateShownResults { it.copy(isRefreshing = true) }
        productPageRequests.tryEmit(FIRST_PAGE)
    }

    private suspend fun loadProducts(query: String, page: Int) {
        if (page == FIRST_PAGE) loadFirstPage(query) else loadNextPage(query, page)
    }

    private suspend fun loadFirstPage(query: String) {
        val shownResultsForQuery = (_uiState.value.content as? SearchContent.Loaded)?.takeIf { it.searchedQuery == query }
        val areResultsAlreadyShown = shownResultsForQuery != null && !shownResultsForQuery.isRefreshing
        if (areResultsAlreadyShown) {
            allowLoadingMoreAgain()
            return
        }
        val isRefresh = shownResultsForQuery != null
        keepShownProductsOrShowSkeleton()
        val result = if (isRefresh) {
            takingAtLeast(MIN_REFRESH_SPINNER_MS.milliseconds) { searchProducts(query, FIRST_PAGE) }
        } else {
            searchProducts(query, FIRST_PAGE)
        }
        val hasQueryChangedMeanwhile = this.query.value != query
        if (hasQueryChangedMeanwhile) return
        refreshPending = false
        when (result) {
            is AppResult.Success -> _uiState.update { it.copy(content = result.data.toContent(query)) }
            is AppResult.Failure -> if (isRefresh) {
                updateShownResults { it.copy(isRefreshing = false, refreshFailure = RefreshFailure(result.error)) }
            } else {
                _uiState.update { it.copy(content = SearchContent.Error(result.error)) }
            }
        }
    }

    private fun allowLoadingMoreAgain() {
        updateShownResults { it.copy(paging = it.paging.idleIfLoading()) }
    }

    private fun keepShownProductsOrShowSkeleton() {
        _uiState.update { state ->
            val content = state.content
            val loading = if (content is SearchContent.Loaded) {
                content.copy(isRefreshing = true, paging = content.paging.idleIfLoadingOrFailed(), refreshFailure = null)
            } else {
                SearchContent.Loading
            }
            state.copy(content = loading)
        }
    }

    private suspend fun loadNextPage(query: String, page: Int) {
        updateShownResults { it.copy(paging = Paging.Loading(page)) }
        when (val result = searchProducts(query, page)) {
            is AppResult.Success -> updateShownResults { loaded ->
                loaded.copy(
                    products = (loaded.products + result.data.products.map { it.toUiModel() }).withoutDuplicateProducts(),
                    paging = pagingFor(result.data.nextPage),
                )
            }

            is AppResult.Failure -> updateShownResults { it.copy(paging = Paging.Failed(page, result.error)) }
        }
    }

    private fun updateShownResults(transform: (SearchContent.Loaded) -> SearchContent.Loaded) {
        _uiState.update { state ->
            val content = state.content
            if (content is SearchContent.Loaded) state.copy(content = transform(content)) else state
        }
    }

    private fun ProductPage.toContent(query: String): SearchContent =
        if (products.isEmpty()) {
            SearchContent.Empty(query)
        } else {
            SearchContent.Loaded(
                searchedQuery = query,
                products = products.map { it.toUiModel() }.withoutDuplicateProducts(),
                paging = pagingFor(nextPage),
            )
        }

    private companion object {
        const val KEY_QUERY = "query"
        const val FIRST_PAGE = 1
        const val TYPING_PAUSE_MS = 300L
        const val MIN_REFRESH_SPINNER_MS = 800L
    }
}

private fun List<ProductUiModel>.withoutDuplicateProducts() = distinctBy { it.id }

private fun pagingFor(nextPage: Int?): Paging = if (nextPage == null) Paging.End else Paging.Idle(nextPage)

private fun Paging.idleIfLoading(): Paging = if (this is Paging.Loading) Paging.Idle(nextPage) else this

private fun Paging.idleIfLoadingOrFailed(): Paging = when (this) {
    is Paging.Loading -> Paging.Idle(nextPage)
    is Paging.Failed -> Paging.Idle(nextPage)
    is Paging.Idle, Paging.End -> this
}

private suspend fun <T> takingAtLeast(duration: Duration, block: suspend () -> T): T = coroutineScope {
    val minimum = launch { delay(duration) }
    block().also { minimum.join() }
}
