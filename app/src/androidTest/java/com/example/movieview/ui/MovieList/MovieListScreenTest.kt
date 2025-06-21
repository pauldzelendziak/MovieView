package com.example.movieview.ui.MovieList

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.movieview.model.Movie
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.hasTestTag

@RunWith(AndroidJUnit4::class)
class MovieListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initialState_showsCorrectly() {
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(
                    movies = emptyList(),
                    isLoading = false,
                    errors = null
                ),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = {}
            )
        }
        composeTestRule.onNodeWithText("Популярні").assertExists()
        composeTestRule.onNodeWithText("Трендові").assertExists()
        composeTestRule.onNodeWithText("Movie 1").assertDoesNotExist()
    }

    @Test
    fun moviesLoaded_areDisplayed() {
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(
                    movies = getFakeMovies(2),
                    isLoading = false,
                    errors = null
                ),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = {}
            )
        }

        composeTestRule.onNodeWithText("Movie 1").assertExists()
        composeTestRule.onNodeWithText("Movie 2").assertExists()
    }

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(isLoading = true),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = {}
            )
        }

        composeTestRule.onNode(isRoot()).printToLog("LoadingState")
        composeTestRule.onNode(hasTestTag("DefaultCircularProgressIndicator"), useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun errorState_showsErrorAndRetryButton() {
        val errorMessage = "Network Error"
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(errors = errorMessage, movies = emptyList()),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = {}
            )
        }

        composeTestRule.onNodeWithText("Помилка : $errorMessage").assertExists()
        composeTestRule.onNodeWithText("Повторити").assertExists()
    }

    @Test
    fun retryButton_callsOnRetry() {
        val onRetryClicked = mutableStateOf(false)
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(errors = "Error", movies = emptyList()),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = { onRetryClicked.value = true },
                onTypeChange = {}
            )
        }

        composeTestRule.onNodeWithText("Повторити").performClick()
        assert(onRetryClicked.value)
    }

    @Test
    fun pagination_loadsMoreItems() {
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(
                    movies = getFakeMovies(20),
                    isLoadingNextPage = true
                ),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = {}
            )
        }
        composeTestRule.onNodeWithText("Movie 20").performScrollTo()
        composeTestRule.onNode(hasTestTag("DefaultCircularProgressIndicator"), useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun endOfList_showsEndMessage() {
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(
                    movies = getFakeMovies(20),
                    endReach = true
                ),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = {}
            )
        }

        composeTestRule.onNodeWithText("Досягнуто кінця").assertExists()
    }

    @Test
    fun movieClick_triggersCallback() {
        val clickedMovieId = mutableStateOf<Int?>(null)
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(movies = getFakeMovies(1)),
                onMovieClick = { clickedMovieId.value = it },
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = {}
            )
        }
        composeTestRule.onNodeWithText("Movie 1").performClick()
        assert(clickedMovieId.value == 1)
    }

    @Test
    fun buttonClick_changesListType() {
        val onTypeChangeClicked = mutableStateOf(MovieListType.Popular)
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(
                    movies = getFakeMovies(1),
                    currentType = onTypeChangeClicked.value
                ),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = { onTypeChangeClicked.value = it }
            )
        }

        composeTestRule.onNodeWithText("Трендові").performClick()
        assert(onTypeChangeClicked.value == MovieListType.Trending)

        composeTestRule.onNodeWithText("Популярні").performClick()
        assert(onTypeChangeClicked.value == MovieListType.Popular)
    }

    @Test
    fun noErrorShown_whenNoErrorState() {
        composeTestRule.setContent {
            MovieListScreen(
                uiState = MovieListUiState(
                    movies = getFakeMovies(1),
                    errors = null
                ),
                onMovieClick = {},
                onLoadingNextPage = {},
                onRetry = {},
                onTypeChange = {}
            )
        }
        composeTestRule.onNodeWithText("Помилка").assertDoesNotExist()
    }

    private fun getFakeMovies(count: Int): List<Movie> {
        return (1..count).map {
            Movie(
                id = it,
                title = "Movie $it",
                posterPath = "/path$it.jpg",
                releaseDate = "2023-01-01",
                voteAverage = 7.0f,
                overview = "Overview $it",
                backdropPath = "/backdrop$it.jpg"
            )
        }
    }
} 