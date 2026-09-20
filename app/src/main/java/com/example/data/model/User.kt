package com.example.data.model

enum class UserRole {
    BUYER,
    SELLER,
    BROKER,
    ADMIN,
    MODERATOR,
    INVESTOR,
    OWNER,
    TENANT
}

enum class VerificationMethodType(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val levelGranted: Int
) {
    PHONE_OTP("Phone Number (OTP)", "SMS instant verification", "phone", 1),
    GOVERNMENT_ID("Government ID", "Aadhaar, Passport, DL or Voter ID", "badge", 2),
    FINANCIAL_PRE_APPROVAL("Financial Pre-Approval", "Bank loan sanction or proof of funds", "account_balance", 3),
    SELFIE_LIVENESS("Biometric Liveness", "Quick front-camera face match", "face", 4)
}

data class UserProfile(
    val uid: String,
    val displayName: String = "User",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.BUYER,
    val accountStatus: String = "ACTIVE",
    val verificationStatus: String = "UNVERIFIED",
    val verificationLevel: Int = 0,
    val isPhoneVerified: Boolean = false,
    val isGovtIdVerified: Boolean = false,
    val isFinancialVerified: Boolean = false,
    val isSelfieVerified: Boolean = false,
    val verifiedPhone: String = "",
    val govtIdType: String = "",
    val govtIdNumberMasked: String = "",
    val buyerBudgetRange: String = "",
    val preApprovalBank: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isVerifiedBuyer: Boolean
        get() = verificationLevel > 0 || isPhoneVerified || isGovtIdVerified

    val verifiedBadgeText: String
        get() = when (verificationLevel) {
            4 -> "⭐ Elite Verified Buyer"
            3 -> "💰 Pre-Approved Buyer"
            2 -> "🛡️ ID Verified Buyer"
            1 -> "📱 Phone Verified Buyer"
            else -> if (isPhoneVerified) "📱 Phone Verified Buyer" else "Unverified Buyer"
        }
}

data class User(
    val uid: String,
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.BUYER
)


