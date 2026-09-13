package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.domain.usecase.ObserveUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application,
    private val authRepository: AuthRepository = com.example.data.repository.AuthRepositoryImpl(),
    private val observeUserProfileUseCase: ObserveUserProfileUseCase = ObserveUserProfileUseCase(authRepository)
) : AndroidViewModel(application) {

    val currentUserProfile: StateFlow<UserProfile> = observeUserProfileUseCase.authState
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            observeUserProfileUseCase.getCurrentUser()
        )

    private val _selectedRole = MutableStateFlow("Individual")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    fun setRole(role: String) {
        _selectedRole.value = role
    }

    fun signOut() {
        authRepository.signOut()
    }
}
