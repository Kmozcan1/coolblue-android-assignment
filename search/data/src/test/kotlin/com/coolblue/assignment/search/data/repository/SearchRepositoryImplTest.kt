package com.coolblue.assignment.search.data.repository

import com.coolblue.assignment.search.data.api.SearchApi
import com.coolblue.assignment.search.domain.result.AppError
import com.coolblue.assignment.search.domain.result.AppResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class SearchRepositoryImplTest {

    private val server = MockWebServer()
    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repository = SearchRepositoryImpl(retrofit.create<SearchApi>())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `given a real page 1 response, when searching, then 24 products of page 1 of 3 come back`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("search_page1.json")).build())

        val result = repository.search("apple", 1)

        val page = (result as AppResult.Success).data
        assertThat(page.products).hasSize(24)
        assertThat(page.page).isEqualTo(1)
        assertThat(page.pageCount).isEqualTo(3)
        assertThat(page.products.first().name).isEqualTo("Apple iPhone 6 32GB Grijs")
        assertThat(page.products.first().salesPriceCents).isEqualTo(36900)
    }

    @Test
    fun `given the request, then query and page are sent as query parameters`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("search_page1.json")).build())

        repository.search("apple", 2)

        val request = server.takeRequest()
        assertThat(request.url.encodedPath).isEqualTo("/search")
        assertThat(request.url.queryParameter("query")).isEqualTo("apple")
        assertThat(request.url.queryParameter("page")).isEqualTo("2")
    }

    @Test
    fun `given a 500, when searching, then the failure is Server 500`() = runTest {
        server.enqueue(MockResponse.Builder().code(500).build())

        val result = repository.search("apple", 1)

        assertThat(result).isEqualTo(AppResult.Failure(AppError.Server(500)))
    }

    @Test
    fun `given no server, when searching, then the failure is Network`() = runTest {
        server.close()

        val result = repository.search("apple", 1)

        assertThat(result).isEqualTo(AppResult.Failure(AppError.Network))
    }

    @Test
    fun `given a body that is not JSON, when searching, then the failure is Unknown`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body("<html>not json</html>").build())

        val result = repository.search("apple", 1)

        assertThat(result).isEqualTo(AppResult.Failure(AppError.Unknown))
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.getResource("/$name")) { "missing test resource $name" }.readText()
}
