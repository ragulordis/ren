package com.example.data.repository

import android.util.Log
import com.example.data.model.AuthException
import com.example.data.model.AuthState
import com.example.data.model.InvalidCredentialsException
import com.example.data.model.NotAuthenticatedException
import com.example.data.model.UserAlreadyExistsException
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Interface contract for production user authentication and profile management.
 */
interface AuthRepository {
    val authState: Flow<AuthState>

    fun currentUserId(): String?
    fun currentUser(): UserProfile?
    fun isAuthenticated(): Boolean

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<UserProfile>

    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: UserRole = UserRole.BUYER
    ): Result<UserProfile>

    suspend fun signInWithGoogle(
        idToken: String
    ): Result<UserProfile>

    suspend fun signInWithGoogleCredential(
        credential: AuthCredential
    ): Result<UserProfile>

    suspend fun sendPasswordReset(
        email: String
    ): Result<Unit>

    suspend fun signOut()

    suspend fun deleteAccount(): Result<Unit>

    suspend fun updateUserProfile(
        displayName: String? = null,
        phone: String? = null,
        photoUrl: String? = null
    ): Result<UserProfile>
}

/**
 * Production implementation of AuthRepository connecting Firebase Auth and Firestore.
 * Strictly separates public profile data (users/{uid}) from private profile data (users/{uid}/private/profile).
 */
class AuthRepositoryImpl(
    customAuth: FirebaseAuth? = null,
    customFirestore: FirebaseFirestore? = null
) : AuthRepository {

    companion object {
        @Volatile
        private var sharedUserProfile: UserProfile? = null
        private val _sharedAuthState = MutableStateFlow<AuthState>(AuthState.SignedOut)
        private var isFirebaseListenerRegistered = false
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val firebaseAuth: FirebaseAuth? by lazy {
        customAuth ?: runCatching { FirebaseAuth.getInstance() }
            .onFailure { Log.w("AuthRepository", "FirebaseAuth not yet initialized: ${it.message}") }
            .getOrNull()
    }

    private val firestore: FirebaseFirestore? by lazy {
        customFirestore ?: runCatching { FirebaseFirestore.getInstance() }
            .onFailure { Log.w("AuthRepository", "FirebaseFirestore not yet initialized: ${it.message}") }
            .getOrNull()
    }

    init {
        val auth = firebaseAuth
        if (auth != null) {
            if (!isFirebaseListenerRegistered) {
                isFirebaseListenerRegistered = true
                auth.addAuthStateListener { currentAuth ->
                    val user = currentAuth.currentUser
                    if (user == null) {
                        sharedUserProfile = null
                        _sharedAuthState.value = AuthState.SignedOut
                    } else {
                        scope.launch {
                            try {
                                val profile = fetchOrCreateUserProfile(user)
                                sharedUserProfile = profile
                                _sharedAuthState.value = AuthState.SignedIn(profile)
                            } catch (e: Exception) {
                                Log.e("AuthRepository", "Error resolving user profile: ${e.message}", e)
                                _sharedAuthState.value = AuthState.Error("Failed to synchronize user profile", e)
                            }
                        }
                    }
                }
            }
        } else {
            _sharedAuthState.value = AuthState.Error("FirebaseAuth service unavailable")
        }
    }

    override val authState: Flow<AuthState> = _sharedAuthState.asStateFlow()

    override fun currentUserId(): String? {
        return firebaseAuth?.currentUser?.uid ?: sharedUserProfile?.uid
    }

    override fun currentUser(): UserProfile? {
        return sharedUserProfile
    }

    override fun isAuthenticated(): Boolean {
        return currentUserId() != null
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            require(email.isNotBlank()) { "Email cannot be empty" }
            require(password.isNotBlank()) { "Password cannot be empty" }

            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth service unavailable")
            val authResult = try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                throw InvalidCredentialsException(e.message ?: "Invalid email or password", e)
            } catch (e: Exception) {
                throw AuthException(e.message ?: "Sign in failed", e)
            }

            val firebaseUser = authResult.user ?: throw NotAuthenticatedException("Sign-in succeeded but no user was returned")
            val profile = fetchOrCreateUserProfile(firebaseUser)
            sharedUserProfile = profile
            _sharedAuthState.value = AuthState.SignedIn(profile)
            profile
        }
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: UserRole
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            require(email.isNotBlank()) { "Email cannot be empty" }
            require(password.length >= 6) { "Password must be at least 6 characters" }
            require(displayName.isNotBlank()) { "Name cannot be empty" }

            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth service unavailable")
            val authResult = try {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
            } catch (e: FirebaseAuthUserCollisionException) {
                throw UserAlreadyExistsException("An account with email $email already exists", e)
            } catch (e: Exception) {
                throw AuthException(e.message ?: "Registration failed", e)
            }

            val firebaseUser = authResult.user ?: throw NotAuthenticatedException("Registration succeeded but no user was returned")
            val uid = firebaseUser.uid
            val initialRole = UserRole.BUYER

            val db = firestore
                ?: throw AuthException("User profile service unavailable (Firestore unavailable)")

            // 1. Write public profile document
            val publicProfileData = hashMapOf(
                "uid" to uid,
                "displayName" to displayName.trim(),
                "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                "role" to initialRole.name,
                "accountStatus" to "ACTIVE",
                "verificationStatus" to if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
                "verificationLevel" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).set(publicProfileData).await()

            // 2. Write private profile document (isolated access)
            val privateProfileData = hashMapOf(
                "email" to (firebaseUser.email ?: email.trim()),
                "phone" to (firebaseUser.phoneNumber ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid)
                .collection("private").document("profile")
                .set(privateProfileData).await()

            val profile = UserProfile(
                uid = uid,
                displayName = displayName.trim(),
                email = firebaseUser.email ?: email.trim(),
                phone = firebaseUser.phoneNumber ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                role = initialRole,
                accountStatus = "ACTIVE",
                verificationStatus = if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
                verificationLevel = 0
            )

            sharedUserProfile = profile
            _sharedAuthState.value = AuthState.SignedIn(profile)
            profile
        }
    }

    override suspend fun signInWithGoogle(
        idToken: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            require(idToken.isNotBlank()) { "Google ID Token cannot be empty" }
            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth service unavailable")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            signInWithGoogleCredential(credential).getOrThrow()
        }
    }

    override suspend fun signInWithGoogleCredential(
        credential: AuthCredential
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth service unavailable")
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw NotAuthenticatedException("Google Sign-In returned no user")
            val profile = fetchOrCreateUserProfile(firebaseUser)
            sharedUserProfile = profile
            _sharedAuthState.value = AuthState.SignedIn(profile)
            profile
        }
    }

    override suspend fun sendPasswordReset(
        email: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(email.isNotBlank()) { "Email cannot be empty" }
            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth service unavailable")
            auth.sendPasswordResetEmail(email.trim()).await()
            Unit
        }
    }

    override suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        sharedUserProfile = null
        _sharedAuthState.value = AuthState.SignedOut
        runCatching { firebaseAuth?.signOut() }
        Unit
    }

    override suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val user = firebaseAuth?.currentUser
            if (user != null) {
                val uid = user.uid
                val db = firestore
                if (db != null) {
                    runCatching {
                        db.collection("users").document(uid)
                            .collection("private").document("profile")
                            .delete().await()
                    }
                }
                user.delete().await()
            }
            sharedUserProfile = null
            _sharedAuthState.value = AuthState.SignedOut
            Unit
        }
    }

    override suspend fun updateUserProfile(
        displayName: String?,
        phone: String?,
        photoUrl: String?
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            val user = firebaseAuth?.currentUser ?: throw NotAuthenticatedException()
            val uid = user.uid
            val db = firestore ?: throw IllegalStateException("Firestore unavailable")

            // Update public profile fields
            val publicUpdates = hashMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            displayName?.let { publicUpdates["displayName"] = it.trim() }
            photoUrl?.let { publicUpdates["photoUrl"] = it.trim() }

            if (displayName != null || photoUrl != null) {
                db.collection("users").document(uid).set(publicUpdates, SetOptions.merge()).await()
            }

            // Update private profile fields
            if (phone != null) {
                val privateUpdates = hashMapOf<String, Any>(
                    "phone" to phone.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid)
                    .collection("private").document("profile")
                    .set(privateUpdates, SetOptions.merge()).await()
            }

            val updatedProfile = fetchOrCreateUserProfile(user)
            sharedUserProfile = updatedProfile
            _sharedAuthState.value = AuthState.SignedIn(updatedProfile)
            updatedProfile
        }
    }

    private suspend fun fetchOrCreateUserProfile(firebaseUser: FirebaseUser): UserProfile {
        val uid = firebaseUser.uid
        val db = firestore
            ?: throw AuthException("User profile service unavailable (Firestore unavailable)")

        val publicDoc = db.collection("users").document(uid).get().await()

        val privateDoc = runCatching {
            db.collection("users").document(uid)
                .collection("private").document("profile")
                .get().await()
        }.getOrNull()

        if (publicDoc.exists()) {
            val roleStr = publicDoc.getString("role") ?: UserRole.BUYER.name
            val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.BUYER)
            val name = publicDoc.getString("displayName")?.takeIf { it.isNotBlank() }
                ?: firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: "Ren User"

            val email = privateDoc?.getString("email")
                ?: publicDoc.getString("email")
                ?: firebaseUser.email
                ?: ""

            val phone = privateDoc?.getString("phone")
                ?: publicDoc.getString("phone")
                ?: firebaseUser.phoneNumber
                ?: ""

            return UserProfile(
                uid = uid,
                displayName = name,
                email = email,
                phone = phone,
                photoUrl = publicDoc.getString("photoUrl") ?: firebaseUser.photoUrl?.toString() ?: "",
                role = role,
                accountStatus = publicDoc.getString("accountStatus") ?: "ACTIVE",
                verificationStatus = publicDoc.getString("verificationStatus") ?: (if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED"),
                verificationLevel = publicDoc.getLong("verificationLevel")?.toInt() ?: (if (firebaseUser.isEmailVerified) 1 else 0),
                createdAt = (publicDoc.getTimestamp("createdAt")?.toDate()?.time) ?: System.currentTimeMillis()
            )
        } else {
            // First-time sign in (e.g. Google Sign In) -> initialize user document
            val fallbackName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Ren User"
            val publicProfileData = hashMapOf(
                "uid" to uid,
                "displayName" to fallbackName,
                "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                "role" to UserRole.BUYER.name,
                "accountStatus" to "ACTIVE",
                "verificationStatus" to if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
                "verificationLevel" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).set(publicProfileData).await()

            val privateProfileData = hashMapOf(
                "email" to (firebaseUser.email ?: ""),
                "phone" to (firebaseUser.phoneNumber ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid)
                .collection("private").document("profile")
                .set(privateProfileData).await()

            return UserProfile(
                uid = uid,
                displayName = fallbackName,
                email = firebaseUser.email ?: "",
                phone = firebaseUser.phoneNumber ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                role = UserRole.BUYER,
                accountStatus = "ACTIVE",
                verificationStatus = if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
                verificationLevel = 0
            )
        }
    }
}

/**
 * Default alias pointing to AuthRepositoryImpl for backwards compatibility.
 */
typealias AuthRepositoryDefault = AuthRepositoryImpl
