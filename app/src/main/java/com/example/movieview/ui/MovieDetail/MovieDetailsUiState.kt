package com.example.movieview.ui.MovieDetail

import com.example.movieview.model.Movie

data class MovieDetailsUiState(
    val movie: Movie? = null,
    val isLoading:Boolean = false,
    val error: String? = null
)