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
    val createdAt: Long = System.currentTimeMillis()
)
