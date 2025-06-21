package com.example.movieview.ui.MovieDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun MovieDetailScreen(
    uiState: MovieDetailsUiState,
    onBack: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val blueGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF42A5F5))
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(blueGradient)
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Помилка : ${uiState.error}", color = Color.White)
                    Button(onClick = onBack) {
                        Text(text = "Повернутись")
                    }
                }
            }
            uiState.movie != null -> {
                val movie = uiState.movie
                if (isLandscape) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Box(
                            Modifier
                                .weight(1f)
                                .heightIn(min = 200.dp)
                                .background(Color(0xFF2C3848))
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w500${movie.backdropPath}",
                                contentDescription = movie.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(movie.title ?: "", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            Text("Випущено : ${movie.releaseDate}", color = Color.White)
                            Text("Рейтинг : ${movie.voteAverage}", color = Color.White)
                            Text(movie.overview ?: "", color = Color.White)
                        }
                    }
                } else {
                    Column(
                        Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(Color(0xFF2C3848))
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w500${movie.backdropPath}",
                                contentDescription = movie.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(movie.title ?: "", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(8.dp), color = Color.White)
                        Text("Випущено : ${movie.releaseDate}", modifier = Modifier.padding(horizontal = 8.dp), color = Color.White)
                        Text("Рейтинг : ${movie.voteAverage}", modifier = Modifier.padding(horizontal = 8.dp), color = Color.White)
                        Text(movie.overview ?: "", modifier = Modifier.padding(horizontal = 8.dp), color = Color.White)
                    }
                }
            }
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Закрити",
                tint = Color.Red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieDetailScreenPreview() {
    MovieDetailScreen(
        uiState = MovieDetailsUiState(
            movie = com.example.movieview.model.Movie(
                id = 1,
                title = "Title",
                posterPath = "/inception.jpg",
                releaseDate = "2025-06-21",
                voteAverage = 8.8f,
                overview = "Some overview.",
                backdropPath = "/backdrop.jpg"
            ),
            isLoading = false,
            error = null
        ),
        onBack = {}
    )
}
