package com.example.movieview.network

import com.example.movieview.model.MovieResponse
import com.example.movieview.model.Movie
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int = 1): MovieResponse

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(@Query("page") page: Int = 1): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(@Path("movie_id") movieId: Int): Movie
} 