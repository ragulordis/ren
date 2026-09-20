package com.example.data.model

import java.util.UUID

enum class NotificationType(val displayName: String) {
    PRICE_DROP("Price Drop"),
    NEW_LISTING("New Listing"),
    VISIT_UPDATE("Site Visit"),
    CHAT_MESSAGE("Inquiry"),
    PROPERTY_ALERT("Property Alert"),
    VERIFICATION("Trust & Verification"),
    SYSTEM_UPDATE("System Alert");

    companion object {
        fun fromString(value: String): NotificationType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: SYSTEM_UPDATE
        }
    }
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val propertyId: String? = null,
    val targetLocation: String? = null,
    val actionText: String? = null
)
