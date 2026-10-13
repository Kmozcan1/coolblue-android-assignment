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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.mapper.messageRes
import com.coolblue.assignment.search.ui.presentation.SearchContent
import com.coolblue.assignment.search.ui.presentation.SearchUiAction
import com.coolblue.assignment.search.ui.presentation.SearchUiState

/** Where the empty and error blocks sit: under the field, anchored top-left like the rows. */
private val StatusPadding = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)

/** Stateless: everything comes in through [uiState], everything goes out through [onAction]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    uiState: SearchUiState,
    onAction: (SearchUiAction) -> Unit,
) {
    // Which product's sheet is open. Screen-local on purpose: it is transient chrome over a product
    // the Loaded content already holds, so it has no business in SearchUiState. Keyed on Loaded-ness:
    // rotation keeps the ViewModel and so the sheet, but after process death the search restarts and
    // a saved id must not reopen the sheet when its page is fetched again.
    var openProductId by rememberSaveable(uiState.content is SearchContent.Loaded) { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Edge-to-edge switches off the window's own keyboard resizing, so the keyboard is an inset here:
    // the grid shrinks above it instead of scrolling under it.
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
                onQueryChange = {
                    openProductId = null
                    onAction(SearchUiAction.QueryChanged(it))
                },
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
            )
            // A short fade between kinds of content (skeleton to rows, rows to empty). Keyed on the
            // kind, so a new page appended to Loaded updates in place without animating.
            AnimatedContent(
                targetState = uiState.content,
                modifier = Modifier.fillMaxSize(),
                contentKey = { it::class },
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = fadeIn(tween(durationMillis = 200, easing = LinearOutSlowInEasing)),
                        initialContentExit = fadeOut(tween(durationMillis = 150, easing = FastOutLinearInEasing)),
                        sizeTransform = null,
                    )
                },
                label = "content",
            ) { content ->
                when (content) {
                    SearchContent.Loading -> LoadingList()
                    is SearchContent.Empty -> StatusMessage(
                        title = stringResource(R.string.search_empty, content.query),
                        detail = stringResource(R.string.search_empty_hint),
                        modifier = StatusPadding,
                    )
                    is SearchContent.Error -> StatusMessage(
                        title = stringResource(content.error.messageRes()),
                        modifier = StatusPadding,
                        actionLabel = stringResource(R.string.retry),
                        onAction = { onAction(SearchUiAction.Retry) },
                    )
                    is SearchContent.Loaded -> ProductList(
                        content = content,
                        onProductClick = { openProductId = it },
                        onLoadMore = { onAction(SearchUiAction.LoadMore) },
                        onRetry = { onAction(SearchUiAction.Retry) },
                        onRefresh = { onAction(SearchUiAction.Refresh) },
                    )
                }
            }
        }
    }

    // A failed refresh leaves the rows on screen, so it is told in a snackbar rather than in their place.
    // Acknowledged once dismissed, so a rotation mid-snackbar shows it again rather than losing it.
    val refreshError = (uiState.content as? SearchContent.Loaded)?.refreshError
    if (refreshError != null) {
        val message = stringResource(R.string.refresh_error, stringResource(refreshError.messageRes()))
        val retryLabel = stringResource(R.string.retry)
        LaunchedEffect(refreshError) {
            // Long, not the Indefinite an action label defaults to: the snackbar must go away on its own.
            val result = snackbarHostState.showSnackbar(message, actionLabel = retryLabel, duration = SnackbarDuration.Long)
            onAction(if (result == SnackbarResult.ActionPerformed) SearchUiAction.Refresh else SearchUiAction.RefreshErrorShown)
        }
    }

    val openProduct = (uiState.content as? SearchContent.Loaded)?.products?.firstOrNull { it.id == openProductId }
    if (openProduct != null) {
        ModalBottomSheet(
            onDismissRequest = { openProductId = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            ProductDetail(openProduct)
        }
    }
}

/** First page in flight: row-shaped placeholders in the same cells as the list, so nothing jumps when rows land. */
@Composable
private fun LoadingList() {
    val label = stringResource(R.string.loading)
    LazyVerticalGrid(
        columns = ProductGridCells,
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize().semantics { contentDescription = label },
    ) {
        items(count = 6) {
            Column {
                HorizontalDivider()
                ProductRowSkeleton()
            }
        }
    }
}
