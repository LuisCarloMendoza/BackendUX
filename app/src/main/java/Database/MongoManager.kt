package Database

//MongoDB
import com.example.backend.Movie
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.bson.Document

class MongoManager(
    private val mongoUri: String = "mongodb+srv://lelekey682:ProyectoUX@uxbase.0pkkp.mongodb.net/",
    private val databaseName: String = "UXBase"
) {

    private val mongoClient: MongoClient by lazy { MongoClient.create(mongoUri) }
    private val database = mongoClient.getDatabase(databaseName)
    private val userCollection: MongoCollection<Document> by lazy { database.getCollection("User") }
    private val moviesCollection: MongoCollection<Document> by lazy { database.getCollection("Movie") }

    suspend fun createUser(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userDoc = Document("username", username)
                .append("password", password)
                .append("movies", emptyList<Document>())
            userCollection.insertOne(userDoc)
            println("User created: $username")
            true
        } catch (e: Exception) {
            println("Error creating user: ${e.message}")
            false
        }
    }

    suspend fun findUserByUsername(username: String): Document? = withContext(Dispatchers.IO) {
        try {
            userCollection.find(eq("username", username)).firstOrNull()
        } catch (e: Exception) {
            println("Error finding user: ${e.message}")
            null
        }
    }

    suspend fun deleteUser(username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            userCollection.deleteOne(eq("username", username))
            println("User deleted: $username")
            true
        } catch (e: Exception) {
            println("Error deleting user: ${e.message}")
            false
        }
    }

    suspend fun addMovieToUser(username: String, movie: Movie): Boolean = withContext(Dispatchers.IO) {
        try {
            val movieDoc = Document("id", movie.id)
                .append("nombre", movie.nombre)
            userCollection.updateOne(
                eq("username", username),
                Document("\$addToSet", Document("movies", movieDoc))
            )
            println("Movie added to user: $username")
            true
        } catch (e: Exception) {
            println("Error adding movie: ${e.message}")
            false
        }
    }

    suspend fun removeMovieFromUser(username: String, movieId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val movieDoc = Document("id", movieId)
            userCollection.updateOne(
                eq("username", username),
                Document("\$pull", Document("movies", movieDoc))
            )
            println("Movie removed from user: $username")
            true
        } catch (e: Exception) {
            println("Error removing movie: ${e.message}")
            false
        }
    }

    suspend fun getPasswordByUsername(username: String): String? = withContext(Dispatchers.IO) {
        try {
            val user = userCollection.find(eq("username", username)).firstOrNull()
            user?.getString("password")
        } catch (e: Exception) {
            println("Error retrieving password for user $username: ${e.message}")
            null
        }
    }

    fun close() {
        try {
            mongoClient.close()
            println("MongoDB connection closed.")
        } catch (e: Exception) {
            println("Error closing MongoDB connection: ${e.message}")
        }
    }
}

data class Movie(
    val id: Int,
    val nombre: String
)