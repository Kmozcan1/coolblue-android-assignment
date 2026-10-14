package com.coolblue.assignment.search.ui.view

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.mapper.messageRes
import com.coolblue.assignment.search.ui.presentation.Paging
import com.coolblue.assignment.search.ui.presentation.SearchContent
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance

private const val LOAD_MORE_WHEN_PRODUCTS_LEFT = 3

internal val ProductGridCells = GridCells.Adaptive(minSize = 400.dp)

@Composable
internal fun ProductList(
    content: SearchContent.Loaded,
    isRefining: Boolean,
    header: CollapsingHeaderState,
    onProductClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    LoadMoreNearEndEffect(gridState, content, isRefining, onLoadMore)
    ScrollToTopOnNewResultsEffect(gridState, content.searchedQuery)
    SnapHeaderWhenScrollingStopsEffect(gridState, header)
    HideKeyboardOnDragEffect(gridState)

    val focusManager = LocalFocusManager.current
    val inputModeManager = LocalInputModeManager.current
    val refreshFromAccessibilityMenu = CustomAccessibilityAction(stringResource(R.string.refresh)) {
        gridState.requestScrollToItem(0)
        onRefresh()
        true
    }

    val pullState = rememberPullToRefreshState()
    val isPullRefreshing = content.isRefreshing && !isRefining
    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        state = pullState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = isPullRefreshing,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        LazyVerticalGrid(
            columns = ProductGridCells,
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(header.scrollConnection)
                .semantics { customActions = listOf(refreshFromAccessibilityMenu) },
        ) {
            items(content.products, key = { it.id }) { product ->
                CellWithDividerAbove {
                    ProductRow(
                        product = product,
                        onClick = {
                            if (inputModeManager.inputMode == InputMode.Touch) focusManager.clearFocus()
                            onProductClick(product.id)
                        },
                    )
                }
            }
            nextPageFooter(content.paging, productCount = content.products.size, onRetry = onRetry)
        }
        if (isRefining) RefiningProgressBar()
    }
}

@Composable
private fun LoadMoreNearEndEffect(gridState: LazyGridState, content: SearchContent.Loaded, isRefining: Boolean, onLoadMore: () -> Unit) {
    val isNearEnd by remember(gridState) {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= gridState.layoutInfo.totalItemsCount - 1 - LOAD_MORE_WHEN_PRODUCTS_LEFT
        }
    }
    LaunchedEffect(isNearEnd, content, isRefining) {
        if (isNearEnd) onLoadMore()
    }
}

@Composable
private fun ScrollToTopOnNewResultsEffect(gridState: LazyGridState, searchedQuery: String) {
    val latestSearchedQuery by rememberUpdatedState(searchedQuery)
    LaunchedEffect(gridState) {
        val newResults = snapshotFlow { latestSearchedQuery }.drop(1)
        // Not scrollToItem: a drag holds the scroll mutex at a higher priority, which would cancel this collector for good.
        newResults.collect { gridState.requestScrollToItem(0) }
    }
}

@Composable
private fun SnapHeaderWhenScrollingStopsEffect(gridState: LazyGridState, header: CollapsingHeaderState) {
    LaunchedEffect(gridState, header) {
        snapshotFlow { gridState.isScrollInProgress }.filter { !it }.collect { header.snapToShownOrHidden() }
    }
}

@Composable
private fun HideKeyboardOnDragEffect(gridState: LazyGridState) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(gridState) {
        gridState.interactionSource.interactions.filterIsInstance<DragInteraction.Start>().collect { focusManager.clearFocus() }
    }
}

private fun LazyGridScope.nextPageFooter(paging: Paging, productCount: Int, onRetry: () -> Unit) {
    when (paging) {
        is Paging.Idle -> Unit
        is Paging.Loading -> item(key = "loading") {
            CellWithDividerAbove { ProductRowSkeleton() }
        }
        is Paging.Failed -> fullWidthItem(key = "error") {
            StatusMessage(
                title = stringResource(R.string.load_more_error),
                detail = stringResource(paging.error.messageRes()),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
            )
        }
        Paging.End -> fullWidthItem(key = "end") {
            Text(
                text = appPluralString(R.plurals.list_end, productCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            )
        }
    }
}

private fun LazyGridScope.fullWidthItem(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) {
        CellWithDividerAbove(content)
    }
}

@Composable
internal fun CellWithDividerAbove(content: @Composable () -> Unit) {
    Column {
        HorizontalDivider()
        content()
    }
}

@Composable
private fun BoxScope.RefiningProgressBar() {
    val orange = MaterialTheme.colorScheme.secondary
    val opaqueOrangeTrack = orange.copy(alpha = 0.24f).compositeOver(MaterialTheme.colorScheme.background)
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
        color = orange,
        trackColor = opaqueOrangeTrack,
    )
}
