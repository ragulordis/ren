package com.example.data.model

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(
        val user: UserProfile
    ) : AuthState
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : AuthState
}

open class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
class NotAuthenticatedException(message: String = "User is not authenticated", cause: Throwable? = null) : AuthException(message, cause)
class InvalidCredentialsException(message: String = "Invalid email or password", cause: Throwable? = null) : AuthException(message, cause)
class UserAlreadyExistsException(message: String = "An account with this email already exists", cause: Throwable? = null) : AuthException(message, cause)
