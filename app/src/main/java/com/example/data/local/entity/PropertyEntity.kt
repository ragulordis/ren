package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "properties",
    indices = [
        Index(value = ["location"]),
        Index(value = ["category"]),
        Index(value = ["listingType"]),
        Index(value = ["price"])
    ]
)
data class PropertyEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val listingType: String,
    val sellingSpeed: String,
    val category: String,
    val propertyType: String,
    val price: Long,
    val originalPrice: Long,
    val marketEstimate: Long,
    val location: String,
    val approximateArea: String,
    val distanceKm: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val areaSqFt: Int,
    val urgencyScore: Int,
    val verificationLevel: Int,
    val imageResName: String,
    val imageUrlsCsv: String = "",
    val furnishing: String = "",
    val securityDeposit: Long = 0L,
    val maintenanceAmount: Long = 0L,
    val isMaintenanceIncluded: Boolean = false,
    val availableFrom: String = "",
    val nearbyLandmark: String = "",
    val tenantPreferencesCsv: String = "",
    val featuresCsv: String,
    val suitableFor: String,
    val leaseDurationMonths: Int?,
    val isDepositRefundable: Boolean,
    val ownerName: String,
    val ownerPhone: String,
    val ownerType: String,
    val isSaved: Boolean,
    val viewsCount: Int,
    val savedCount: Int,
    val messagesCount: Int,
    val visitRequestsCount: Int,
    val interestedBuyersCount: Int,
    val mapLat: Double,
    val mapLng: Double,
    val isPrivate: Boolean,
    val status: String
)
