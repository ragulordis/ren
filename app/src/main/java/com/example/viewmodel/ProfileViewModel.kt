package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuthState
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.domain.usecase.ObserveUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val observeUserProfileUseCase: ObserveUserProfileUseCase = ObserveUserProfileUseCase(authRepository)
) : AndroidViewModel(application) {

    val authState: StateFlow<AuthState> = observeUserProfileUseCase.authState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AuthState.Loading
        )

    val currentUserProfile: StateFlow<UserProfile?> = MutableStateFlow(authRepository.currentUser())

    private val _selectedRole = MutableStateFlow("Individual")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    fun setRole(role: String) {
        _selectedRole.value = role
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
