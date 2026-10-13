package com.coolblue.assignment.search.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coolblue.assignment.search.ui.presentation.SearchViewModel
import com.coolblue.assignment.search.ui.view.SearchScreen

/** The only composable that knows the ViewModel exists. */
@Composable
fun SearchRoute() {
    val viewModel: SearchViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(uiState = uiState, onAction = viewModel::onAction)
}
