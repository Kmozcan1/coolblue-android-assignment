package com.coolblue.assignment.search.data.repository

import android.util.Log
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
        Log.w(TAG, "search(query=$query, page=$page) failed", e)
        AppResult.Failure(AppError.Unknown)
    }
}

private const val TAG = "SearchRepository"
