package com.coolblue.assignment.search.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.presentation.SearchContent

/** Ask for the next page when the user is this many rows from the end. */
private const val LOAD_MORE_THRESHOLD = 3

@Composable
internal fun ProductList(
    content: SearchContent.Loaded,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val nearEnd by remember(listState) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - 1 - LOAD_MORE_THRESHOLD
        }
    }
    // Re-evaluated when the user reaches the end and again after each page lands.
    // Repeat calls are harmless: the ViewModel ignores LoadMore while loading or on the last page.
    LaunchedEffect(nearEnd, content.products.size) {
        if (nearEnd) onLoadMore()
    }

    LazyColumn(state = listState, modifier = modifier.fillMaxSize()) {
        items(content.products, key = { it.id }) { product ->
            ProductRow(product)
            HorizontalDivider()
        }
        when {
            content.isLoadingMore -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            content.loadMoreError != null -> item(key = "error") {
                StatusMessage(
                    message = stringResource(R.string.load_more_error),
                    modifier = Modifier.fillMaxWidth(),
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRetry,
                )
            }
        }
    }
}
