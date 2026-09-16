package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuthState
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ren_app_prefs", Context.MODE_PRIVATE)

    private val _isOnboarded = MutableStateFlow(prefs.getBoolean("is_onboarded", false))
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(
        authRepository.isAuthenticated() || prefs.getBoolean("is_guest_mode", false)
    )
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AuthState.Loading
        )

    private val _currentUserProfile = MutableStateFlow(authRepository.currentUser())
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                when (state) {
                    is AuthState.SignedIn -> {
                        _currentUserProfile.value = state.profile
                        _isLoggedIn.value = true
                    }
                    is AuthState.SignedOut -> {
                        _currentUserProfile.value = null
                        if (!prefs.getBoolean("is_guest_mode", false)) {
                            _isLoggedIn.value = false
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("is_onboarded", true).apply()
        _isOnboarded.value = true
    }

    fun continueAsGuest() {
        prefs.edit().putBoolean("is_guest_mode", true).apply()
        _isLoggedIn.value = true
    }

    fun signInWithEmail(
        email: String,
        pass: String,
        onResult: (Result<UserProfile>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, pass)
            result.onSuccess {
                prefs.edit().putBoolean("is_guest_mode", false).apply()
                _isLoggedIn.value = true
            }
            onResult(result)
        }
    }

    fun registerWithEmail(
        email: String,
        pass: String,
        name: String,
        role: UserRole = UserRole.BUYER,
        city: String = "Chennai",
        onResult: (Result<UserProfile>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepository.registerWithEmail(email, pass, name, role)
            result.onSuccess {
                prefs.edit().putBoolean("is_guest_mode", false).apply()
                _isLoggedIn.value = true
            }
            onResult(result)
        }
    }

    fun signInWithGoogle(
        idToken: String,
        city: String = "Chennai",
        onResult: (Result<UserProfile>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            result.onSuccess {
                prefs.edit().putBoolean("is_guest_mode", false).apply()
                _isLoggedIn.value = true
            }
            onResult(result)
        }
    }

    fun sendPasswordReset(email: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            onResult(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.edit().putBoolean("is_guest_mode", false).apply()
            authRepository.signOut()
            _isLoggedIn.value = false
        }
    }
}
