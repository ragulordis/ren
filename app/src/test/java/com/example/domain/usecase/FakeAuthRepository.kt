package com.example.domain.usecase

import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository(
    initialUser: UserProfile = UserProfile(
        uid = "test-user-id",
        displayName = "Test User",
        email = "test@quicknest.in",
        role = UserRole.BUYER
    )
) : AuthRepository {

    private val _authState = MutableStateFlow<UserProfile?>(initialUser)
    override val authState: Flow<UserProfile?> = _authState.asStateFlow()

    private var currentUser: UserProfile = initialUser

    override fun getCurrentUserId(): String = currentUser.uid

    override fun getCurrentUser(): UserProfile = currentUser

    override suspend fun signInAnonymously(): Result<UserProfile> {
        currentUser = UserProfile(
            uid = "anon-123",
            displayName = "Anonymous User",
            email = "",
            role = UserRole.BUYER
        )
        _authState.value = currentUser
        return Result.success(currentUser)
    }

    override fun signOut() {
        currentUser = UserProfile(
            uid = "guest",
            displayName = "Guest User",
            email = "",
            role = UserRole.BUYER
        )
        _authState.value = null
    }

    fun setUser(user: UserProfile) {
        currentUser = user
        _authState.value = user
    }
}
