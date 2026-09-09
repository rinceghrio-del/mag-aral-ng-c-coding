package com.rustech.cpplearn.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rustech.cpplearn.data.model.UserProfile
import kotlinx.coroutines.tasks.await

/**
 * Handles registration, login, and writing the user's profile / login
 * timestamps to Firestore so the Admin Panel can list registered users
 * and see who logged in and when.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("No UID returned"))

            val profile = UserProfile(
                uid = uid,
                name = name,
                email = email,
                dateRegistered = System.currentTimeMillis(),
                lastLogin = System.currentTimeMillis()
            )
            db.collection("users").document(uid).set(profile).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("No UID returned"))

            // Record this login so the admin panel can see login activity per user.
            db.collection("users").document(uid)
                .update("lastLogin", System.currentTimeMillis())
                .await()

            db.collection("loginLogs").add(
                mapOf(
                    "uid" to uid,
                    "email" to email,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() = auth.signOut()

    fun isLoggedIn(): Boolean = auth.currentUser != null
}
