package com.example.data.model

data class UserPreferences(
    val maxBudget: Long = 4000000L, // Default ₹40 Lakhs
    val minBudget: Long = 0L,
    val location: String = "Kottakuppam",
    val propertyType: String = "All Types",
    val listingType: ListingType = ListingType.BUY,
    val preferredBedrooms: Int = 0 // 0 = Any
) {
    val formattedMaxBudget: String
        get() = formatIndianCurrency(maxBudget, listingType)

    val summaryText: String
        get() = buildString {
            if (maxBudget < Long.MAX_VALUE) {
                append("Budget ≤ $formattedMaxBudget")
            } else {
                append("Any Budget")
            }
            if (location.isNotBlank() && !location.equals("All", ignoreCase = true) && !location.equals("All Locations", ignoreCase = true)) {
                append(" • $location")
            }
            if (propertyType.isNotBlank() && !propertyType.equals("All", ignoreCase = true) && !propertyType.equals("All Types", ignoreCase = true)) {
                append(" • $propertyType")
            }
            if (preferredBedrooms > 0) {
                append(" • ${preferredBedrooms}+ BHK")
            }
        }
}

data class SmartMatchScore(
    val overallPercentage: Int, // 0 - 100%
    val budgetScore: Int, // 0 - 100%
    val locationScore: Int, // 0 - 100%
    val typeScore: Int, // 0 - 100%
    val urgencyBonus: Int, // 0 - 10%
    val matchReasons: List<String>,
    val bestFitHeadline: String
)

data class SmartMatchResult(
    val property: Property,
    val matchScore: SmartMatchScore,
    val isFromFirestore: Boolean = true
)

object SmartMatchEngine {

    /**
     * Smart Match Algorithm:
     * - Budget Score: 40% weight
     * - Location Score: 35% weight
     * - Property Type: 25% weight
     * - Urgency & Fast Deal Bonus: up to +10%
     */
    fun calculateScore(property: Property, preferences: UserPreferences): SmartMatchScore {
        val matchReasons = mutableListOf<String>()

        // 1. Budget Score (40% weight)
        val budgetScore: Int
        if (preferences.maxBudget <= 0L || preferences.maxBudget == Long.MAX_VALUE) {
            budgetScore = 95
            matchReasons.add("Fits within open budget")
        } else if (property.price <= preferences.maxBudget) {
            val ratio = property.price.toDouble() / preferences.maxBudget
            if (ratio >= 0.65) {
                budgetScore = 100
                matchReasons.add("Optimal budget utilization (${property.formattedPrice})")
            } else {
                budgetScore = 94
                val savings = preferences.maxBudget - property.price
                if (savings > 200000) {
                    matchReasons.add("Under budget by ${formatIndianCurrency(savings, property.listingType)}")
                } else {
                    matchReasons.add("Comfortably within your price target")
                }
            }
        } else {
            // Price exceeds budget
            val overage = property.price - preferences.maxBudget
            val overagePercent = (overage.toDouble() / preferences.maxBudget) * 100
            when {
                overagePercent <= 10.0 -> {
                    budgetScore = 78
                    matchReasons.add("Only ~${overagePercent.toInt()}% above target (high negotiation room)")
                }
                overagePercent <= 25.0 -> {
                    budgetScore = 60
                    matchReasons.add("Slight stretch budget (+${overagePercent.toInt()}%)")
                }
                overagePercent <= 50.0 -> {
                    budgetScore = 40
                }
                else -> {
                    budgetScore = 20
                }
            }
        }

        // 2. Location Score (35% weight)
        val locationScore: Int
        val targetLoc = preferences.location.trim()
        val isAllLocations = targetLoc.isBlank() || targetLoc.equals("All", ignoreCase = true) || targetLoc.equals("All Locations", ignoreCase = true)

        if (isAllLocations) {
            locationScore = 95
            matchReasons.add("Located in prime coastal corridor (${property.location})")
        } else if (property.location.equals(targetLoc, ignoreCase = true)) {
            // Exact location match
            locationScore = 100
            matchReasons.add("Exact location match in ${property.location} (${property.distanceKm} km away)")
        } else {
            // Nearby locations in Kottakuppam/Pondicherry/Auroville corridor
            val isCorridorNeighbor = when {
                targetLoc.contains("Kottakuppam", ignoreCase = true) &&
                        (property.location.contains("Pondicherry", ignoreCase = true) || property.location.contains("Auroville", ignoreCase = true)) -> true
                targetLoc.contains("Pondicherry", ignoreCase = true) &&
                        (property.location.contains("Kottakuppam", ignoreCase = true) || property.location.contains("White Town", ignoreCase = true)) -> true
                targetLoc.contains("Auroville", ignoreCase = true) &&
                        (property.location.contains("Kottakuppam", ignoreCase = true) || property.location.contains("Bommayapalayam", ignoreCase = true)) -> true
                else -> false
            }

            if (isCorridorNeighbor) {
                locationScore = 82
                matchReasons.add("Adjacent corridor in ${property.location} (~${property.distanceKm} km)")
            } else {
                val penalty = (property.distanceKm * 6).toInt()
                locationScore = (70 - penalty).coerceIn(25, 65)
            }
        }

        // 3. Property Type Score (25% weight)
        val typeScore: Int
        val targetType = preferences.propertyType.trim()
        val isAllTypes = targetType.isBlank() || targetType.equals("All", ignoreCase = true) || targetType.equals("All Types", ignoreCase = true)

        if (isAllTypes) {
            typeScore = 95
            matchReasons.add("Type: ${property.propertyType}")
        } else if (property.propertyType.equals(targetType, ignoreCase = true)) {
            typeScore = 100
            matchReasons.add("Exact property type: ${property.propertyType}")
        } else {
            val isCompatibleType = when {
                targetType.contains("House", ignoreCase = true) && property.propertyType.contains("Villa", ignoreCase = true) -> true
                targetType.contains("Villa", ignoreCase = true) && property.propertyType.contains("House", ignoreCase = true) -> true
                targetType.contains("Plot", ignoreCase = true) && property.category == PropertyCategory.LAND -> true
                targetType.contains("Land", ignoreCase = true) && property.category == PropertyCategory.LAND -> true
                else -> false
            }

            if (isCompatibleType) {
                typeScore = 85
                matchReasons.add("Close category match: ${property.propertyType}")
            } else {
                typeScore = 40
            }
        }

        // 4. Urgency & Fast-Deal Bonus (up to +10%)
        var urgencyBonus = 0
        if (property.sellingSpeed == SellingSpeed.URGENT) {
            urgencyBonus += 6
            matchReasons.add(0, "Urgent Sale: Owner ready for rapid closing")
        } else if (property.sellingSpeed == SellingSpeed.FAST) {
            urgencyBonus += 4
            matchReasons.add("Fast Sale deal with verified paperwork")
        }

        if (property.verificationLevel >= 3) {
            urgencyBonus += 3
            matchReasons.add("High verification level (${property.verificationLevel}/4)")
        }

        if (preferences.preferredBedrooms > 0) {
            if (property.bedrooms >= preferences.preferredBedrooms) {
                urgencyBonus += 3
                matchReasons.add("Meets bedroom preference (${property.bedrooms} BHK)")
            } else {
                urgencyBonus -= 4
            }
        }

        // Composite Weighted Score
        val rawScore = (budgetScore * 0.40f) + (locationScore * 0.35f) + (typeScore * 0.25f) + urgencyBonus
        val overallPercentage = rawScore.toInt().coerceIn(10, 99) // Realistic capped at 99%

        val bestFitHeadline = when {
            overallPercentage >= 90 -> "🎯 Exceptional Match"
            overallPercentage >= 80 -> "✨ Strong Candidate"
            overallPercentage >= 70 -> "👍 Good Alternative"
            else -> "📋 Potential Fit"
        }

        return SmartMatchScore(
            overallPercentage = overallPercentage,
            budgetScore = budgetScore,
            locationScore = locationScore,
            typeScore = typeScore,
            urgencyBonus = urgencyBonus,
            matchReasons = matchReasons.take(4),
            bestFitHeadline = bestFitHeadline
        )
    }
}
