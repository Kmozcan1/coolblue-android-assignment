package com.coolblue.assignment.search.ui.view

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.mapper.messageRes
import com.coolblue.assignment.search.ui.presentation.SearchContent
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance

/** Ask for the next page when the user is this many rows from the end. */
private const val LOAD_MORE_THRESHOLD = 3

/** One column on phones, two or three on tablets and in landscape. The loading skeleton uses the same cells. */
internal val ProductGridCells = GridCells.Adaptive(minSize = 400.dp)

@Composable
internal fun ProductList(
    content: SearchContent.Loaded,
    onProductClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val nearEnd by remember(gridState) {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= gridState.layoutInfo.totalItemsCount - 1 - LOAD_MORE_THRESHOLD
        }
    }
    // Re-evaluated when the user reaches the end and whenever the content changes: a page landing, a
    // refined query replacing the rows (possibly with the same number of them), an error clearing.
    // Repeat calls are harmless: the ViewModel ignores LoadMore while loading, refreshing, showing an
    // error or on the last page.
    LaunchedEffect(nearEnd, content) {
        if (nearEnd) onLoadMore()
    }
    // When a refined query's rows land they start at the top. Keyed on the query the rows answer, not
    // on refreshing ending, so typing a letter and deleting it again leaves the list where it was.
    // The first value is skipped so a restored screen keeps its scroll position. requestScrollToItem
    // rather than scrollToItem: the latter suspends on the scroll mutex and a finger-down drag holds it
    // at a higher priority, which would cancel this collector for good.
    val rowsQuery by rememberUpdatedState(content.query)
    LaunchedEffect(gridState) {
        snapshotFlow { rowsQuery }.drop(1).collect { gridState.requestScrollToItem(0) }
    }
    // A drag on the list means the user is done typing: drop the field's focus, which also hides the keyboard.
    val focusManager = LocalFocusManager.current
    LaunchedEffect(gridState) {
        gridState.interactionSource.interactions.filterIsInstance<DragInteraction.Start>().collect { focusManager.clearFocus() }
    }

    LazyVerticalGrid(columns = ProductGridCells, state = gridState, modifier = modifier.fillMaxSize()) {
        // The rule sits above each cell, so in a multi-column line the rules align whatever the cell heights.
        items(content.products, key = { it.id }) { product ->
            Column {
                HorizontalDivider()
                ProductRow(product = product, onClick = { onProductClick(product.id) })
            }
        }
        when {
            content.isLoadingMore -> fullWidthItem(key = "loading") { ProductRowSkeleton() }
            content.loadMoreError != null -> fullWidthItem(key = "error") {
                StatusMessage(
                    title = stringResource(R.string.load_more_error),
                    detail = stringResource(content.loadMoreError.messageRes()),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRetry,
                )
            }
            content.nextPage == null -> fullWidthItem(key = "end") {
                val count = content.products.size
                Text(
                    text = pluralStringResource(R.plurals.list_end, count, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                )
            }
        }
    }
}

/** A footer that spans every column, with the same rule above it as the product cells. */
private fun LazyGridScope.fullWidthItem(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) {
        Column {
            HorizontalDivider()
            content()
        }
    }
}
