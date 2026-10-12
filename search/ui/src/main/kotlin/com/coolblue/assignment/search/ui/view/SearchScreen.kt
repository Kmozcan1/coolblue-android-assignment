package com.coolblue.assignment.search.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.mapper.messageRes
import com.coolblue.assignment.search.ui.presentation.SearchContent
import com.coolblue.assignment.search.ui.presentation.SearchUiAction
import com.coolblue.assignment.search.ui.presentation.SearchUiState

/** Stateless: everything comes in through [uiState], everything goes out through [onAction]. */
@Composable
internal fun SearchScreen(
    uiState: SearchUiState,
    onAction: (SearchUiAction) -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(
                query = uiState.query,
                onQueryChange = { onAction(SearchUiAction.QueryChanged(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when (val content = uiState.content) {
                SearchContent.Idle -> StatusMessage(
                    message = stringResource(R.string.search_idle),
                    modifier = Modifier.fillMaxSize(),
                )
                SearchContent.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                SearchContent.Empty -> StatusMessage(
                    message = stringResource(R.string.search_empty, uiState.query),
                    modifier = Modifier.fillMaxSize(),
                )
                is SearchContent.Error -> StatusMessage(
                    message = stringResource(content.error.messageRes()),
                    modifier = Modifier.fillMaxSize(),
                    actionLabel = stringResource(R.string.retry),
                    onAction = { onAction(SearchUiAction.Retry) },
                )
                is SearchContent.Loaded -> ProductList(
                    content = content,
                    onLoadMore = { onAction(SearchUiAction.LoadMore) },
                    onRetry = { onAction(SearchUiAction.Retry) },
                )
            }
        }
    }
}
