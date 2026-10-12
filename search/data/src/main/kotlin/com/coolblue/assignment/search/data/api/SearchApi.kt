package com.coolblue.assignment.search.data.api

import retrofit2.http.GET
import retrofit2.http.Query

internal interface SearchApi {
    @GET("search")
    suspend fun search(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): SearchResponseDto
}
