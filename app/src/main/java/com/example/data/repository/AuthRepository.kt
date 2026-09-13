package com.example.data.repository

import android.util.Log
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * Interface contract for user authentication and profile management.
 */
interface AuthRepository {
    val authState: Flow<UserProfile?>
    fun getCurrentUserId(): String
    fun getCurrentUser(): UserProfile
    suspend fun signInAnonymously(): Result<UserProfile>
    fun signOut()
}

/**
 * Production implementation of AuthRepository with resilient Firebase initialization.
 */
class AuthRepositoryImpl(
    customAuth: FirebaseAuth? = null
) : AuthRepository {

    private val firebaseAuth: FirebaseAuth? by lazy {
        customAuth ?: runCatching { FirebaseAuth.getInstance() }
            .onFailure { Log.w("AuthRepository", "FirebaseAuth not yet initialized: ${it.message}") }
            .getOrNull()
    }

    override val authState: Flow<UserProfile?> = callbackFlow {
        val auth = firebaseAuth
        if (auth == null) {
            trySend(getCurrentUser())
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { current ->
            val user = current.currentUser
            trySend(user?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun getCurrentUserId(): String {
        return firebaseAuth?.currentUser?.uid ?: "anonymous_user"
    }

    override fun getCurrentUser(): UserProfile {
        return firebaseAuth?.currentUser?.toDomain() ?: UserProfile(
            uid = getCurrentUserId(),
            displayName = "Guest User",
            email = "",
            role = UserRole.BUYER
        )
    }

    override suspend fun signInAnonymously(): Result<UserProfile> = runCatching {
        val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth unavailable")
        val result = auth.signInAnonymously().await()
        val user = result.user ?: throw IllegalStateException("Failed to get FirebaseUser")
        user.toDomain()
    }

    override fun signOut() {
        firebaseAuth?.signOut()
    }

    private fun FirebaseUser.toDomain(): UserProfile {
        return UserProfile(
            uid = uid,
            displayName = displayName?.takeIf { it.isNotBlank() } ?: "User_${uid.take(5)}",
            email = email ?: "",
            phone = phoneNumber ?: "",
            photoUrl = photoUrl?.toString() ?: "",
            role = UserRole.BUYER,
            verificationStatus = if (isEmailVerified) "VERIFIED" else "UNVERIFIED",
            verificationLevel = if (isEmailVerified) 1 else 0
        )
    }
}

/**
 * Default alias pointing to AuthRepositoryImpl for backwards compatibility.
 */
typealias AuthRepositoryDefault = AuthRepositoryImpl
