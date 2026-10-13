package com.coolblue.assignment.search.ui.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.usecase.SearchProductsUseCase
import com.coolblue.assignment.search.ui.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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

    // The query lives in SavedStateHandle so it survives process death; the pipeline below
    // observes it, so after a restore the search simply runs again.
    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    // Dropping the older request keeps tryEmit from ever failing; collectLatest wants only the newest anyway.
    private val pageRequests = MutableSharedFlow<Int>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    // Outlives an edit away and back, which recomputes isRefreshing from the query alone.
    private var refreshPending = false

    // The query is searched straight away (the whole catalogue while it is blank), so the screen opens
    // on the skeleton.
    private val _uiState = MutableStateFlow(SearchUiState(query = query.value, content = SearchContent.Loading))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // Every request goes through this one pipeline. collectLatest cancels the previous
        // load when a new request arrives, so at most one request is in flight and a stale
        // response can never overwrite a newer state.
        val openingQuery = query.value
        viewModelScope.launch {
            // The opening query (blank, or restored after process death) goes at once; typing waits out the
            // debounce. Compared by value too, so a query typed before collection starts is still debounced.
            val queries = query.withIndex().debounce { (index, value) ->
                if (index == 0 && value == openingQuery) Duration.ZERO else DEBOUNCE_MS.milliseconds
            }
            merge(queries.map { FIRST_PAGE }, pageRequests)
                .collectLatest { page -> load(query.value, page) }
        }
    }

    fun onAction(action: SearchUiAction) {
        when (action) {
            is SearchUiAction.QueryChanged -> onQueryChanged(action.query)
            SearchUiAction.LoadMore -> loadMore()
            SearchUiAction.Retry -> retry()
            SearchUiAction.Refresh -> refresh()
            SearchUiAction.RefreshFailureShown -> updateLoaded { it.copy(refreshFailure = null) }
        }
    }

    private fun onQueryChanged(newQuery: String) {
        if (newQuery == _uiState.value.query) return
        savedStateHandle[KEY_QUERY] = newQuery
        // Updated synchronously so the text field never lags behind the keyboard.
        _uiState.update { state ->
            val content = state.content
            state.copy(
                query = newQuery,
                content = when {
                    // From this keystroke the rows belong to the previous query, so LoadMore must not
                    // fetch the new query's page 2 under them during the debounce. Edited back to the
                    // query the rows answer: nothing to wait for, unless a refresh is.
                    content is SearchContent.Loaded -> content.copy(isRefreshing = content.query != newQuery || refreshPending)
                    else -> content
                },
            )
        }
    }

    private fun loadMore() {
        val content = _uiState.value.content as? SearchContent.Loaded ?: return
        // No next page, already loading, showing an error (the user retries that by hand), or refreshing:
        // page 1 is about to replace these rows, and after a refined query page 2 would not even match them.
        if (content.nextPage == null || content.isLoadingMore || content.isRefreshing || content.loadMoreError != null) return
        pageRequests.tryEmit(content.nextPage)
    }

    private fun retry() {
        when (val content = _uiState.value.content) {
            is SearchContent.Error -> pageRequests.tryEmit(FIRST_PAGE)
            // Not while refreshing: page 1 is about to replace these rows, so page N would not follow them.
            is SearchContent.Loaded -> if (content.loadMoreError != null && content.nextPage != null && !content.isRefreshing) {
                pageRequests.tryEmit(content.nextPage)
            }

            SearchContent.Loading, is SearchContent.Empty -> Unit
        }
    }

    private fun refresh() {
        val content = _uiState.value.content as? SearchContent.Loaded ?: return
        if (content.isRefreshing) return // page 1 is already on its way
        refreshPending = true
        // Now, not when the load starts, so the pull indicator never springs back.
        updateLoaded { it.copy(isRefreshing = true) }
        pageRequests.tryEmit(FIRST_PAGE)
    }

    private suspend fun load(query: String, page: Int) {
        if (page == FIRST_PAGE) loadFirstPage(query) else loadNextPage(query, page)
    }

    private suspend fun loadFirstPage(query: String) {
        val rowsForQuery = (_uiState.value.content as? SearchContent.Loaded)?.takeIf { it.query == query }
        if (rowsForQuery != null && !rowsForQuery.isRefreshing) {
            // Edited away and back: nothing to fetch. A load-more that was in flight has just been
            // cancelled by collectLatest, so its spinner goes too; the list asks again.
            updateLoaded { it.copy(isRefreshing = false, isLoadingMore = false) }
            return
        }
        val isRefresh = rowsForQuery != null
        // Rows already on screen stay while a refined query loads; only a cold start shows the skeleton.
        // (onQueryChanged already set isRefreshing; this also drops a cancelled load-more's state.)
        _uiState.update { state ->
            val content = state.content
            val loading = if (content is SearchContent.Loaded) {
                content.copy(isRefreshing = true, isLoadingMore = false, loadMoreError = null, refreshFailure = null)
            } else {
                SearchContent.Loading
            }
            state.copy(content = loading)
        }
        val result = if (isRefresh) {
            // An instant answer would only blink the pull indicator, so it stays up long enough to be seen.
            takingAtLeast(REFRESH_MIN_MS.milliseconds) { searchProducts(query, FIRST_PAGE) }
        } else {
            searchProducts(query, FIRST_PAGE)
        }
        // Typed on while this was in flight: the debounce will load the newer query, and these rows
        // must not show under it meanwhile. collectLatest only cancels once that debounce fires.
        if (this.query.value != query) return
        refreshPending = false
        when (result) {
            is AppResult.Success -> _uiState.update { it.copy(content = result.data.toContent(query)) }
            is AppResult.Failure -> if (isRefresh) {
                updateLoaded { it.copy(isRefreshing = false, refreshFailure = RefreshFailure(result.error)) }
            } else {
                _uiState.update { it.copy(content = SearchContent.Error(result.error)) }
            }
        }
    }

    private suspend fun loadNextPage(query: String, page: Int) {
        updateLoaded { it.copy(isLoadingMore = true, loadMoreError = null) }
        when (val result = searchProducts(query, page)) {
            is AppResult.Success -> updateLoaded { loaded ->
                loaded.copy(
                    // Results can shift between page requests, so a page may repeat a row on screen;
                    // the grid keys rows by id and two equal keys would crash it.
                    products = (loaded.products + result.data.products.map { it.toUiModel() }).distinctBy { it.id },
                    nextPage = result.data.nextPage,
                    isLoadingMore = false,
                )
            }

            is AppResult.Failure -> updateLoaded {
                it.copy(
                    isLoadingMore = false,
                    loadMoreError = result.error
                )
            }
        }
    }

    /** Applies [transform] only while the content is Loaded; a query change in between makes it a no-op. */
    private fun updateLoaded(transform: (SearchContent.Loaded) -> SearchContent.Loaded) {
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
                query = query,
                // The same guard as a later page: a repeated row would crash the grid.
                products = products.map { it.toUiModel() }.distinctBy { it.id },
                nextPage = nextPage
            )
        }

    private companion object {
        const val KEY_QUERY = "query"
        const val FIRST_PAGE = 1
        const val DEBOUNCE_MS = 300L
        const val REFRESH_MIN_MS = 800L
    }
}

/** Runs [block], then waits out whatever is left of [duration]. */
private suspend fun <T> takingAtLeast(duration: Duration, block: suspend () -> T): T = coroutineScope {
    val minimum = launch { delay(duration) }
    block().also { minimum.join() }
}
