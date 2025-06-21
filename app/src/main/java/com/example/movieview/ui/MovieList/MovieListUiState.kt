package com.example.movieview.ui.MovieList

import com.example.movieview.model.Movie

data class MovieListUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val errors: String? = null,
    val page: Int = 49,
    val totalPages: Int = 0,
    val endReach: Boolean = false,
    val currentType: MovieListType = MovieListType.Popular

)
