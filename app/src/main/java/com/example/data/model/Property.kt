package com.example.data.model

enum class ListingType(val label: String) {
    BUY("Buy"),
    RENT("Rent"),
    LEASE("Lease"),
    URGENT_SALE("Urgent Sale")
}

enum class SellingSpeed(
    val title: String,
    val durationText: String,
    val emoji: String
) {
    NORMAL("Normal", "30–90 Days", "🟢"),
    FAST("Fast Sale", "7–30 Days", "🟡"),
    URGENT("Urgent Sale", "1–7 Days", "🔴"),
    PRIVATE("Private Sale", "Verified Buyers Only", "⚫")
}

enum class PropertyCategory(val label: String, val iconEmoji: String) {
    ALL("All", "✨"),
    BUY("Buy", "🏠"),
    RENT("Rent", "🔑"),
    LEASE("Lease", "📜"),
    LAND("Land", "🌳"),
    COMMERCIAL("Commercial", "🏢")
}

data class Property(
    val id: String,
    val title: String,
    val description: String,
    val listingType: ListingType,
    val sellingSpeed: SellingSpeed,
    val category: PropertyCategory,
    val propertyType: String,
    val price: Long,
    val originalPrice: Long = price,
    val marketEstimate: Long = price,
    val location: String,
    val approximateArea: String,
    val distanceKm: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val areaSqFt: Int,
    val urgencyScore: Int = 3, // 1-5 scale (flames)
    val verificationLevel: Int = 2, // 1: Phone, 2: ID, 3: Ownership, 4: Trusted Seller
    val imageResName: String,
    val featuresList: List<String>,
    val suitableFor: String = "Family & Working Professionals",
    val leaseDurationMonths: Int? = null,
    val isDepositRefundable: Boolean = true,
    val ownerName: String,
    val ownerPhone: String = "+91 98401 23456",
    val ownerType: String = "Owner",
    val isSaved: Boolean = false,
    val viewsCount: Int = 120,
    val savedCount: Int = 18,
    val messagesCount: Int = 5,
    val visitRequestsCount: Int = 2,
    val interestedBuyersCount: Int = 12,
    val mapLat: Double = 11.9800,
    val mapLng: Double = 79.8350,
    val isPrivate: Boolean = false,
    val status: String = "Active",
    val ownerEmail: String = "${ownerName.lowercase().replace(" ", "").filter { it.isLetterOrDigit() }}@quicknest.in"
) {
    val formattedPrice: String
        get() = formatIndianCurrency(price, listingType)

    val formattedMarketEstimate: String
        get() = formatIndianCurrency(marketEstimate, listingType)

    val formattedOriginalPrice: String
        get() = formatIndianCurrency(originalPrice, listingType)

    val savingsAmount: Long
        get() = if (marketEstimate > price) marketEstimate - price else 0L

    val formattedSavings: String
        get() = if (savingsAmount > 0) formatIndianCurrency(savingsAmount, ListingType.BUY) else ""

    val isVerified: Boolean
        get() = verificationLevel >= 2

    val isUrgent: Boolean
        get() = urgencyScore >= 4 || sellingSpeed == SellingSpeed.URGENT || listingType == ListingType.URGENT_SALE
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

fun formatIndianCurrency(amount: Long, type: ListingType): String {
    return when {
        type == ListingType.RENT -> {
            "₹%,d/mo".format(amount)
        }
        amount >= 10000000 -> {
            val cr = amount / 10000000.0
            if (cr % 1.0 == 0.0) "₹%.0f Cr".format(cr) else "₹%.1f Cr".format(cr)
        }
        amount >= 100000 -> {
            val lakhs = amount / 100000.0
            if (lakhs % 1.0 == 0.0) "₹%.0f Lakhs".format(lakhs) else "₹%.1f Lakhs".format(lakhs)
        }
        else -> {
            "₹%,d".format(amount)
        }
    }
}
