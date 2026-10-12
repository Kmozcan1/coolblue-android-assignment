package com.coolblue.assignment.search.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

/** Where the idle, empty and error blocks sit: under the field, anchored top-left like the rows. */
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

    Scaffold { padding ->
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
                    SearchContent.Idle -> IdlePrompt()
                    SearchContent.Loading -> LoadingList()
                    SearchContent.Empty -> StatusMessage(
                        title = stringResource(R.string.search_empty, uiState.query),
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
                    )
                }
            }
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

/** Blank query: a headline and one line of guidance. */
@Composable
private fun IdlePrompt() {
    Column(modifier = StatusPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.search_idle_title), style = MaterialTheme.typography.displaySmall)
        Text(
            text = stringResource(R.string.search_idle_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
