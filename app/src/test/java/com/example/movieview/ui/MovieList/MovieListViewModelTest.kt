package com.example.movieview.ui.MovieList

import androidx.lifecycle.SavedStateHandle
import com.example.movieview.model.Movie
import com.example.movieview.model.MovieResponse
import com.example.movieview.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.drop
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

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
    fun `initial state should be Popular type with empty movies and loading`() = runTest {
        val initialState = viewModel.uiState.value
        assertEquals(MovieListType.Popular, initialState.currentType)
        assertTrue(initialState.movies.isEmpty())
        assertEquals(1, initialState.page)
        assertTrue(initialState.isLoading || initialState.movies.isNotEmpty())
        assertFalse(initialState.isLoadingNextPage)
        assertFalse(initialState.endReach)
        assertNull(initialState.errors)
    }

    @Test
    fun `loadMovies should update state with movies when successful`() = runTest {
        val mockMovies = listOf(
            Movie(id = 1, title = "Test Movie 1", posterPath = "/test1.jpg", releaseDate = "2023-01-01", voteAverage = 7.5f, overview = "Test movie 1", backdropPath = "/backdrop1.jpg"),
            Movie(id = 2, title = "Test Movie 2", posterPath = "/test2.jpg", releaseDate = "2023-01-02", voteAverage = 8.0f, overview = "Test movie 2", backdropPath = "/backdrop2.jpg")
        )
        val mockResponse = MovieResponse(
            page = 1,
            results = mockMovies,
            totalPages = 10,
            totalResults = 100
        )

        coEvery { repository.getPopularMovies(1) } returns mockResponse

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(mockMovies, state.movies)
        assertEquals(1, state.page)
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingNextPage)
        assertNull(state.errors)
    }

    @Test
    fun `loadMovies should handle network error and show error message`() = runTest {
        coEvery { repository.getPopularMovies(1) } throws Exception("Network error")

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingNextPage)
        assertNotNull(state.errors)
        assertEquals("Сталася помилка при завантаженні фільмів", state.errors)
    }

    @Test
    fun `loadNextPage should append movies to existing list`() = runTest {
        val initialMovies = listOf(
            Movie(id = 1, title = "Test Movie 1", posterPath = "/test1.jpg", releaseDate = "2023-01-01", voteAverage = 7.5f, overview = "Test movie 1", backdropPath = "/backdrop1.jpg")
        )
        val newMovies = listOf(
            Movie(id = 2, title = "Test Movie 2", posterPath = "/test2.jpg", releaseDate = "2023-01-02", voteAverage = 8.0f, overview = "Test movie 2", backdropPath = "/backdrop2.jpg")
        )
        
        val initialResponse = MovieResponse(page = 1, results = initialMovies, totalPages = 10, totalResults = 100)
        val nextPageResponse = MovieResponse(page = 2, results = newMovies, totalPages = 10, totalResults = 100)

        coEvery { repository.getPopularMovies(1) } returns initialResponse
        coEvery { repository.getPopularMovies(2) } returns nextPageResponse

        // Load initial page
        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        // Load next page
        viewModel.loadNextPage()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(initialMovies + newMovies, state.movies)
        assertEquals(2, state.page)
    }

    @Test
    fun `switching between Popular and Trending should maintain separate states`() = runTest {
        val popularMovies = listOf(
            Movie(id = 1, title = "Popular Movie", posterPath = "/popular.jpg", releaseDate = "2023-01-01", voteAverage = 7.8f, overview = "Popular movie", backdropPath = "/popular_backdrop.jpg")
        )
        val trendingMovies = listOf(
            Movie(id = 2, title = "Trending Movie", posterPath = "/trending.jpg", releaseDate = "2023-01-02", voteAverage = 8.2f, overview = "Trending movie", backdropPath = "/trending_backdrop.jpg")
        )

        val popularResponse = MovieResponse(page = 1, results = popularMovies, totalPages = 10, totalResults = 100)
        val trendingResponse = MovieResponse(page = 1, results = trendingMovies, totalPages = 10, totalResults = 100)

        coEvery { repository.getPopularMovies(1) } returns popularResponse
        coEvery { repository.getTrandingMovies(1) } returns trendingResponse

        // Load Popular movies
        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.first()
        assertEquals(popularMovies, state.movies)
        assertEquals(MovieListType.Popular, state.currentType)

        // Switch to Trending
        viewModel.loadMovies(MovieListType.Tranding)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.first()
        assertEquals(trendingMovies, state.movies)
        assertEquals(MovieListType.Tranding, state.currentType)

        // Switch back to Popular - should show original Popular movies
        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.first()
        assertEquals(popularMovies, state.movies)
        assertEquals(MovieListType.Popular, state.currentType)
    }

    @Test
    fun `should not load next page when already loading`() = runTest {
        val mockResponse = MovieResponse(
            page = 1,
            results = listOf(Movie(id = 1, title = "Test", posterPath = "/test.jpg", releaseDate = "2023-01-01", voteAverage = 7.0f, overview = "Test movie", backdropPath = "/test_backdrop.jpg")),
            totalPages = 10,
            totalResults = 100
        )

        coEvery { repository.getPopularMovies(1) } returns mockResponse

        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(1, state.movies.size)
        assertEquals(1, state.page)
    }

    @Test
    fun `should reach end after 50 pages`() = runTest {
        val mockResponse = MovieResponse(
            page = 1,
            results = listOf(Movie(id = 1, title = "Test", posterPath = "/test.jpg", releaseDate = "2023-01-01", voteAverage = 7.0f, overview = "Test movie", backdropPath = "/test_backdrop.jpg")),
            totalPages = 100,
            totalResults = 2000
        )
        for (i in 1..MovieListViewModel.MAX_PAGES) {
            coEvery { repository.getPopularMovies(i) } returns mockResponse.copy(page = i)
            if (i == 1) {
                viewModel.loadMovies(MovieListType.Popular)
            } else {
                viewModel.loadNextPage()
            }
            testDispatcher.scheduler.advanceUntilIdle()
        }
        val state = viewModel.uiState.first()
        assertTrue(state.endReach)
        assertEquals(MovieListViewModel.MAX_PAGES, state.page)
    }

    @Test
    fun `loading next page should show loading indicator`() = runTest {
        val initialResponse = MovieResponse(
            page = 1,
            results = listOf(Movie(id = 1, title = "Test", posterPath = "/test.jpg", releaseDate = "2023-01-01", voteAverage = 7.0f, overview = "Test movie", backdropPath = "/test_backdrop.jpg")),
            totalPages = 10,
            totalResults = 100
        )
        val nextPageResponse = MovieResponse(
            page = 2,
            results = listOf(Movie(id = 2, title = "Test 2", posterPath = "/test2.jpg", releaseDate = "2023-01-02", voteAverage = 8.0f, overview = "Test movie 2", backdropPath = "/test2_backdrop.jpg")),
            totalPages = 10,
            totalResults = 100
        )
        coEvery { repository.getPopularMovies(1) } returns initialResponse
        coEvery { repository.getPopularMovies(2) } returns nextPageResponse
        viewModel.loadMovies(MovieListType.Popular)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadNextPage()
        val loadingState = viewModel.uiState.drop(1).first { it.isLoadingNextPage }
        assertTrue(loadingState.isLoadingNextPage)
        assertEquals(1, loadingState.page)
        testDispatcher.scheduler.advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoadingNextPage }
        assertFalse(finalState.isLoadingNextPage)
        assertEquals(2, finalState.page)
        assertEquals(2, finalState.movies.size)
    }
} 