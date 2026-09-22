package com.example.domain.usecase

import com.example.data.model.BudgetFilter
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SortOption

data class FilterCriteria(
    val query: String = "",
    val category: PropertyCategory = PropertyCategory.ALL,
    val propertyType: String? = null,
    val location: String = "All",
    val budget: BudgetFilter = BudgetFilter.ALL,
    val bedrooms: Int = 0,
    val sortOption: SortOption = SortOption.URGENCY,
    val verifiedOnly: Boolean = false,
    val urgentOnly: Boolean = false,
    val minPrice: Long? = null,
    val maxPrice: Long? = null
)

/**
 * Pure domain UseCase for filtering, searching, and sorting properties.
 */
class FilterPropertiesUseCase {

    operator fun invoke(
        properties: List<Property>,
        criteria: FilterCriteria
    ): List<Property> {
        return properties
            .asSequence()
            .filter { prop ->
                // Category filter
                if (criteria.category != PropertyCategory.ALL) {
                    prop.category == criteria.category
                } else true
            }
            .filter { prop ->
                // Property type filter
                if (!criteria.propertyType.isNullOrBlank() && criteria.propertyType != "All") {
                    val filterType = criteria.propertyType
                    if (filterType.contains("Plot", ignoreCase = true) || filterType.contains("Land", ignoreCase = true)) {
                        prop.propertyType.contains("Plot", ignoreCase = true) ||
                        prop.propertyType.contains("Land", ignoreCase = true) ||
                        prop.category == PropertyCategory.LAND
                    } else {
                        prop.propertyType.contains(filterType, ignoreCase = true)
                    }
                } else true
            }
            .filter { prop ->
                // Location filter
                if (criteria.location != "All" && criteria.location != "All Locations" && criteria.location.isNotBlank()) {
                    prop.location.contains(criteria.location, ignoreCase = true)
                } else true
            }
            .filter { prop ->
                // Budget filter
                val fitsPreset = when (criteria.budget) {
                    BudgetFilter.ALL -> true
                    else -> {
                        val min = criteria.budget.minPrice
                        val max = criteria.budget.maxPrice
                        prop.price in min..max
                    }
                }
                val minP = criteria.minPrice ?: 0L
                val maxP = criteria.maxPrice ?: Long.MAX_VALUE
                val fitsManual = prop.price in minP..maxP
                fitsPreset && fitsManual
            }
            .filter { prop ->
                // Bedrooms filter
                if (criteria.bedrooms > 0) {
                    prop.bedrooms >= criteria.bedrooms
                } else true
            }
            .filter { prop ->
                // Verified filter
                if (criteria.verifiedOnly) prop.isVerified else true
            }
            .filter { prop ->
                // Urgency filter
                if (criteria.urgentOnly) prop.isUrgent else true
            }
            .filter { prop ->
                // Query search
                if (criteria.query.isNotBlank()) {
                    val q = criteria.query.trim().lowercase()
                    prop.title.lowercase().contains(q) ||
                    prop.location.lowercase().contains(q) ||
                    prop.propertyType.lowercase().contains(q) ||
                    prop.description.lowercase().contains(q) ||
                    prop.featuresList.any { it.lowercase().contains(q) }
                } else true
            }
            .sortedWith { a, b ->
                when (criteria.sortOption) {
                    SortOption.URGENCY -> b.urgencyScore.compareTo(a.urgencyScore)
                    SortOption.PRICE_LOW_HIGH -> a.price.compareTo(b.price)
                    SortOption.PRICE_HIGH_LOW -> b.price.compareTo(a.price)
                    SortOption.AREA_HIGH_LOW -> b.areaSqFt.compareTo(a.areaSqFt)
                }
            }
            .toList()
    }
}
