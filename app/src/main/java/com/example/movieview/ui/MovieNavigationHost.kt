package com.example.movieview.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.movieview.ui.MovieDetail.MovieDetailScreen
import com.example.movieview.ui.MovieDetail.MovieDetailsViewModel
import com.example.movieview.ui.MovieList.MovieListScreen
import com.example.movieview.ui.MovieList.MovieListViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.util.Log

@Composable
fun MovieNavigationHost(
){
    val navController = rememberNavController()
    val movieListViewModel: MovieListViewModel = hiltViewModel()
    val movieDetailsViewModel: MovieDetailsViewModel = hiltViewModel()
    NavHost(navController, startDestination = "list") {
        composable("list") {
            val uiState by movieListViewModel.uiState.collectAsState()
            
            // Debug logging for state collection
            LaunchedEffect(uiState.movies.size) {
                Log.d("NavigationHost", "UI State updated: movies=${uiState.movies.size}, loading=${uiState.isLoading}, loadingNext=${uiState.isLoadingNextPage}")
            }
            
            MovieListScreen(
                uiState = uiState,
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onLoadingNextPage = { movieListViewModel.loadNextPage() },
                onRetry = { movieListViewModel.loadMovies() },
                onTypeChange = { type -> movieListViewModel.loadMovies(type) }
            )
        }
        composable("detail/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull() ?: return@composable
            LaunchedEffect(movieId) { movieDetailsViewModel.loadMovie(movieId) }
            val uiState by movieDetailsViewModel.uiState.collectAsState()
            MovieDetailScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
        }
    }
}