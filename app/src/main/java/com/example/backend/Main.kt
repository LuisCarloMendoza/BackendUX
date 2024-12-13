package com.example.backend

//MongoDB
import Database.MongoManager
//Firebase
import Firebase.FirebaseManager
import Firebase.FirebaseInit
//TMBD
import MovieDataBaseAPI.MovieManager
//Ktor
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.HttpStatusCode


fun main() {
    try {
        FirebaseInit.initializeFirebase()
    } catch (e: Exception) {
        println("Failed to initialize Firebase: ${e.message}")
        return
    }
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        configureRouting()
    }.start(wait = true)
}


fun Application.configureRouting() {
    val mongoManager = MongoManager()
    val FirebaseManager = FirebaseManager()
    val movieManager = MovieManager("74dfc29543d4486989f41799ebf80d18")


    routing {
        // Root endpoint for health check
        get("/") {
            call.respondText("API is running!")
        }

        // Register a user
        post("/register") {
            val request = call.receive<UserRequest>()
            val firebaseResult = FirebaseManager.createUser(request.username, request.password)
            if (firebaseResult) {
                val result = mongoManager.createUser(request.username, request.password)
                call.respond(mapOf("success" to result))
            } else {
                call.respondText("Failed to register user in Firebase.", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }

        // Login a user
        post("/login") {
            val request = call.receive<UserRequest>()
            val firebaseResult = FirebaseManager.signInUser(request.username, request.password)
            if (firebaseResult) {
                val password = mongoManager.getPasswordByUsername(request.username)
                if(password == request.password){
                    call.respond(mapOf("success" to true, "message" to "User logged in successfully."))
                }else{
                    call.respondText("Invalid email or password.", status = io.ktor.http.HttpStatusCode.Unauthorized)
                }
            } else {
                call.respondText("Invalid email or password.", status = io.ktor.http.HttpStatusCode.Unauthorized)
            }
        }

        // Find a user by username
        get("/user/{username}") {
            val username = call.parameters["username"] ?: return@get call.respondText(
                "Missing username", status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val user = runBlocking { mongoManager.findUserByUsername(username) }
            if (user != null) {
                call.respond(user)
            } else {
                call.respondText("User not found", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }

        // Delete a user
        delete("/user/{username}") {
            val username = call.parameters["username"] ?: return@delete call.respondText(
                "Missing username", status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val result = mongoManager.deleteUser(username)
            call.respond(mapOf("success" to result))
        }

        // Add a movie to a user
        post("/user/{username}/movie") {
            val username = call.parameters["username"] ?: return@post call.respondText(
                "Missing username", status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val movie = call.receive<Movie>()
            val result = mongoManager.addMovieToUser(username, movie)
            call.respond(mapOf("success" to result))
        }

        // Remove a movie from a user
        delete("/user/{username}/movie/{movieId}") {
            val username = call.parameters["username"] ?: return@delete call.respondText(
                "Missing username", status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val movieId = call.parameters["movieId"]?.toIntOrNull() ?: return@delete call.respondText(
                "Invalid movie ID", status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val result = mongoManager.removeMovieFromUser(username, movieId)
            call.respond(mapOf("success" to result))
        }

        // Disconnect MongoDB when the application stops
        environment?.monitor?.subscribe(ApplicationStopped) {
            mongoManager.close()
        }

        get("/movies/popular") {
            val movies = movieManager.getPopularMovies()
            call.respond(movies)
        }

        // Fetch top-rated movies
        get("/movies/top_rated") {
            val movies = movieManager.getTopRatedMovies()
            call.respond(movies)
        }

        // Fetch upcoming movies
        get("/movies/upcoming") {
            val movies = movieManager.getUpcomingMovies()
            call.respond(movies)
        }

        // Fetch currently playing movies
        get("/movies/now_playing") {
            val movies = movieManager.getNowPlayingMovies()
            call.respond(movies)
        }

        // Fetch anime movies
        get("/movies/anime") {
            val animeMovies = movieManager.getAnimeMovies()
            call.respond(animeMovies)
        }

        // Fetch anime TV shows
        get("/tv/anime") {
            val animeTvShows = movieManager.getAnimeTvShows()
            call.respond(animeTvShows)
        }

        // Fetch detailed movie information with trailers
        get("/movies/{movieId}") {
            val movieId = call.parameters["movieId"]?.toIntOrNull()
            if (movieId == null) {
                call.respondText("Invalid movie ID", status = HttpStatusCode.BadRequest)
                return@get
            }

            val details = movieManager.getMovieDetails(movieId)
            val trailers = movieManager.getMovieTrailers(movieId)

            call.respond(mapOf(
                "details" to details,
                "trailers" to trailers
            ))
        }

    }
}

@Serializable
data class UserRequest(val username: String, val password: String)

@Serializable
data class Movie(val id: Int, val nombre: String)
