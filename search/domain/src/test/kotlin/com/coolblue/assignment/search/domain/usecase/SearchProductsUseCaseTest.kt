package com.coolblue.assignment.search.domain.usecase

import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchProductsUseCaseTest {

    private val iphone = Product(
        id = 785359,
        name = "Apple iPhone 6 32GB Grijs",
        imageUrl = "https://image.coolblue.nl/300x750/products/818870",
        salesPriceCents = 26999,
        listPriceCents = null,
        reviewAverage = 9.1,
        reviewCount = 952,
        usps = emptyList(),
        promoText = null,
        nextDayDelivery = true,
    )

    private fun searchProductsOver(vararg pages: List<Product>) = SearchProductsUseCase(
        object : SearchRepository {
            override suspend fun search(query: String, page: Int): AppResult<ProductPage> =
                AppResult.Success(ProductPage(products = pages[page - 1], page = page, pageCount = pages.size))
        },
    )

    @Test
    fun `given page 1 has no product to show, when searching, then page 2 comes back`() = runTest {
        val searchProducts = searchProductsOver(emptyList(), listOf(iphone), listOf(iphone.copy(id = 1)))

        val result = searchProducts("apple", page = 1)

        assertThat(result).isEqualTo(AppResult.Success(ProductPage(listOf(iphone), page = 2, pageCount = 3)))
    }

    @Test
    fun `given no page has a product to show, when searching, then the last page comes back empty`() = runTest {
        val searchProducts = searchProductsOver(emptyList(), emptyList())

        val result = searchProducts("apple", page = 1)

        assertThat(result).isEqualTo(AppResult.Success(ProductPage(emptyList(), page = 2, pageCount = 2)))
    }

    @Test
    fun `given the server answers every page as an empty page 1, when searching, then it stops after page 2`() = runTest {
        val requestedPages = mutableListOf<Int>()
        val searchProducts = SearchProductsUseCase(
            object : SearchRepository {
                override suspend fun search(query: String, page: Int): AppResult<ProductPage> {
                    requestedPages += page
                    check(requestedPages.size <= 5) { "Still searching after pages $requestedPages" }
                    return AppResult.Success(ProductPage(products = emptyList(), page = 1, pageCount = 3))
                }
            },
        )

        searchProducts("apple", page = 1)

        assertThat(requestedPages).containsExactly(1, 2).inOrder()
    }
}
