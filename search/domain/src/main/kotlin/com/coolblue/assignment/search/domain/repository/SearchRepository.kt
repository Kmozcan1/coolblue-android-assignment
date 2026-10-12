package com.coolblue.assignment.search.domain.repository

import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.result.AppResult

interface SearchRepository {
    suspend fun search(query: String, page: Int): AppResult<ProductPage>
}
