package com.coolblue.assignment.search.data.repository

import com.coolblue.assignment.search.data.api.SearchApi
import com.coolblue.assignment.search.data.mapper.toDomain
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import com.coolblue.assignment.search.domain.result.AppError
import com.coolblue.assignment.search.domain.result.AppResult
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit suspend calls are already main-safe (they run on OkHttp's own threads), so there is
 * no dispatcher switch here. Only the three expected failures are caught; anything else,
 * including CancellationException, keeps propagating so coroutine cancellation works.
 */
@Singleton
internal class SearchRepositoryImpl @Inject constructor(
    private val api: SearchApi,
) : SearchRepository {

    override suspend fun search(query: String, page: Int): AppResult<ProductPage> = try {
        AppResult.Success(api.search(query, page).toDomain())
    } catch (e: IOException) {
        AppResult.Failure(AppError.Network)
    } catch (e: HttpException) {
        AppResult.Failure(AppError.Server(e.code()))
    } catch (e: SerializationException) {
        AppResult.Failure(AppError.Unknown)
    }
}
