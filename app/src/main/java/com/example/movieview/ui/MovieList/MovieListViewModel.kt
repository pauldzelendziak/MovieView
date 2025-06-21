package com.example.movieview.ui.MovieList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieview.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    companion object {
        const val MAX_PAGES = 50
    }

    private val _popularState =
        MutableStateFlow(MovieListUiState(currentType = MovieListType.Popular))
    private val _trendingState =
        MutableStateFlow(MovieListUiState(currentType = MovieListType.Trending))
    private val _currentState = MutableStateFlow(_popularState.value)
    val uiState: StateFlow<MovieListUiState> = _currentState

    private var currentType: MovieListType = MovieListType.Popular

    init {
        loadMovies(MovieListType.Popular)
    }

    fun loadMovies(type: MovieListType = MovieListType.Popular, nextPage: Boolean = false) {
        if (type != currentType) {
            currentType = type
            _currentState.value = when (type) {
                MovieListType.Popular -> _popularState.value
                MovieListType.Trending -> _trendingState.value
            }
        }
        val currentState = when (type) {
            MovieListType.Popular -> _popularState.value
            MovieListType.Trending -> _trendingState.value
        }

        if (currentState.isLoading || currentState.endReach) {
            return
        }

        viewModelScope.launch {
            val updatedState = if (nextPage) {
                currentState.copy(isLoadingNextPage = true)
            } else {
                currentState.copy(isLoading = true, errors = null)
            }
            when (type) {
                MovieListType.Popular -> _popularState.value = updatedState
                MovieListType.Trending -> _trendingState.value = updatedState
            }
            _currentState.value = updatedState

            try {
                val page = if (nextPage) currentState.page + 1 else currentState.page
                if (page < 1) {

                    val errorState = updatedState.copy(
                        isLoading = false,
                        isLoadingNextPage = false,
                        errors = "Невірний номер сторінки"
                    )
                    updateState(type, errorState)
                    return@launch
                }
                val maxPages = MAX_PAGES
                if (page > maxPages) {
                    val endState = updatedState.copy(
                        isLoading = false,
                        isLoadingNextPage = false,
                        endReach = true,
                        page = maxPages
                    )
                    updateState(type, endState)
                    return@launch
                }

                val response = when (type) {
                    MovieListType.Popular -> repository.getPopularMovies(page)
                    MovieListType.Trending -> repository.getTrendingMovies(page)
                }

                val newMovies =
                    if (nextPage) currentState.movies + response.results else response.results

                val newState = updatedState.copy(
                    movies = newMovies,
                    isLoading = false,
                    isLoadingNextPage = false,
                    page = page,
                    totalPages = maxPages,
                    endReach = page >= maxPages
                )

                updateState(type, newState)
            } catch (e: Exception) {
                val errorState = updatedState.copy(
                    isLoading = false,
                    isLoadingNextPage = false,
                    errors = "Сталася помилка при завантаженні фільмів"
                )
                updateState(type, errorState)
            }
        }
    }

    private fun updateState(type: MovieListType, newState: MovieListUiState) {
        when (type) {
            MovieListType.Popular -> _popularState.value = newState
            MovieListType.Trending -> _trendingState.value = newState
        }
        if (type == currentType) {
            _currentState.value = newState
        }
    }

    fun loadNextPage() {
        loadMovies(currentType, nextPage = true)
    }
}