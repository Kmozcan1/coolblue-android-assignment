package com.coolblue.assignment.search.domain.usecase

import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import com.coolblue.assignment.search.domain.result.AppError
import com.coolblue.assignment.search.domain.result.AppResult
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchProductsUseCaseTest {

    private val repository: SearchRepository = mockk()
    private val useCase = SearchProductsUseCase(repository)

    private val iphone = product(1, "Apple iPhone X 64GB Space Gray", usps = listOf("64 GB opslagcapaciteit"))
    private val ipad = product(2, "Apple iPad (2018) 128GB Wifi", usps = listOf("9,7 inch Retina scherm"))
    private val cover = product(3, "Leather Back Cover Zwart", usps = listOf("Geschikt voor iPhone X"))

    private fun product(id: Int, name: String, usps: List<String>) = Product(
        id = id, name = name, imageUrl = "", salesPriceCents = 100, listPriceCents = null,
        reviewAverage = 9.0, reviewCount = 1, usps = usps, promoText = null, nextDayDelivery = false,
    )

    private fun page(products: List<Product>, page: Int = 1, pageCount: Int = 1) =
        AppResult.Success(ProductPage(products = products, page = page, pageCount = pageCount))

    @Test
    fun `given a word in some names, when searching, then products without it are dropped`() = runTest {
        coEvery { repository.search("ipad", 1) } returns page(listOf(iphone, ipad, cover))

        val result = useCase("ipad", 1) as AppResult.Success

        assertThat(result.data.products).containsExactly(ipad)
    }

    @Test
    fun `given a word that only appears in a USP, when searching, then that product is kept`() = runTest {
        coEvery { repository.search("geschikt", 1) } returns page(listOf(iphone, ipad, cover))

        val result = useCase("geschikt", 1) as AppResult.Success

        assertThat(result.data.products).containsExactly(cover)
    }

    @Test
    fun `given two words, when searching, then every word must match, ignoring case`() = runTest {
        coEvery { repository.search("IPHONE 64gb", 1) } returns page(listOf(iphone, ipad, cover))

        val result = useCase("IPHONE 64gb", 1) as AppResult.Success

        assertThat(result.data.products).containsExactly(iphone)
    }

    @Test
    fun `given a blank query, when searching, then every product is kept`() = runTest {
        coEvery { repository.search("  ", 1) } returns page(listOf(iphone, ipad, cover))

        val result = useCase("  ", 1) as AppResult.Success

        assertThat(result.data.products).containsExactly(iphone, ipad, cover).inOrder()
    }

    @Test
    fun `given page 1 has no match and page 2 has, when searching page 1, then page 2 comes back`() = runTest {
        coEvery { repository.search("ipad", 1) } returns page(listOf(iphone, cover), page = 1, pageCount = 3)
        coEvery { repository.search("ipad", 2) } returns page(listOf(ipad), page = 2, pageCount = 3)

        val result = useCase("ipad", 1) as AppResult.Success

        assertThat(result.data.products).containsExactly(ipad)
        assertThat(result.data.page).isEqualTo(2)
        assertThat(result.data.nextPage).isEqualTo(3)
    }

    @Test
    fun `given no page matches, when searching, then the last page comes back empty`() = runTest {
        coEvery { repository.search("samsung", 1) } returns page(listOf(iphone), page = 1, pageCount = 2)
        coEvery { repository.search("samsung", 2) } returns page(listOf(ipad), page = 2, pageCount = 2)

        val result = useCase("samsung", 1) as AppResult.Success

        assertThat(result.data.products).isEmpty()
        assertThat(result.data.nextPage).isNull()
        coVerify(exactly = 1) { repository.search("samsung", 2) }
    }

    @Test
    fun `given the repository fails, when searching, then the failure is passed through unchanged`() = runTest {
        coEvery { repository.search("ipad", 1) } returns AppResult.Failure(AppError.Network)

        val result = useCase("ipad", 1)

        assertThat(result).isEqualTo(AppResult.Failure(AppError.Network))
    }
}
