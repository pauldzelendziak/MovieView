package com.example.movieview.ui.MovieList

import com.example.movieview.model.Movie
import com.example.movieview.model.MovieResponse
import com.example.movieview.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class ErrorHandlingTest {

    private lateinit var viewModel: MovieListViewModel
    private lateinit var repository: MovieRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = MovieListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should handle network timeout error`() = runTest {
        coEvery { repository.getPopularMovies(1) } throws SocketTimeoutException("Connection timeout")

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle no internet connection error`() = runTest {
        coEvery { repository.getPopularMovies(1) } throws UnknownHostException("No internet connection")

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle API 404 error`() = runTest {
        val errorResponse =
            Response.error<MovieResponse>(404, okhttp3.ResponseBody.create(null, "Not Found"))
        coEvery { repository.getPopularMovies(1) } throws HttpException(errorResponse)

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle API 500 error`() = runTest {
        val errorResponse = Response.error<MovieResponse>(
            500,
            okhttp3.ResponseBody.create(null, "Internal Server Error")
        )
        coEvery { repository.getPopularMovies(1) } throws HttpException(errorResponse)

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle API 401 unauthorized error`() = runTest {
        val errorResponse =
            Response.error<MovieResponse>(401, okhttp3.ResponseBody.create(null, "Unauthorized"))
        coEvery { repository.getPopularMovies(1) } throws HttpException(errorResponse)

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle API 403 forbidden error`() = runTest {
        val errorResponse =
            Response.error<MovieResponse>(403, okhttp3.ResponseBody.create(null, "Forbidden"))
        coEvery { repository.getPopularMovies(1) } throws HttpException(errorResponse)

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle API rate limit error`() = runTest {
        val errorResponse = Response.error<MovieResponse>(
            429,
            okhttp3.ResponseBody.create(null, "Too Many Requests")
        )
        coEvery { repository.getPopularMovies(1) } throws HttpException(errorResponse)

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle generic IOException`() = runTest {
        coEvery { repository.getPopularMovies(1) } throws IOException("Generic IO error")

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle error during next page loading`() = runTest {
        val initialMovies = listOf(
            Movie(
                id = 1,
                title = "Initial Movie",
                posterPath = "/initial.jpg",
                releaseDate = "2023-01-01",
                voteAverage = 6.5f,
                overview = "An initial movie",
                backdropPath = "/initial_backdrop.jpg"
            )
        )
        val initialResponse = MovieResponse(
            page = 1, 
            results = initialMovies, 
            totalPages = 10, 
            totalResults = 100
        )

        coEvery { repository.getPopularMovies(1) } returns initialResponse
        coEvery { repository.getPopularMovies(2) } throws IOException("Network error on page 2")
        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.first()
        assertEquals(initialMovies, state.movies)
        assertNull(state.errors)
        viewModel.loadNextPage()
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.first()
        assertEquals(initialMovies, state.movies)
        assertEquals(1, state.page)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle error when switching between types`() = runTest {
        val popularMovies = listOf(
            Movie(
                id = 1,
                title = "Popular Movie",
                posterPath = "/popular.jpg",
                releaseDate = "2023-01-01",
                voteAverage = 7.8f,
                overview = "A popular movie",
                backdropPath = "/popular_backdrop.jpg"
            )
        )
        val popularResponse = MovieResponse(
            page = 1, 
            results = popularMovies, 
            totalPages = 10, 
            totalResults = 100
        )

        coEvery { repository.getPopularMovies(1) } returns popularResponse
        coEvery { repository.getTrandingMovies(1) } throws IOException("Network error for trending")

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.first()
        assertEquals(popularMovies, state.movies)
        assertEquals(MovieListType.Popular, state.currentType)
        assertNull(state.errors)

        viewModel.loadMovies(MovieListType.Tranding)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.first()
        assertEquals(MovieListType.Tranding, state.currentType)
        assertTrue(state.movies.isEmpty())
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should clear error when retrying successfully`() = runTest {
        var callCount = 0
        coEvery { repository.getPopularMovies(1) } answers {
            callCount++
            if (callCount == 1) {
                throw IOException("Network error")
            } else {
                MovieResponse(
                    page = 1,
                    results = listOf(
                        Movie(
                            id = 1,
                            title = "Success Movie",
                            posterPath = "/success.jpg",
                            releaseDate = "2023-01-01",
                            voteAverage = 8.5f,
                            overview = "A successful movie",
                            backdropPath = "/backdrop.jpg"
                        )
                    ),
                    totalPages = 10,
                    totalResults = 100
                )
            }
        }
        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.first()
        assertNotNull(state.errors)
        assertTrue(state.movies.isEmpty())
        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.first()
        assertNull(state.errors)
        assertEquals(1, state.movies.size)
        assertEquals("Success Movie", state.movies[0].title)
    }

    @Test
    fun `should handle multiple consecutive errors`() = runTest {
        coEvery { repository.getPopularMovies(1) } throws IOException("Network error")
        repeat(3) {
            viewModel.loadMovies(MovieListType.Popular)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `should handle error during trending movies loading`() = runTest {
        coEvery { repository.getTrandingMovies(1) } throws IOException("Network error for trending")

        viewModel.loadMovies(MovieListType.Tranding)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
        assertEquals(MovieListType.Tranding, state.currentType)
    }
}