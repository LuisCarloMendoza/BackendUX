package MovieDataBaseAPI

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

class MovieManager(private val apiKey: String) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getPopularMovies(): List<MovieResult> {
        return fetchMovies("https://api.themoviedb.org/3/movie/popular")
    }

    suspend fun getTopRatedMovies(): List<MovieResult> {
        return fetchMovies("https://api.themoviedb.org/3/movie/top_rated")
    }

    suspend fun getUpcomingMovies(): List<MovieResult> {
        return fetchMovies("https://api.themoviedb.org/3/movie/upcoming")
    }

    suspend fun getNowPlayingMovies(): List<MovieResult> {
        return fetchMovies("https://api.themoviedb.org/3/movie/now_playing")
    }

    suspend fun getMovieDetails(movieId: Int): MovieDetails {
        return client.get("https://api.themoviedb.org/3/movie/$movieId") {
            parameter("api_key", apiKey)
        }.body()
    }

    suspend fun getMovieTrailers(movieId: Int): List<VideoResult> {
        val response: VideoResponse = client.get("https://api.themoviedb.org/3/movie/$movieId/videos") {
            parameter("api_key", apiKey)
        }.body()
        return response.results.filter { it.site == "YouTube" && it.type == "Trailer" }
    }

    suspend fun getAnimeMovies(): List<MovieResult> {
        return fetchMovies("https://api.themoviedb.org/3/discover/movie", mapOf("with_genres" to "16"))
    }

    suspend fun getAnimeTvShows(): List<MovieResult> {
        return fetchMovies("https://api.themoviedb.org/3/discover/tv", mapOf("with_genres" to "16"))
    }

    private suspend fun fetchMovies(
        url: String,
        additionalParams: Map<String, String> = emptyMap()
    ): List<MovieResult> {
        val response: TmdbResponse = client.get(url) {
            parameter("api_key", apiKey)
            parameter("language", "en-US")
            parameter("page", 1)
            additionalParams.forEach { (key, value) -> parameter(key, value) }
        }.body()
        return response.results
    }
}

@Serializable
data class TmdbResponse(
    val results: List<MovieResult>
)

@Serializable
data class MovieResult(
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val release_date: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val popularity: Double?
)

@Serializable
data class MovieDetails(
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val release_date: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val popularity: Double?,
    val runtime: Int?,
    val genres: List<Genre>?,
    val homepage: String?,
    val tagline: String?
)

@Serializable
data class Genre(
    val id: Int,
    val name: String
)

@Serializable
data class VideoResponse(
    val results: List<VideoResult>
)

@Serializable
data class VideoResult(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String
)