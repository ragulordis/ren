package com.example.domain.model

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
    NORMAL("Normal", "30–90 Days", ""),
    FAST("Fast Sale", "7–30 Days", ""),
    URGENT("Urgent Sale", "1–7 Days", ""),
    PRIVATE("Private Sale", "Verified Buyers Only", "")
}

enum class PropertyCategory(val label: String, val iconEmoji: String) {
    ALL("All", ""),
    BUY("Buy", ""),
    RENT("Rent", ""),
    LEASE("Lease", ""),
    LAND("Land", ""),
    COMMERCIAL("Commercial", "")
}

enum class ListingStatus(val value: String) {
    DRAFT("Draft"),
    PENDING_REVIEW("Pending Review"),
    ACTIVE("Active"),
    SOLD("Sold"),
    RENTED("Rented"),
    LEASED("Leased"),
    ARCHIVED("Archived");

    companion object {
        fun fromString(status: String): ListingStatus {
            return entries.firstOrNull {
                it.name.equals(status, ignoreCase = true) || it.value.equals(status, ignoreCase = true)
            } ?: ACTIVE
        }

        fun isValidTransition(from: ListingStatus, to: ListingStatus): Boolean {
            if (from == to) return true
            return when (from) {
                DRAFT -> to in listOf(PENDING_REVIEW, ACTIVE, ARCHIVED)
                PENDING_REVIEW -> to in listOf(ACTIVE, DRAFT, ARCHIVED)
                ACTIVE -> to in listOf(SOLD, RENTED, LEASED, ARCHIVED)
                SOLD, RENTED, LEASED -> to in listOf(ACTIVE, ARCHIVED)
                ARCHIVED -> to in listOf(DRAFT, ACTIVE)
            }
        }
    }
}

enum class VisitStatus(val value: String) {
    REQUESTED("Requested"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    DECLINED("Declined");

    companion object {
        fun fromString(status: String): VisitStatus {
            return entries.firstOrNull {
                it.name.equals(status, ignoreCase = true) || it.value.equals(status, ignoreCase = true)
            } ?: REQUESTED
        }

        /**
         * Authoritative state machine transitions:
         * - REQUESTED -> CONFIRMED (by Seller)
         * - REQUESTED -> DECLINED (by Seller)
         * - REQUESTED -> CANCELLED (by Buyer)
         * - CONFIRMED -> COMPLETED (after visit happens)
         * - CONFIRMED -> CANCELLED (by Buyer or Seller)
         */
        fun isValidTransition(from: VisitStatus, to: VisitStatus): Boolean {
            if (from == to) return true
            return when (from) {
                REQUESTED -> to in listOf(CONFIRMED, DECLINED, CANCELLED)
                CONFIRMED -> to in listOf(COMPLETED, CANCELLED)
                COMPLETED -> false
                CANCELLED -> false
                DECLINED -> false
            }
        }
    }
}

data class Property(
    val id: String,
    val ownerId: String,
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
    /** Ordered Cloud Storage URLs for the listing gallery. The first URL is the cover photo. */
    val imageUrls: List<String> = emptyList(),
    val furnishing: String = "",
    val securityDeposit: Long = 0L,
    val maintenanceAmount: Long = 0L,
    val isMaintenanceIncluded: Boolean = false,
    val availableFrom: String = "",
    val nearbyLandmark: String = "",
    val tenantPreferences: List<String> = emptyList(),
    val featuresList: List<String>,
    val suitableFor: String = "Family & Working Professionals",
    val leaseDurationMonths: Int? = null,
    val isDepositRefundable: Boolean = true,
    val ownerName: String,
    /** Never populated in public listing documents; contact occurs through Ren Chat. */
    val ownerPhone: String = "",
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
    val status: String = "Pending Review",
    val ownerEmail: String = if (ownerName.isNotBlank()) "${ownerName.lowercase().replace(" ", "")}@ren.in" else ""
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
