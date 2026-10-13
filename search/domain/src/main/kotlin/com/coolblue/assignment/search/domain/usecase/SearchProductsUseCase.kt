package com.coolblue.assignment.search.domain.usecase

import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import javax.inject.Inject

/** Searches one page of products. */
class SearchProductsUseCase @Inject constructor(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(query: String, page: Int): AppResult<ProductPage> =
        repository.search(query, page)
}
