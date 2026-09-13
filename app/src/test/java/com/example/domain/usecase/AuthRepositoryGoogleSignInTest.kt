package com.example.domain.usecase

import com.example.data.model.AuthState
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryGoogleSignInTest {

    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository(initialUser = null)
    }

    @Test
    fun `signInWithGoogleAccount authenticates user and emits SignedIn auth state`() = runTest {
        val result = authRepository.signInWithGoogleAccount(
            email = "ragulordis@gmail.com",
            displayName = "Ragul Ordis",
            photoUrl = null,
            role = UserRole.BUYER
        )

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()
        assertEquals("Ragul Ordis", profile?.displayName)
        assertEquals("ragulordis@gmail.com", profile?.email)
        assertEquals(UserRole.BUYER, profile?.role)

        val authState = authRepository.authState.first()
        assertTrue(authState is AuthState.SignedIn)
        assertEquals("ragulordis@gmail.com", (authState as AuthState.SignedIn).user.email)
    }

    @Test
    fun `signing out after Google sign in transitions to SignedOut state`() = runTest {
        authRepository.signInWithGoogleAccount(
            email = "ragulordis@gmail.com",
            displayName = "Ragul Ordis",
            photoUrl = null,
            role = UserRole.BUYER
        )

        authRepository.signOut()

        assertNull(authRepository.currentUser())
        val authState = authRepository.authState.first()
        assertTrue(authState is AuthState.SignedOut)
    }
}
