package com.example.data.model

typealias Property = com.example.domain.model.Property
typealias ListingType = com.example.domain.model.ListingType
typealias SellingSpeed = com.example.domain.model.SellingSpeed
typealias PropertyCategory = com.example.domain.model.PropertyCategory

fun formatIndianCurrency(amount: Long, type: ListingType): String {
    return com.example.domain.model.formatIndianCurrency(amount, type)
}

data class PropertyVisit(
    val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val location: String,
    val buyerName: String,
    val date: String,
    val timeSlot: String,
    val status: String = "Requested" // Requested, Confirmed, Completed
)

data class ChatMessage(
    val id: String,
    val propertyId: String,
    val senderName: String,
    val message: String,
    val time: String,
    val isFromMe: Boolean
)

data class BuyerMatch(
    val buyerName: String,
    val buyerType: String, // Individual, Investor, Broker
    val matchScore: Int, // 70 - 98%
    val budgetRange: String,
    val preferredLocation: String,
    val preferredType: String,
    val contactStatus: String = "Ready to Contact"
)
