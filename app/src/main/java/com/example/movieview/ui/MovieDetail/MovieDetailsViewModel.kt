package com.example.movieview.ui.MovieDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieview.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val respository: MovieRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieDetailsUiState())
    val uiState: StateFlow<MovieDetailsUiState> = _uiState

    fun loadMovie(movieId: Int) {
        viewModelScope.launch {
            _uiState.value = MovieDetailsUiState(isLoading = true)
            try {
                val movie = respository.getMovieDetails(movieId)
                _uiState.value = MovieDetailsUiState(movie = movie)
            } catch (e: Exception) {
                _uiState.value = MovieDetailsUiState(error = e.message)
            }
        }
    }
}