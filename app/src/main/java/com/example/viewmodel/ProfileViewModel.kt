package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.AuthState
import com.example.data.model.NotificationType
import com.example.data.model.Property
import com.example.data.model.UserProfile
import com.example.data.model.VerificationMethodType
import com.example.data.repository.AuthRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.PropertyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val propertyRepository: PropertyRepository,
    private val notificationRepository: NotificationRepository? = null
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AuthState.Loading
        )

    val currentUserProfile: StateFlow<UserProfile?> = authState.map { state ->
        when (state) {
            is AuthState.SignedIn -> state.user
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

    // --- VERIFIED BUYER SYSTEM STATE ---
    private val _showVerificationDialog = MutableStateFlow(false)
    val showVerificationDialog: StateFlow<Boolean> = _showVerificationDialog.asStateFlow()

    private val _selectedVerificationMethod = MutableStateFlow(VerificationMethodType.PHONE_OTP)
    val selectedVerificationMethod: StateFlow<VerificationMethodType> = _selectedVerificationMethod.asStateFlow()

    private val _activeOtpCode = MutableStateFlow<String?>(null)
    val activeOtpCode: StateFlow<String?> = _activeOtpCode.asStateFlow()

    private val _otpCountdown = MutableStateFlow(0)
    val otpCountdown: StateFlow<Int> = _otpCountdown.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _verificationMessage = MutableStateFlow<String?>(null)
    val verificationMessage: StateFlow<String?> = _verificationMessage.asStateFlow()

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> = _verificationError.asStateFlow()

    private var countdownJob: Job? = null

    fun openVerificationDialog(method: VerificationMethodType = VerificationMethodType.PHONE_OTP) {
        _selectedVerificationMethod.value = method
        _verificationError.value = null
        _verificationMessage.value = null
        _showVerificationDialog.value = true
    }

    fun closeVerificationDialog() {
        _showVerificationDialog.value = false
        _verificationError.value = null
        _verificationMessage.value = null
    }

    fun selectVerificationMethod(method: VerificationMethodType) {
        _selectedVerificationMethod.value = method
        _verificationError.value = null
        _verificationMessage.value = null
    }

    fun sendPhoneOtp(phoneNumber: String) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationError.value = null
            _verificationMessage.value = null

            val result = authRepository.sendBuyerPhoneOtp(phoneNumber)
            _isVerifying.value = false
            result.onSuccess { otp ->
                _activeOtpCode.value = otp
                _verificationMessage.value = "OTP sent successfully to $phoneNumber!"
                startCountdown()
            }.onFailure { err ->
                _verificationError.value = err.message ?: "Failed to send OTP"
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _otpCountdown.value = 60
            while (_otpCountdown.value > 0) {
                delay(1000)
                _otpCountdown.value = _otpCountdown.value - 1
            }
        }
    }

    fun verifyPhoneOtp(
        phoneNumber: String,
        otp: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationError.value = null
            _verificationMessage.value = null

            val result = authRepository.verifyBuyerPhoneWithOtp(phoneNumber, otp)
            _isVerifying.value = false
            result.onSuccess { updated ->
                _activeOtpCode.value = null
                _verificationMessage.value = "Phone verified! You unlocked Level 1 Verified Buyer badge."
                sendVerificationNotification(
                    title = "📱 Phone Verified!",
                    message = "Congratulations! Your mobile number $phoneNumber has been confirmed. You now have Level 1 Verified Buyer badge."
                )
                onSuccess()
            }.onFailure { err ->
                _verificationError.value = err.message ?: "Invalid OTP code. Please try again."
            }
        }
    }

    fun verifyGovernmentId(
        idType: String,
        idNumber: String,
        legalName: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationError.value = null
            _verificationMessage.value = null

            val result = authRepository.verifyBuyerGovernmentId(idType, idNumber, legalName)
            _isVerifying.value = false
            result.onSuccess { updated ->
                _verificationMessage.value = "Government ID verified! Level 2 Verified Buyer unlocked."
                sendVerificationNotification(
                    title = "🛡️ Government ID Verified!",
                    message = "Your $idType has been verified for $legalName. Trust level upgraded to Level 2."
                )
                onSuccess()
            }.onFailure { err ->
                _verificationError.value = err.message ?: "ID verification failed. Please check your details."
            }
        }
    }

    fun verifyFinancials(
        budgetRange: String,
        institution: String,
        proofType: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationError.value = null
            _verificationMessage.value = null

            val result = authRepository.verifyBuyerFinancials(budgetRange, institution, proofType)
            _isVerifying.value = false
            result.onSuccess { updated ->
                _verificationMessage.value = "Purchasing power confirmed! Level 3 Pre-Approved Buyer unlocked."
                sendVerificationNotification(
                    title = "💰 Financial Capacity Pre-Approved!",
                    message = "Verified budget range of $budgetRange with $institution. Sellers will prioritize your inquiries."
                )
                onSuccess()
            }.onFailure { err ->
                _verificationError.value = err.message ?: "Financial verification failed."
            }
        }
    }

    fun verifySelfie(
        photoUri: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationError.value = null
            _verificationMessage.value = null

            val result = authRepository.verifyBuyerSelfie(photoUri)
            _isVerifying.value = false
            result.onSuccess { updated ->
                _verificationMessage.value = "⭐ Elite Verified Buyer unlocked! Maximum trust score achieved."
                sendVerificationNotification(
                    title = "⭐ Elite Verified Buyer!",
                    message = "Biometric liveness confirmed! You now enjoy VIP access to off-market private listings and zero brokerage friction."
                )
                onSuccess()
            }.onFailure { err ->
                _verificationError.value = err.message ?: "Selfie verification failed."
            }
        }
    }

    private fun sendVerificationNotification(title: String, message: String) {
        viewModelScope.launch {
            try {
                notificationRepository?.sendNotification(
                    title = title,
                    message = message,
                    type = NotificationType.VERIFICATION,
                    actionText = "View Profile"
                )
            } catch (e: Exception) {
                Log.w("ProfileViewModel", "Failed to dispatch verification notification: ${e.message}")
            }
        }
    }

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

