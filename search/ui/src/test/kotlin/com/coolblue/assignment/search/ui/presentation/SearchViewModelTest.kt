package com.coolblue.assignment.search.ui.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.coolblue.assignment.core.test.MainDispatcherRule
import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.result.AppError
import com.coolblue.assignment.search.domain.result.AppResult
import com.coolblue.assignment.search.domain.usecase.SearchProductsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchProducts: SearchProductsUseCase = mockk()

    private fun viewModel(savedQuery: String? = null) = SearchViewModel(
        searchProducts = searchProducts,
        savedStateHandle = SavedStateHandle(savedQuery?.let { mapOf("query" to it) } ?: emptyMap()),
    )

    private fun product(id: Int) = Product(
        id = id, name = "Product $id", imageUrl = "", salesPriceCents = 100, listPriceCents = null,
        reviewAverage = 9.0, reviewCount = 1, usps = emptyList(), promoText = null, nextDayDelivery = false,
    )

    private fun page(page: Int, pageCount: Int = 3, products: List<Product> = listOf(product(page))) =
        AppResult.Success(ProductPage(products = products, page = page, pageCount = pageCount))

    @Test
    fun `given a blank query, then content is Idle and nothing is requested`() = runTest {
        val viewModel = viewModel()

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Idle)
        coVerify(exactly = 0) { searchProducts(any(), any()) }
    }

    @Test
    fun `given the user types ip then iph within 300 ms, then only iph is searched`() = runTest {
        coEvery { searchProducts("iph", 1) } returns page(1)
        val viewModel = viewModel()

        viewModel.onAction(SearchUiAction.QueryChanged("ip"))
        advanceTimeBy(100)
        viewModel.onAction(SearchUiAction.QueryChanged("iph"))
        advanceUntilIdle()

        coVerify(exactly = 0) { searchProducts("ip", any()) }
        coVerify(exactly = 1) { searchProducts("iph", 1) }
        assertThat(viewModel.uiState.value.query).isEqualTo("iph")
        assertThat(viewModel.uiState.value.content).isInstanceOf(SearchContent.Loaded::class.java)
    }

    @Test
    fun `given a query, when the first page loads, then state goes Loading then Loaded with a next page`() = runTest {
        coEvery { searchProducts("iphone", 1) } returns page(1)
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertThat(awaitItem().content).isEqualTo(SearchContent.Idle)
            viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
            assertThat(awaitItem().query).isEqualTo("iphone")
            assertThat(awaitItem().content).isEqualTo(SearchContent.Loading)
            val loaded = awaitItem().content as SearchContent.Loaded
            assertThat(loaded.products.map { it.id }).containsExactly(1)
            assertThat(loaded.nextPage).isEqualTo(2)
        }
    }

    @Test
    fun `given the first page fails, then content is Error, and Retry loads it`() = runTest {
        coEvery { searchProducts("iphone", 1) } returns AppResult.Failure(AppError.Network) andThen page(1)
        val viewModel = viewModel()

        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Error(AppError.Network))

        viewModel.onAction(SearchUiAction.Retry)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.content).isInstanceOf(SearchContent.Loaded::class.java)
    }

    @Test
    fun `given zero results, then content is Empty`() = runTest {
        coEvery { searchProducts("nothing", 1) } returns page(1, pageCount = 1, products = emptyList())
        val viewModel = viewModel()

        viewModel.onAction(SearchUiAction.QueryChanged("nothing"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Empty)
    }

    @Test
    fun `given a saved query, when the view model is created, then the search runs by itself`() = runTest {
        coEvery { searchProducts("iphone", 1) } returns page(1)

        val viewModel = viewModel(savedQuery = "iphone")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEqualTo("iphone")
        assertThat(viewModel.uiState.value.content).isInstanceOf(SearchContent.Loaded::class.java)
    }

    @Test
    fun `given results on screen, when the query is cleared, then content is Idle at once`() = runTest {
        coEvery { searchProducts("iphone", 1) } returns page(1)
        val viewModel = viewModel()
        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
        advanceUntilIdle()

        viewModel.onAction(SearchUiAction.QueryChanged(""))

        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Idle)
    }
}
