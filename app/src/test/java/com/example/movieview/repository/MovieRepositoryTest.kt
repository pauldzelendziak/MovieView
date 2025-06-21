package com.example.movieview.repository

import com.example.movieview.model.Movie
import com.example.movieview.model.MovieResponse
import com.example.movieview.network.TmdbApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryTest {

    private lateinit var repository: MovieRepository
    private lateinit var apiService: TmdbApiService
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        apiService = mockk()
        repository = MovieRepository(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getPopularMovies should return movies when API call succeeds`() = runTest {
        val mockMovies = listOf(
            Movie(id = 1, title = "Test Movie", posterPath = "/test.jpg", releaseDate = "2023-01-01", voteAverage = 7.5f, overview = "Test movie", backdropPath = "/backdrop.jpg")
        )
        val mockResponse = MovieResponse(
            page = 1,
            results = mockMovies,
            totalPages = 10,
            totalResults = 100
        )

        coEvery { apiService.getPopularMovies(1) } returns mockResponse

        val result = repository.getPopularMovies(1)

        assertEquals(mockMovies, result.results)
        assertEquals(1, result.page)
        assertEquals(10, result.totalPages)
        assertEquals(100, result.totalResults)
    }

    @Test
    fun `getTrendingMovies should return movies when API call succeeds`() = runTest {
        val mockMovies = listOf(
            Movie(id = 2, title = "Trending Movie", posterPath = "/trending.jpg", releaseDate = "2023-01-02", voteAverage = 8.0f, overview = "Trending movie", backdropPath = "/trending_backdrop.jpg")
        )
        val mockResponse = MovieResponse(
            page = 1,
            results = mockMovies,
            totalPages = 10,
            totalResults = 100
        )

        coEvery { apiService.getTrendingMovies(1) } returns mockResponse

        val result = repository.getTrandingMovies(1)

        assertEquals(mockMovies, result.results)
        assertEquals(1, result.page)
        assertEquals(10, result.totalPages)
        assertEquals(100, result.totalResults)
    }

    @Test
    fun `getPopularMovies should throw exception when API call fails`() = runTest {
        coEvery { apiService.getPopularMovies(1) } throws IOException("Network error")
        try {
            repository.getPopularMovies(1)
            assert(false) { "Should have thrown IOException" }
        } catch (e: IOException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `getTrendingMovies should throw exception when API call fails`() = runTest {
        coEvery { apiService.getTrendingMovies(1) } throws IOException("Network error")
        try {
            repository.getTrandingMovies(1)
            assert(false) { "Should have thrown IOException" }
        } catch (e: IOException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `getPopularMovies should throw IOException when network error occurs`() = runTest {
        coEvery { apiService.getPopularMovies(1) } throws IOException("Network error")
        try {
            repository.getPopularMovies(1)
            assert(false) { "Should have thrown IOException" }
        } catch (e: IOException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `getTrendingMovies should throw IOException when network error occurs`() = runTest {
        coEvery { apiService.getTrendingMovies(1) } throws IOException("Network error")
        try {
            repository.getTrandingMovies(1)
            assert(false) { "Should have thrown IOException" }
        } catch (e: IOException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `getPopularMovies should throw HttpException when API returns error`() = runTest {
        val errorResponse = Response.error<MovieResponse>(404, okhttp3.ResponseBody.create(null, "Not Found"))
        coEvery { apiService.getPopularMovies(1) } throws HttpException(errorResponse)
        try {
            repository.getPopularMovies(1)
            assert(false) { "Should have thrown HttpException" }
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }

    @Test
    fun `getTrendingMovies should throw HttpException when API returns error`() = runTest {
        val errorResponse = Response.error<MovieResponse>(500, okhttp3.ResponseBody.create(null, "Internal Server Error"))
        coEvery { apiService.getTrendingMovies(1) } throws HttpException(errorResponse)
        try {
            repository.getTrandingMovies(1)
            assert(false) { "Should have thrown HttpException" }
        } catch (e: HttpException) {
            assertEquals(500, e.code())
        }
    }

    @Test
    fun `getPopularMovies should handle empty response`() = runTest {
        val emptyResponse = MovieResponse(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0
        )

        coEvery { apiService.getPopularMovies(1) } returns emptyResponse

        val result = repository.getPopularMovies(1)

        assertEquals(emptyResponse, result)
        assertTrue(result.results.isEmpty())
        assertEquals(0, result.totalResults)
    }

    @Test
    fun `getTrendingMovies should handle empty response`() = runTest {
        val emptyResponse = MovieResponse(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0
        )

        coEvery { apiService.getTrendingMovies(1) } returns emptyResponse

        val result = repository.getTrandingMovies(1)

        assertEquals(emptyResponse, result)
        assertTrue(result.results.isEmpty())
        assertEquals(0, result.totalResults)
    }

    @Test
    fun `getPopularMovies should handle movies with null values`() = runTest {
        val moviesWithNulls = listOf(
            Movie(id = 1, title = null, posterPath = null, releaseDate = null, voteAverage = null, overview = null, backdropPath = null),
            Movie(id = 2, title = "Valid Movie", posterPath = "/valid.jpg", releaseDate = "2023-01-01", voteAverage = 7.0f, overview = "Some overview", backdropPath = "/backdrop.jpg")
        )
        val mockResponse = MovieResponse(
            page = 1,
            results = moviesWithNulls,
            totalPages = 5,
            totalResults = 50
        )

        coEvery { apiService.getPopularMovies(1) } returns mockResponse

        val result = repository.getPopularMovies(1)

        assertEquals(mockResponse, result)
        assertEquals(2, result.results.size)
        assertNull(result.results[0].title)
        assertNotNull(result.results[1].title)
    }

    @Test
    fun `getTrendingMovies should handle movies with null values`() = runTest {
        val moviesWithNulls = listOf(
            Movie(id = 1, title = null, posterPath = null, releaseDate = null, voteAverage = null, overview = null, backdropPath = null),
            Movie(id = 2, title = "Valid Movie", posterPath = "/valid.jpg", releaseDate = "2023-01-01", voteAverage = 7.0f, overview = "Some overview", backdropPath = "/backdrop.jpg")
        )
        val mockResponse = MovieResponse(
            page = 1,
            results = moviesWithNulls,
            totalPages = 5,
            totalResults = 50
        )

        coEvery { apiService.getTrendingMovies(1) } returns mockResponse

        val result = repository.getTrandingMovies(1)

        assertEquals(mockResponse, result)
        assertEquals(2, result.results.size)
        assertNull(result.results[0].title)
        assertNotNull(result.results[1].title)
    }

    @Test
    fun `getPopularMovies should handle different page numbers`() = runTest {
        val mockResponse = MovieResponse(
            page = 5,
            results = listOf(Movie(id = 1, title = "Page 5 Movie", posterPath = "/test.jpg", releaseDate = "2023-01-01", voteAverage = 7.0f, overview = "Page 5 overview", backdropPath = "/backdrop5.jpg")),
            totalPages = 10,
            totalResults = 100
        )

        coEvery { apiService.getPopularMovies(5) } returns mockResponse

        val result = repository.getPopularMovies(5)

        assertEquals(5, result.page)
        assertEquals(1, result.results.size)
    }

    @Test
    fun `getTrendingMovies should handle different page numbers`() = runTest {
        val mockResponse = MovieResponse(
            page = 10,
            results = listOf(Movie(id = 1, title = "Page 10 Movie", posterPath = "/test.jpg", releaseDate = "2023-01-01", voteAverage = 8.0f, overview = "Page 10 overview", backdropPath = "/backdrop10.jpg")),
            totalPages = 20,
            totalResults = 200
        )

        coEvery { apiService.getTrendingMovies(10) } returns mockResponse

        val result = repository.getTrandingMovies(10)

        assertEquals(10, result.page)
        assertEquals(1, result.results.size)
    }
} 