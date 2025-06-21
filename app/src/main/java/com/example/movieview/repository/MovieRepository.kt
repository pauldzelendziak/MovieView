package com.example.movieview.repository

import com.example.movieview.model.Movie
import com.example.movieview.model.MovieResponse
import com.example.movieview.network.TmdbApiService
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val api: TmdbApiService
) {
    suspend fun getPopularMovies(page: Int) : MovieResponse = api.getPopularMovies(page)
    suspend fun getTrendingMovies(page: Int) : MovieResponse = api.getTrendingMovies(page)
    suspend fun getMovieDetails(movieId: Int) : Movie = api.getMovieDetails(movieId)

}