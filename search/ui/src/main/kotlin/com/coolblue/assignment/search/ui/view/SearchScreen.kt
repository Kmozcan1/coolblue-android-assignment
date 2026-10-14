package com.coolblue.assignment.search.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.mapper.messageRes
import com.coolblue.assignment.search.ui.presentation.RefreshFailure
import com.coolblue.assignment.search.ui.presentation.SearchContent
import com.coolblue.assignment.search.ui.presentation.SearchUiAction
import com.coolblue.assignment.search.ui.presentation.SearchUiState
import kotlinx.coroutines.launch

private val StatusPadding = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)

private const val SKELETON_ROWS_TO_FILL_A_TABLET = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    uiState: SearchUiState,
    onAction: (SearchUiAction) -> Unit,
) {
    val hasResults = uiState.content is SearchContent.Loaded
    var openProductId by rememberSaveable(hasResults) { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val headerState = rememberCollapsingHeaderState()
    val scope = rememberCoroutineScope()
    ShowSearchFieldOnTypingOrNewResultsEffect(headerState, uiState)

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(
                query = uiState.query,
                onQueryChange = { onAction(SearchUiAction.QueryChanged(it)) },
                modifier = Modifier
                    .collapsingHeader(headerState)
                    .onFocusChanged { if (it.hasFocus) scope.launch { headerState.show() } }
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
            )
            AnimatedContent(
                targetState = uiState.content,
                modifier = Modifier.fillMaxSize(),
                contentKey = { content -> content::class },
                transitionSpec = { shortFade() },
                label = "content",
            ) { content ->
                when (content) {
                    SearchContent.Loading -> LoadingList()
                    is SearchContent.Empty -> StatusMessage(
                        title = stringResource(R.string.search_empty, content.searchedQuery),
                        detail = stringResource(R.string.search_empty_hint),
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).then(StatusPadding),
                        centered = true,
                    )
                    is SearchContent.Error -> StatusMessage(
                        title = stringResource(content.error.messageRes()),
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).then(StatusPadding),
                        centered = true,
                        actionLabel = stringResource(R.string.retry),
                        onAction = { onAction(SearchUiAction.Retry) },
                    )
                    is SearchContent.Loaded -> ProductList(
                        content = content,
                        isRefining = content.searchedQuery != uiState.query,
                        header = headerState,
                        onProductClick = { openProductId = it },
                        onLoadMore = { onAction(SearchUiAction.LoadMore) },
                        onRetry = { onAction(SearchUiAction.Retry) },
                        onRefresh = {
                            scope.launch { headerState.show() }
                            onAction(SearchUiAction.Refresh)
                        },
                    )
                }
            }
        }
    }

    val openedProduct = (uiState.content as? SearchContent.Loaded)?.products?.firstOrNull { it.id == openProductId }
    if (openProductId != null && openedProduct == null) SideEffect { openProductId = null }

    val refreshFailure = (uiState.content as? SearchContent.Loaded)?.refreshFailure
    val isProductSheetOpen = openedProduct != null
    if (refreshFailure != null && !isProductSheetOpen) RefreshFailureSnackbar(refreshFailure, snackbarHostState, onAction)

    if (openedProduct != null) {
        ModalBottomSheet(
            onDismissRequest = { openProductId = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            // The default resolves to grey in the dark palette, whose sheet colour equals surfaceVariant.
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            ProductDetail(openedProduct)
        }
    }
}

@Composable
private fun ShowSearchFieldOnTypingOrNewResultsEffect(header: CollapsingHeaderState, uiState: SearchUiState) {
    val resultsQuery = (uiState.content as? SearchContent.Loaded)?.searchedQuery
    LaunchedEffect(uiState.query, resultsQuery) { header.show() }
}

@Composable
private fun RefreshFailureSnackbar(
    refreshFailure: RefreshFailure,
    snackbarHostState: SnackbarHostState,
    onAction: (SearchUiAction) -> Unit,
) {
    val message = stringResource(R.string.refresh_error, stringResource(refreshFailure.error.messageRes()))
    val retryLabel = stringResource(R.string.retry)
    LaunchedEffect(refreshFailure) {
        val result = snackbarHostState.showSnackbar(message, retryLabel, duration = SnackbarDuration.Long)
        onAction(SearchUiAction.RefreshFailureShown)
        if (result == SnackbarResult.ActionPerformed) onAction(SearchUiAction.Refresh)
    }
}

private fun shortFade() = ContentTransform(
    targetContentEnter = fadeIn(tween(durationMillis = 200, easing = LinearOutSlowInEasing)),
    initialContentExit = fadeOut(tween(durationMillis = 150, easing = FastOutLinearInEasing)),
    sizeTransform = null,
)

@Composable
private fun LoadingList() {
    val label = stringResource(R.string.loading)
    LazyVerticalGrid(
        columns = ProductGridCells,
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize().semantics { contentDescription = label },
    ) {
        items(count = SKELETON_ROWS_TO_FILL_A_TABLET) {
            CellWithDividerAbove { ProductRowSkeleton() }
        }
    }
}
