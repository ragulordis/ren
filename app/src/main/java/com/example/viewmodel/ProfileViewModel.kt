package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuthState
import com.example.data.model.Property
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AuthState.Loading
        )

    val currentUserProfile: StateFlow<UserProfile?> = authState.map { state ->
        when (state) {
            is AuthState.SignedIn -> state.profile
            else -> authRepository.currentUser()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser())

    val userRole: StateFlow<String> = currentUserProfile.map { profile ->
        profile?.role?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() }
            ?: "Buyer"
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        authRepository.currentUser()?.role?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Buyer"
    )

    val myProperties: StateFlow<List<Property>> = propertyRepository.allProperties.map { list ->
        val uid = authRepository.currentUserId()
        if (uid != null) {
            list.filter { it.ownerId == uid }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @Deprecated("Roles are authoritative and determined by the backend. Client cannot escalate roles.")
    fun setUserRole(role: String) {
        // No-op: client cannot mutate server-authoritative role.
    }

    fun triggerFeedback(message: String) {
        // Handled via snackbar feedback or analytics
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteProperty(property: Property) {
        viewModelScope.launch {
            val uid = authRepository.currentUserId()
            if (uid != null && property.ownerId == uid) {
                propertyRepository.deleteProperty(property.id)
            }
        }
    }

    fun markPropertyStatus(property: Property, status: String) {
        viewModelScope.launch {
            val uid = authRepository.currentUserId()
            if (uid != null && property.ownerId == uid) {
                propertyRepository.updatePropertyStatus(property.id, status)
            }
        }
    }
}
