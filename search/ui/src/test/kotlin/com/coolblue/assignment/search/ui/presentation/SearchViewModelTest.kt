package com.coolblue.assignment.search.ui.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.coolblue.assignment.core.domain.result.AppError
import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.core.test.MainDispatcherRule
import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.usecase.SearchProductsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
    fun `given no query, when the screen opens, then it shows Loading and then the whole catalogue`() = runTest {
        coEvery { searchProducts("", 1) } returns page(1)
        val viewModel = viewModel()

        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Loading)
        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.searchedQuery).isEmpty()
        assertThat(loaded.products.map { it.id }).containsExactly(1)
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
            assertThat(awaitItem().content).isEqualTo(SearchContent.Loading)
            viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
            val typed = awaitItem()
            assertThat(typed.query).isEqualTo("iphone")
            assertThat(typed.content).isEqualTo(SearchContent.Loading)
            val loaded = awaitItem().content as SearchContent.Loaded
            assertThat(loaded.products.map { it.id }).containsExactly(1)
            assertThat(loaded.paging).isEqualTo(Paging.Idle(2))
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

        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Empty("nothing"))
    }

    @Test
    fun `given zero results, when the user keeps typing, then the empty message keeps the query that returned nothing`() = runTest {
        coEvery { searchProducts("nothing", 1) } returns page(1, pageCount = 1, products = emptyList())
        coEvery { searchProducts("nothing at all", 1) } returns page(1, pageCount = 1, products = emptyList())
        val viewModel = viewModel()
        viewModel.onAction(SearchUiAction.QueryChanged("nothing"))
        advanceUntilIdle()

        viewModel.onAction(SearchUiAction.QueryChanged("nothing at all"))

        assertThat(viewModel.uiState.value.query).isEqualTo("nothing at all")
        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Empty("nothing"))
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
    fun `given a saved query, when the view model is created, then content is Loading before the search runs`() = runTest {
        coEvery { searchProducts("iphone", 1) } returns page(1)

        val viewModel = viewModel(savedQuery = "iphone")

        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Loading)
    }

    @Test
    fun `given results on screen, when the query is cleared, then the rows stay until the whole catalogue lands`() = runTest {
        coEvery { searchProducts("iphone", 1) } returns page(1)
        coEvery { searchProducts("", 1) } returns page(1, products = listOf(product(7)))
        val viewModel = viewModel()
        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
        advanceUntilIdle()

        viewModel.onAction(SearchUiAction.QueryChanged(""))

        val refreshing = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(refreshing.products.map { it.id }).containsExactly(1)
        assertThat(refreshing.isRefreshing).isTrue()

        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.searchedQuery).isEmpty()
        assertThat(loaded.products.map { it.id }).containsExactly(7)
        assertThat(loaded.isRefreshing).isFalse()
    }

    private fun loadedViewModel(): SearchViewModel {
        coEvery { searchProducts("iphone", 1) } returns page(1)
        val viewModel = viewModel()
        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `given page 1 on screen, when LoadMore, then page 2 is appended and next page advances`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } returns page(2)

        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(1, 2).inOrder()
        assertThat(loaded.paging).isEqualTo(Paging.Idle(3))
    }

    @Test
    fun `given page 2 repeats a row from page 1, when LoadMore, then that row is shown once`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } returns page(2, products = listOf(product(1), product(2)))

        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `given a saved query, when the screen opens, then it is searched without waiting for the debounce`() = runTest {
        coEvery { searchProducts("iphone", 1) } returns page(1)
        viewModel(savedQuery = "iphone")

        runCurrent()

        coVerify(exactly = 1) { searchProducts("iphone", 1) }
    }

    @Test
    fun `given page 1 repeats a row, when it loads, then that row is shown once`() = runTest {
        coEvery { searchProducts("", 1) } returns page(1, products = listOf(product(1), product(1), product(2)))
        val viewModel = viewModel()
        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `given a load more in flight, when the query is edited and edited back, then LoadMore works again`() = runTest {
        coEvery { searchProducts("", 1) } returns page(1)
        coEvery { searchProducts("", 2) } coAnswers { delay(1_000); page(2) }
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onAction(SearchUiAction.LoadMore)
        runCurrent()

        viewModel.onAction(SearchUiAction.QueryChanged("a"))
        runCurrent()
        advanceTimeBy(100)
        viewModel.onAction(SearchUiAction.QueryChanged(""))
        advanceTimeBy(400) // the debounce sends "" again, which cancels the load more

        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).paging).isEqualTo(Paging.Idle(2))
        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).products.map { it.id }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `given the last page on screen, when LoadMore, then nothing is requested`() = runTest {
        coEvery { searchProducts("iphone", 1) } returns page(1, pageCount = 1)
        val viewModel = viewModel()
        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
        advanceUntilIdle()

        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()

        coVerify(exactly = 1) { searchProducts(any(), any()) }
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).paging).isEqualTo(Paging.End)
    }

    @Test
    fun `given a load more in flight, when LoadMore again, then only one request is made`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } coAnswers { delay(1_000); page(2) }

        viewModel.onAction(SearchUiAction.LoadMore)
        advanceTimeBy(100)
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).paging).isEqualTo(Paging.Loading(2))
        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()

        coVerify(exactly = 1) { searchProducts("iphone", 2) }
    }

    @Test
    fun `given load more fails, then rows stay and the error is set, and Retry loads page 2`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } returns AppResult.Failure(AppError.Server(500)) andThen page(2)

        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()
        val failed = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(failed.products.map { it.id }).containsExactly(1)
        assertThat(failed.paging).isEqualTo(Paging.Failed(2, AppError.Server(500)))

        viewModel.onAction(SearchUiAction.LoadMore) // ignored while the error shows
        advanceUntilIdle()
        coVerify(exactly = 1) { searchProducts("iphone", 2) }
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).paging).isEqualTo(Paging.Failed(2, AppError.Server(500)))

        viewModel.onAction(SearchUiAction.Retry)
        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(1, 2).inOrder()
        assertThat(loaded.paging).isEqualTo(Paging.Idle(3))
        coVerify(exactly = 2) { searchProducts("iphone", 2) }
    }

    @Test
    fun `given results on screen, when the query is refined, then the rows stay and refresh until the new rows land`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone 6", 1) } coAnswers { delay(1_000); page(1, products = listOf(product(6))) }

        viewModel.onAction(SearchUiAction.QueryChanged("iphone 6"))
        viewModel.onAction(SearchUiAction.LoadMore) // inside the debounce: page 2 would belong to the new query, ignored
        advanceTimeBy(500) // past the debounce, the request is in flight
        val refreshing = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(refreshing.products.map { it.id }).containsExactly(1)
        assertThat(refreshing.isRefreshing).isTrue()

        viewModel.onAction(SearchUiAction.LoadMore) // in flight: still ignored
        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(6)
        assertThat(loaded.isRefreshing).isFalse()
        coVerify(exactly = 0) { searchProducts(any(), 2) }
    }

    @Test
    fun `given a load more in flight, when the query changes, then it is cancelled and the new query loads`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } coAnswers { delay(1_000); page(2) }
        coEvery { searchProducts("ipad", 1) } returns page(1, products = listOf(product(10)))

        viewModel.onAction(SearchUiAction.LoadMore)
        advanceTimeBy(100)
        viewModel.onAction(SearchUiAction.QueryChanged("ipad"))
        advanceTimeBy(500) // past the debounce, long before the load more would have finished

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(10)
        assertThat(loaded.paging).isEqualTo(Paging.Idle(2))
        advanceUntilIdle() // the cancelled page 2 never lands
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).products.map { it.id }).containsExactly(10)
    }

    @Test
    fun `given the first page is in flight, when the query is cleared, then its late result is dropped`() = runTest {
        coEvery { searchProducts("iphone", 1) } coAnswers { delay(400); page(1) }
        coEvery { searchProducts("", 1) } returns page(1, products = listOf(product(7)))
        val viewModel = viewModel()
        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
        advanceTimeBy(500) // past the debounce, the request is in flight

        viewModel.onAction(SearchUiAction.QueryChanged(""))
        advanceTimeBy(250) // the response has landed, the debounce for the cleared query has not fired yet

        assertThat(viewModel.uiState.value.content).isEqualTo(SearchContent.Loading)
    }

    @Test
    fun `given a load more error, when the query changes and Retry is pressed within the debounce, then no page 2 is requested`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } returns AppResult.Failure(AppError.Network)
        coEvery { searchProducts("ipad", 1) } returns page(1, products = listOf(product(10)))
        coEvery { searchProducts("ipad", 2) } returns page(2, products = listOf(product(20)))
        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()

        viewModel.onAction(SearchUiAction.QueryChanged("ipad"))
        viewModel.onAction(SearchUiAction.Retry)
        advanceUntilIdle()

        coVerify(exactly = 0) { searchProducts("ipad", 2) }
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).products.map { it.id }).containsExactly(10)
    }

    @Test
    fun `given results on screen, when the query is edited and edited back within the debounce, then nothing is fetched and the rows stay`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(SearchUiAction.QueryChanged("iphonex"))
        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
        advanceUntilIdle()

        coVerify(exactly = 1) { searchProducts("iphone", 1) }
        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(1)
        assertThat(loaded.isRefreshing).isFalse()
    }

    @Test
    fun `given results on screen, when the same query is reported again, then it is not refreshing`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))

        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).isRefreshing).isFalse()
    }

    @Test
    fun `given results on screen, when Refresh, then the rows stay until page 1 is fetched again and replaces them`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 1) } returns page(1, products = listOf(product(5)))

        viewModel.onAction(SearchUiAction.Refresh)

        val refreshing = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(refreshing.products.map { it.id }).containsExactly(1)
        assertThat(refreshing.isRefreshing).isTrue()

        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(5)
        assertThat(loaded.isRefreshing).isFalse()
        coVerify(exactly = 2) { searchProducts("iphone", 1) }
    }

    @Test
    fun `given a refresh fails, then the rows stay with the error until it has been shown`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 1) } returns AppResult.Failure(AppError.Network)

        viewModel.onAction(SearchUiAction.Refresh)
        advanceUntilIdle()

        val failed = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(failed.products.map { it.id }).containsExactly(1)
        assertThat(failed.isRefreshing).isFalse()
        assertThat(failed.refreshFailure?.error).isEqualTo(AppError.Network)

        viewModel.onAction(SearchUiAction.RefreshFailureShown)

        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).refreshFailure).isNull()
    }

    @Test
    fun `given a refresh fails twice with the same error, then the second failure is a new value`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 1) } returns AppResult.Failure(AppError.Network)

        viewModel.onAction(SearchUiAction.Refresh)
        advanceUntilIdle()
        val first = (viewModel.uiState.value.content as SearchContent.Loaded).refreshFailure
        viewModel.onAction(SearchUiAction.Refresh)
        advanceUntilIdle()
        val second = (viewModel.uiState.value.content as SearchContent.Loaded).refreshFailure

        assertThat(second).isNotEqualTo(first)
    }

    @Test
    fun `given a refresh in flight, when the query is edited and edited back, then the refresh still lands`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 1) } coAnswers { delay(1_000); page(1, products = listOf(product(5))) }

        viewModel.onAction(SearchUiAction.Refresh)
        runCurrent()
        viewModel.onAction(SearchUiAction.QueryChanged("iphonex"))
        runCurrent()
        advanceTimeBy(100)
        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))

        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).isRefreshing).isTrue()
        advanceUntilIdle()
        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(5)
        assertThat(loaded.isRefreshing).isFalse()
    }

    @Test
    fun `given a refined query is loading, when Refresh, then only the refined query is fetched`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone 6", 1) } coAnswers { delay(1_000); page(1, products = listOf(product(6))) }

        viewModel.onAction(SearchUiAction.QueryChanged("iphone 6"))
        advanceTimeBy(500) // past the debounce, the request is in flight
        viewModel.onAction(SearchUiAction.Refresh)
        advanceUntilIdle()

        coVerify(exactly = 1) { searchProducts("iphone", 1) }
        coVerify(exactly = 1) { searchProducts("iphone 6", 1) }
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).products.map { it.id }).containsExactly(6)
    }

    @Test
    fun `given a load more in flight, when Refresh, then it is cancelled and page 1 replaces the rows`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } coAnswers { delay(1_000); page(2) }
        coEvery { searchProducts("iphone", 1) } returns page(1, products = listOf(product(5)))
        viewModel.onAction(SearchUiAction.LoadMore)
        advanceTimeBy(100)

        viewModel.onAction(SearchUiAction.Refresh)
        runCurrent()
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).paging).isEqualTo(Paging.Idle(2)) // no skeleton while it refreshes
        advanceUntilIdle() // the cancelled page 2 never lands

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.products.map { it.id }).containsExactly(5)
        assertThat(loaded.paging).isEqualTo(Paging.Idle(2))
    }

    @Test
    fun `given a refresh in flight, when the query changes before it lands, then its result is dropped`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 1) } coAnswers { delay(100); page(1, products = listOf(product(5))) }
        coEvery { searchProducts("ipad", 1) } returns page(1, products = listOf(product(10)))

        viewModel.onAction(SearchUiAction.Refresh)
        advanceTimeBy(600)
        viewModel.onAction(SearchUiAction.QueryChanged("ipad"))
        advanceTimeBy(250) // the refresh has landed (held to 800 ms), the debounce for ipad has not fired yet

        val waiting = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(waiting.products.map { it.id }).containsExactly(1)
        assertThat(waiting.isRefreshing).isTrue()

        advanceUntilIdle()
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).products.map { it.id }).containsExactly(10)
    }

    @Test
    fun `given a load more error, when a refresh fails, then the error is gone and LoadMore can ask again`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } returns AppResult.Failure(AppError.Server(500))
        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()
        coEvery { searchProducts("iphone", 1) } returns AppResult.Failure(AppError.Network)

        viewModel.onAction(SearchUiAction.Refresh)
        advanceUntilIdle()

        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).paging).isEqualTo(Paging.Idle(2))
    }

    @Test
    fun `given a load more error, when the query is edited and edited back, then the error stays`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } returns AppResult.Failure(AppError.Network)
        viewModel.onAction(SearchUiAction.LoadMore)
        advanceUntilIdle()

        viewModel.onAction(SearchUiAction.QueryChanged("iphonex"))
        advanceTimeBy(100)
        viewModel.onAction(SearchUiAction.QueryChanged("iphone"))
        advanceUntilIdle()

        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).paging).isEqualTo(Paging.Failed(2, AppError.Network))
    }

    @Test
    fun `given a load more not yet started, when Refresh, then page 1 replaces it and the refresh ends`() = runTest {
        val viewModel = loadedViewModel()
        coEvery { searchProducts("iphone", 2) } returns page(2)
        coEvery { searchProducts("iphone", 1) } returns page(1, products = listOf(product(10)))

        viewModel.onAction(SearchUiAction.LoadMore)
        viewModel.onAction(SearchUiAction.Refresh) // before the load more request has been picked up
        advanceUntilIdle()

        val loaded = viewModel.uiState.value.content as SearchContent.Loaded
        assertThat(loaded.isRefreshing).isFalse()
        assertThat(loaded.products.map { it.id }).containsExactly(10)
        coVerify(exactly = 0) { searchProducts("iphone", 2) }
    }

    @Test
    fun `given a refresh that answers at once, then it shows as refreshing long enough to be seen`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(SearchUiAction.Refresh)
        advanceTimeBy(500)

        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).isRefreshing).isTrue()
        advanceUntilIdle()
        assertThat((viewModel.uiState.value.content as SearchContent.Loaded).isRefreshing).isFalse()
    }
}
