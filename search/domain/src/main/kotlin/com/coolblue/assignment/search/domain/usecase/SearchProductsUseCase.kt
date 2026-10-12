package com.coolblue.assignment.search.domain.usecase

import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import com.coolblue.assignment.search.domain.result.AppResult
import javax.inject.Inject

/**
 * Searches one page of products. Pass-through today; this is where validation, caching or a
 * second data source would go without the ViewModel changing.
 */
class SearchProductsUseCase @Inject constructor(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(query: String, page: Int): AppResult<ProductPage> =
        repository.search(query, page)
}
