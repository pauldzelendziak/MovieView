package com.example.movieview.ui.MovieList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movieview.model.Movie
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    uiState: MovieListUiState,
    onMovieClick: (Int) -> Unit,
    onLoadingNextPage: () -> Unit,
    onRetry: () -> Unit,
    onTypeChange: (MovieListType) -> Unit,
) {
    val popularGridState = rememberLazyGridState()
    val trendingGridState = rememberLazyGridState()
    val currentGridState = when (uiState.currentType) {
        MovieListType.Popular -> popularGridState
        MovieListType.Trending -> trendingGridState
    }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    LaunchedEffect(uiState.errors) {
        uiState.errors?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(
        currentGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index,
        uiState.movies.size,
        uiState.isLoadingNextPage,
        uiState.endReach,
        uiState.currentType
    ) {
        val lastVisibleItem = currentGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val totalItems = uiState.movies.size
        val threshold = 2

        val shouldLoad = lastVisibleItem >= totalItems - threshold &&
                !uiState.isLoadingNextPage &&
                !uiState.endReach &&
                totalItems > 0

        if (shouldLoad) {
            onLoadingNextPage()
        }
    }

    var lastLoadedPage by remember { mutableStateOf(0) }
    LaunchedEffect(uiState.page) {
        if (uiState.page > lastLoadedPage) {
            lastLoadedPage = uiState.page
        }
    }

    val blueGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF42A5F5))
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(blueGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0D47A1), Color(0xFF42A5F5))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Movie Viewer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onTypeChange(MovieListType.Popular) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (uiState.currentType == MovieListType.Popular)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Популярні",
                        color = if (uiState.currentType == MovieListType.Popular)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onTypeChange(MovieListType.Trending) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (uiState.currentType == MovieListType.Trending)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Трендові",
                        color = if (uiState.currentType == MovieListType.Trending)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.movies.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }

                    uiState.errors != null && uiState.movies.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Помилка : ${uiState.errors}")
                            Button(onClick = onRetry) {
                                Text("Повторити")
                            }
                        }
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            state = currentGridState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {

                            items(uiState.movies) { movie ->
                                MovieGridItem(movie, onClick = { onMovieClick(movie.id) })
                            }
                            if (uiState.isLoadingNextPage && uiState.movies.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            if (uiState.endReach && uiState.movies.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 32.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "На сьогодні все",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Ви переглянули усі фільми",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.7f
                                                    )
                                                )
                                                if (uiState.totalPages > 0) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Сторінка ${uiState.page} з ${uiState.totalPages}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.5f
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Загалом переглянуто: ${uiState.movies.size} фільмів",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.5f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieGridItem(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.65f)
                    .heightIn(max = 220.dp)
                    .background(Color(0xFF2C3848))
            ) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                movie.title ?: "",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )
            Text(
                movie.releaseDate ?: "",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieGridItemPreview() {
    MovieGridItem(
        movie = com.example.movieview.model.Movie(
            id = 1,
            title = "Inception",
            posterPath = "/inception.jpg",
            releaseDate = "2010-07-16",
            voteAverage = 8.8f,
            overview = "A mind-bending thriller.",
            backdropPath = "/backdrop.jpg"
        ),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun MovieListScreenPreview() {
    MovieListScreen(
        uiState = MovieListUiState(
            movies = listOf(
                Movie(
                    id = 1,
                    title = "Inception",
                    posterPath = "/inception.jpg",
                    releaseDate = "2010-07-16",
                    voteAverage = 8.8f,
                    overview = "A mind-bending thriller.",
                    backdropPath = "/backdrop.jpg"
                )
            ),
            isLoading = false,
            isLoadingNextPage = false,
            errors = null,
            page = 1,
            totalPages = 1,
            endReach = false,
            currentType = MovieListType.Popular
        ),
        onMovieClick = {},
        onLoadingNextPage = {},
        onRetry = {},
        onTypeChange = {}
    )
}
