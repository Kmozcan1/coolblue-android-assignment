package com.coolblue.assignment.search.domain.usecase

import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import javax.inject.Inject

/**
 * Searches one page of products.
 *
 * The assignment's API is mocked and returns the same catalogue for every search term, so the
 * searching happens here: a product matches when its name or one of its USPs contains every word
 * of the query. A page that filters down to nothing is skipped, so the caller always gets rows
 * or the last page, never a blank page with more behind it. With a backend that searches, this
 * class goes back to a single `repository.search(query, page)` call and nothing else changes.
 */
class SearchProductsUseCase @Inject constructor(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(query: String, page: Int): AppResult<ProductPage> {
        var pageToFetch = page
        while (true) {
            val result = repository.search(query, pageToFetch)
            if (result !is AppResult.Success) return result
            val filtered = result.data.filterBy(query)
            val nextPage = filtered.nextPage
            if (filtered.products.isNotEmpty() || nextPage == null) return AppResult.Success(filtered)
            pageToFetch = nextPage
        }
    }
}

private fun ProductPage.filterBy(query: String): ProductPage {
    val words = query.split(' ').filter { it.isNotBlank() }
    return copy(products = products.filter { product -> words.all { product.matches(it) } })
}

private fun Product.matches(word: String): Boolean =
    name.contains(word, ignoreCase = true) || usps.any { it.contains(word, ignoreCase = true) }
