package com.coolblue.assignment.search.data.repository

import com.coolblue.assignment.core.domain.result.AppError
import com.coolblue.assignment.core.domain.result.AppResult
import com.coolblue.assignment.search.data.api.SearchApi
import com.coolblue.assignment.search.data.mapper.toDomain
import com.coolblue.assignment.search.domain.model.ProductPage
import com.coolblue.assignment.search.domain.repository.SearchRepository
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Retrofit suspend calls are already main-safe (they run on OkHttp's own threads), so there is
 * no dispatcher switch here. Network and HTTP failures get their own cases; anything else that
 * goes wrong (a body that will not parse, a surprise from the stack) is Unknown rather than a
 * crash. CancellationException is rethrown so coroutine cancellation keeps working.
 */
internal class SearchRepositoryImpl @Inject constructor(
    private val api: SearchApi,
) : SearchRepository {

    override suspend fun search(query: String, page: Int): AppResult<ProductPage> = try {
        AppResult.Success(api.search(query, page).toDomain())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        AppResult.Failure(AppError.Network)
    } catch (e: HttpException) {
        AppResult.Failure(AppError.Server(e.code()))
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown)
    }
}
