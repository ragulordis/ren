package com.example.domain.usecase

import com.example.data.model.AuthState
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository(
    initialUser: UserProfile? = UserProfile(
        uid = "test-user-id",
        displayName = "Test User",
        email = "test@quicknest.in",
        role = UserRole.BUYER
    )
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(
        if (initialUser != null) AuthState.SignedIn(initialUser) else AuthState.SignedOut
    )
    override val authState: Flow<AuthState> = _authState.asStateFlow()

    private var currentUser: UserProfile? = initialUser

    override fun currentUserId(): String? = currentUser?.uid

    override fun currentUser(): UserProfile? = currentUser

    override fun isAuthenticated(): Boolean = currentUser != null

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        val user = UserProfile(
            uid = "test-email-uid",
            displayName = "Email User",
            email = email,
            role = UserRole.BUYER
        )
        currentUser = user
        _authState.value = AuthState.SignedIn(user)
        return Result.success(user)
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: UserRole
    ): Result<UserProfile> {
        val user = UserProfile(
            uid = "test-new-uid",
            displayName = displayName,
            email = email,
            role = role
        )
        currentUser = user
        _authState.value = AuthState.SignedIn(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfile> {
        val user = UserProfile(
            uid = "test-google-uid",
            displayName = "Google User",
            email = "google@test.com",
            role = UserRole.BUYER
        )
        currentUser = user
        _authState.value = AuthState.SignedIn(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<UserProfile> {
        val user = UserProfile(
            uid = "test-google-cred-uid",
            displayName = "Google User",
            email = "google@test.com",
            role = UserRole.BUYER
        )
        currentUser = user
        _authState.value = AuthState.SignedIn(user)
        return Result.success(user)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun signOut() {
        currentUser = null
        _authState.value = AuthState.SignedOut
    }

    override suspend fun deleteAccount(): Result<Unit> {
        currentUser = null
        _authState.value = AuthState.SignedOut
        return Result.success(Unit)
    }

    override suspend fun updateUserProfile(
        displayName: String?,
        phone: String?,
        photoUrl: String?
    ): Result<UserProfile> {
        val current = currentUser ?: return Result.failure(Exception("Not signed in"))
        val updated = current.copy(
            displayName = displayName ?: current.displayName,
            phone = phone ?: current.phone,
            photoUrl = photoUrl ?: current.photoUrl
        )
        currentUser = updated
        _authState.value = AuthState.SignedIn(updated)
        return Result.success(updated)
    }

    fun setUser(user: UserProfile?) {
        currentUser = user
        _authState.value = if (user != null) AuthState.SignedIn(user) else AuthState.SignedOut
    }
}
