package com.coolblue.assignment.search.domain.repository

import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.search.domain.model.ProductPage

interface SearchRepository {
    suspend fun search(query: String, page: Int): AppResult<ProductPage>
}
