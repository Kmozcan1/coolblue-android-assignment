package com.coolblue.assignment.search.domain.model

/** One page of search results plus where it sits in the whole result set. */
data class ProductPage(
    val products: List<Product>,
    val page: Int,
    val pageCount: Int,
) {
    /**
     * The page to request next, or null on the last page. Derived from the page count because
     * the API repeats the last page for anything beyond it, so an empty page never comes.
     */
    val nextPage: Int?
        get() = if (page < pageCount) page + 1 else null
}
