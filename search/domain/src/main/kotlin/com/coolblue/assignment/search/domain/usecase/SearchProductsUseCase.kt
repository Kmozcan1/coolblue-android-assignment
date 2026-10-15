package com.coolblue.assignment.search.domain.usecase

import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import javax.inject.Inject

/** Searches one page of products. A page left with no product to show is skipped, so it never reads as the end of the search. */
class SearchProductsUseCase @Inject constructor(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(query: String, page: Int): AppResult<ProductPage> {
        val result = repository.search(query, page)
        val pageAfterAnEmptyOne = (result as? AppResult.Success)?.data?.takeIf { it.products.isEmpty() }?.nextPage
        return if (pageAfterAnEmptyOne == null) result else invoke(query, pageAfterAnEmptyOne)
    }
}
