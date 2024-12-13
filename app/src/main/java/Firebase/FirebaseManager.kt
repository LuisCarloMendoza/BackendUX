package Firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord


class FirebaseManager {
    private val auth = FirebaseAuth.getInstance()

    fun createUser(username: String?, password: String?): Boolean {
        try {
            val request = UserRecord.CreateRequest()
                .setEmail(username)
                .setPassword(password)

            val user: UserRecord = auth.createUser(request)
            println("User created successfully: " + user.uid)
            return true
        } catch (e: Exception) {
            System.err.println("Error creating user: " + e.message)
            return false
        }
    }

    fun signInUser(username: String?, password: String?): Boolean {
        try {
            val user: UserRecord = auth.getUserByEmail(username)
            println("User signed in successfully: " + user.uid)
            return true
        } catch (e: FirebaseAuthException) {
            System.err.println("Error signing in user: " + e.message)
            return false
        }
    }

    fun deleteUser(username: String): Boolean {
        try {
            val user: UserRecord = auth.getUserByEmail(username)
            auth.deleteUser(user.uid)
            println("User deleted successfully: $username")
            return true
        } catch (e: Exception) {
            System.err.println("Error deleting user: " + e.message)
            return false
        }
    }
}