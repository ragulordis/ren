package com.example.domain.usecase

import com.example.data.model.AuthState
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase to observe authentication state and retrieve active user profile.
 */
class ObserveUserProfileUseCase(
    private val authRepository: AuthRepository
) {
    val authState: Flow<AuthState> = authRepository.authState

    fun getCurrentUser(): UserProfile? = authRepository.currentUser()

    fun getCurrentUserId(): String? = authRepository.currentUserId()
}

