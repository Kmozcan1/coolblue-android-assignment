package com.coolblue.assignment.search.domain.usecase

import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import com.coolblue.assignment.search.domain.result.AppResult
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchProductsUseCaseTest {

    private val repository: SearchRepository = mockk()
    private val useCase = SearchProductsUseCase(repository)

    @Test
    fun `given the repository answers, when invoked, then the answer is passed through unchanged`() = runTest {
        val page = ProductPage(products = emptyList(), page = 2, pageCount = 3)
        coEvery { repository.search("iphone", 2) } returns AppResult.Success(page)

        val result = useCase("iphone", 2)

        assertThat(result).isEqualTo(AppResult.Success(page))
    }
}
