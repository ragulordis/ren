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
 * Production implementation of AuthRepository connecting Firebase Auth and Firestore users/{uid}.
 */
class AuthRepositoryImpl(
    customAuth: FirebaseAuth? = null,
    customFirestore: FirebaseFirestore? = null
) : AuthRepository {

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

    @Volatile
    private var cachedUserProfile: UserProfile? = null

    override val authState: Flow<AuthState> = callbackFlow {
        val auth = firebaseAuth
        if (auth == null) {
            trySend(AuthState.SignedOut)
            close()
            return@callbackFlow
        }

        trySend(AuthState.Loading)

        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            val user = currentAuth.currentUser
            if (user == null) {
                cachedUserProfile = null
                trySend(AuthState.SignedOut)
            } else {
                scope.launch {
                    val profile = fetchOrCreateUserProfile(user)
                    cachedUserProfile = profile
                    trySend(AuthState.SignedIn(profile))
                }
            }
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun currentUserId(): String? {
        return firebaseAuth?.currentUser?.uid
    }

    override fun currentUser(): UserProfile? {
        return cachedUserProfile
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

            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth unavailable")
            val authResult = try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                throw InvalidCredentialsException(e.message ?: "Invalid email or password", e)
            } catch (e: Exception) {
                throw AuthException(e.message ?: "Sign in failed", e)
            }

            val firebaseUser = authResult.user ?: throw NotAuthenticatedException("Sign-in succeeded but no user was returned")
            val profile = fetchOrCreateUserProfile(firebaseUser)
            cachedUserProfile = profile
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

            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth unavailable")
            val authResult = try {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
            } catch (e: FirebaseAuthUserCollisionException) {
                throw UserAlreadyExistsException("An account with email $email already exists", e)
            } catch (e: Exception) {
                throw AuthException(e.message ?: "Registration failed", e)
            }

            val firebaseUser = authResult.user ?: throw NotAuthenticatedException("Registration succeeded but no user was returned")

            // Safe role enforcement: client can pick initial standard roles (BUYER, SELLER, INVESTOR, OWNER, TENANT)
            // but NEVER admin or moderator
            val safeRole = when (role) {
                UserRole.ADMIN, UserRole.MODERATOR -> UserRole.BUYER
                else -> role
            }

            val uid = firebaseUser.uid
            val profileData = hashMapOf(
                "uid" to uid,
                "displayName" to displayName.trim(),
                "email" to (firebaseUser.email ?: email.trim()),
                "phone" to (firebaseUser.phoneNumber ?: ""),
                "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                "role" to safeRole.name,
                "accountStatus" to "ACTIVE",
                "verificationStatus" to if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
                "verificationLevel" to if (firebaseUser.isEmailVerified) 1 else 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val db = firestore
            if (db != null) {
                db.collection("users").document(uid).set(profileData).await()
            }

            val profile = UserProfile(
                uid = uid,
                displayName = displayName.trim(),
                email = firebaseUser.email ?: email.trim(),
                phone = firebaseUser.phoneNumber ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                role = safeRole,
                accountStatus = "ACTIVE",
                verificationStatus = if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
                verificationLevel = if (firebaseUser.isEmailVerified) 1 else 0
            )

            cachedUserProfile = profile
            profile
        }
    }

    override suspend fun signInWithGoogle(
        idToken: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            require(idToken.isNotBlank()) { "Google ID Token cannot be empty" }
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            signInWithGoogleCredential(credential).getOrThrow()
        }
    }

    override suspend fun signInWithGoogleCredential(
        credential: AuthCredential
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth unavailable")
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw NotAuthenticatedException("Google Sign-In returned no user")
            val profile = fetchOrCreateUserProfile(firebaseUser)
            cachedUserProfile = profile
            profile
        }
    }

    override suspend fun sendPasswordReset(
        email: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(email.isNotBlank()) { "Email cannot be empty" }
            val auth = firebaseAuth ?: throw IllegalStateException("FirebaseAuth unavailable")
            auth.sendPasswordResetEmail(email.trim()).await()
            Unit
        }
    }

    override suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        cachedUserProfile = null
        firebaseAuth?.signOut()
        Unit
    }

    override suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val user = firebaseAuth?.currentUser ?: throw NotAuthenticatedException()
            user.delete().await()
            cachedUserProfile = null
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

            val updates = hashMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            displayName?.let { updates["displayName"] = it.trim() }
            phone?.let { updates["phone"] = it.trim() }
            photoUrl?.let { updates["photoUrl"] = it.trim() }

            db.collection("users").document(uid).set(updates, SetOptions.merge()).await()

            val updatedDoc = db.collection("users").document(uid).get().await()
            val profile = parseUserProfileDoc(uid, user, updatedDoc)
            cachedUserProfile = profile
            profile
        }
    }

    private suspend fun fetchOrCreateUserProfile(firebaseUser: FirebaseUser): UserProfile {
        val uid = firebaseUser.uid
        val db = firestore

        if (db == null) {
            return fallbackProfile(firebaseUser)
        }

        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                parseUserProfileDoc(uid, firebaseUser, doc)
            } else {
                // First-time user sign in (e.g. Google Sign In) -> create profile in Firestore
                val fallbackName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Ren User"
                val profileData = hashMapOf(
                    "uid" to uid,
                    "displayName" to fallbackName,
                    "email" to (firebaseUser.email ?: ""),
                    "phone" to (firebaseUser.phoneNumber ?: ""),
                    "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                    "role" to UserRole.BUYER.name,
                    "accountStatus" to "ACTIVE",
                    "verificationStatus" to if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
                    "verificationLevel" to if (firebaseUser.isEmailVerified) 1 else 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).set(profileData).await()

                UserProfile(
                    uid = uid,
                    displayName = fallbackName,
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    role = UserRole.BUYER,
                    accountStatus = "ACTIVE",
                    verificationStatus = if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
                    verificationLevel = if (firebaseUser.isEmailVerified) 1 else 0
                )
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Error fetching user document from Firestore: ${e.message}")
            fallbackProfile(firebaseUser)
        }
    }

    private fun parseUserProfileDoc(
        uid: String,
        firebaseUser: FirebaseUser,
        doc: com.google.firebase.firestore.DocumentSnapshot
    ): UserProfile {
        val roleStr = doc.getString("role") ?: UserRole.BUYER.name
        val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.BUYER)
        val name = doc.getString("displayName")?.takeIf { it.isNotBlank() }
            ?: firebaseUser.displayName?.takeIf { it.isNotBlank() }
            ?: "Ren User"

        return UserProfile(
            uid = uid,
            displayName = name,
            email = doc.getString("email") ?: firebaseUser.email ?: "",
            phone = doc.getString("phone") ?: firebaseUser.phoneNumber ?: "",
            photoUrl = doc.getString("photoUrl") ?: firebaseUser.photoUrl?.toString() ?: "",
            role = role,
            accountStatus = doc.getString("accountStatus") ?: "ACTIVE",
            verificationStatus = doc.getString("verificationStatus") ?: (if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED"),
            verificationLevel = doc.getLong("verificationLevel")?.toInt() ?: (if (firebaseUser.isEmailVerified) 1 else 0),
            createdAt = (doc.getTimestamp("createdAt")?.toDate()?.time) ?: System.currentTimeMillis()
        )
    }

    private fun fallbackProfile(firebaseUser: FirebaseUser): UserProfile {
        return UserProfile(
            uid = firebaseUser.uid,
            displayName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Ren User",
            email = firebaseUser.email ?: "",
            phone = firebaseUser.phoneNumber ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: "",
            role = UserRole.BUYER,
            accountStatus = "ACTIVE",
            verificationStatus = if (firebaseUser.isEmailVerified) "VERIFIED" else "UNVERIFIED",
            verificationLevel = if (firebaseUser.isEmailVerified) 1 else 0
        )
    }
}

/**
 * Default alias pointing to AuthRepositoryImpl for backwards compatibility.
 */
typealias AuthRepositoryDefault = AuthRepositoryImpl
