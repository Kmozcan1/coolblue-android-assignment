package com.coolblue.assignment.search.ui.view

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.coolblue.assignment.core.domain.result.AppError
import com.coolblue.assignment.core.ui.theme.CoolblueTheme
import com.coolblue.assignment.search.ui.model.ProductUiModel
import com.coolblue.assignment.search.ui.presentation.SearchContent
import com.coolblue.assignment.search.ui.presentation.SearchUiState

private val iphone = ProductUiModel(
    id = 1,
    name = "Apple iPhone 6 32GB Grijs",
    imageUrl = "https://image.coolblue.nl/300x750/products/818870",
    priceCents = 36900,
    listPriceCents = 39900,
    rating = 9.1,
    reviewCount = 952,
    usps = listOf("32 GB opslagcapaciteit", "4,7 inch Retina HD scherm", "iOS 11"),
    promoText = "middenklasse iPhone",
    nextDayDelivery = true,
)

private val macbook = iphone.copy(
    id = 2,
    name = "Apple MacBook Air 13,3\" (2017) MQD32N/A",
    priceCents = 87900,
    listPriceCents = null,
    rating = 8.7,
    reviewCount = 12,
    promoText = null,
    nextDayDelivery = false,
)

private val loaded = SearchContent.Loaded(query = "iphone", products = listOf(iphone, macbook), nextPage = 2)

@PreviewLightDark
@Composable
private fun ProductRowPreview() {
    CoolblueTheme { Surface { ProductRow(iphone, onClick = {}) } }
}

@PreviewLightDark
@Composable
private fun ProductRowPlainPreview() {
    CoolblueTheme { Surface { ProductRow(macbook, onClick = {}) } }
}

@PreviewLightDark
@Composable
private fun ProductDetailPreview() {
    CoolblueTheme { Surface { ProductDetail(iphone) } }
}

@PreviewLightDark
@Composable
private fun ProductRowSkeletonPreview() {
    CoolblueTheme { Surface { ProductRowSkeleton() } }
}

@PreviewLightDark
@Composable
private fun SearchScreenLoadingPreview() {
    CoolblueTheme { SearchScreen(SearchUiState("iphone", SearchContent.Loading), onAction = {}) }
}

@PreviewLightDark
@Composable
private fun SearchScreenEmptyPreview() {
    CoolblueTheme { SearchScreen(SearchUiState("xyz", SearchContent.Empty("xyz")), onAction = {}) }
}

@PreviewLightDark
@Composable
private fun SearchScreenErrorPreview() {
    CoolblueTheme { SearchScreen(SearchUiState("iphone", SearchContent.Error(AppError.Network)), onAction = {}) }
}

@PreviewLightDark
@Composable
private fun SearchScreenLoadedPreview() {
    CoolblueTheme { SearchScreen(SearchUiState("iphone", loaded), onAction = {}) }
}

@PreviewLightDark
@Composable
private fun SearchScreenRefreshingPreview() {
    CoolblueTheme { SearchScreen(SearchUiState("iphone x", loaded.copy(isRefreshing = true)), onAction = {}) }
}

@PreviewLightDark
@Composable
private fun SearchScreenEndPreview() {
    CoolblueTheme { SearchScreen(SearchUiState("iphone", loaded.copy(nextPage = null)), onAction = {}) }
}

@PreviewLightDark
@Composable
private fun SearchScreenLoadingMorePreview() {
    CoolblueTheme { SearchScreen(SearchUiState("iphone", loaded.copy(isLoadingMore = true)), onAction = {}) }
}

@PreviewLightDark
@Composable
private fun SearchScreenLoadMoreErrorPreview() {
    CoolblueTheme {
        SearchScreen(SearchUiState("iphone", loaded.copy(loadMoreError = AppError.Server(500))), onAction = {})
    }
}

@Preview(locale = "nl")
@Composable
private fun SearchScreenDutchPreview() {
    CoolblueTheme { SearchScreen(SearchUiState("iphone", loaded), onAction = {}) }
}
