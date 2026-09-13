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
    fun `signInWithGoogle authenticates user and emits SignedIn auth state`() = runTest {
        val result = authRepository.signInWithGoogle(
            idToken = "sample-google-id-token"
        )

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()
        assertEquals("Google User", profile?.displayName)
        assertEquals("google@test.com", profile?.email)
        assertEquals(UserRole.BUYER, profile?.role)

        val authState = authRepository.authState.first()
        assertTrue(authState is AuthState.SignedIn)
        assertEquals("google@test.com", (authState as AuthState.SignedIn).user.email)
    }

    @Test
    fun `signing out after Google sign in transitions to SignedOut state`() = runTest {
        authRepository.signInWithGoogle(
            idToken = "sample-google-id-token"
        )

        authRepository.signOut()

        assertNull(authRepository.currentUser())
        val authState = authRepository.authState.first()
        assertTrue(authState is AuthState.SignedOut)
    }
}
