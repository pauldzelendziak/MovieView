package com.example.movieview.model

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Float?,
    val overview: String?,
    @SerializedName("backdrop_path") val backdropPath: String?
)

data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
) 