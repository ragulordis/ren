package com.example.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.data.remote.StorageService
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository

data class PostListingParams(
    val title: String,
    val description: String,
    val price: Long,
    val marketEstimate: Long = price,
    val location: String,
    val category: PropertyCategory,
    val propertyType: String,
    val speed: SellingSpeed,
    val bedrooms: Int,
    val bathrooms: Int,
    val areaSqFt: Int,
    val features: List<String>,
    val isPrivate: Boolean = false,
    val customImageResName: String? = null,
    val imageUri: Uri? = null
)

/**
 * UseCase to validate and publish a new property listing.
 * Enforces production security: verification level starts at 0, no client self-approval,
 * metrics initialized to 0, owner bound to authenticated user.
 * Automatically uploads photo to Firebase Storage if an imageUri is provided.
 */
class PostListingUseCase(
    private val repository: PropertyRepository,
    private val authRepository: AuthRepository,
    private val storageService: StorageService? = null,
    private val context: Context? = null
) {
    suspend operator fun invoke(params: PostListingParams): Result<Property> = runCatching {
        require(params.title.isNotBlank()) { "Title cannot be empty" }
        require(params.price > 0) { "Price must be greater than zero" }
        require(params.location.isNotBlank()) { "Location is required" }

        val uid = authRepository.currentUserId()
            ?: return Result.failure(IllegalStateException("User must be signed in to post a listing"))

        val currentUser = authRepository.currentUser()

        val urgencyScore = when (params.speed) {
            SellingSpeed.URGENT -> 5
            SellingSpeed.FAST -> 4
            SellingSpeed.PRIVATE -> 4
            SellingSpeed.NORMAL -> 2
        }

        val listingType = when (params.category) {
            PropertyCategory.RENT -> ListingType.RENT
            PropertyCategory.LEASE -> ListingType.LEASE
            else -> if (params.speed == SellingSpeed.URGENT) ListingType.URGENT_SALE else ListingType.BUY
        }

        val fallbackImage = when (params.category) {
            PropertyCategory.LAND -> "prop_land_plot"
            PropertyCategory.RENT -> "prop_beach_serenity"
            PropertyCategory.COMMERCIAL -> "prop_villa_auroville"
            else -> "prop_house_kottakuppam"
        }

        val propId = "prop-${System.currentTimeMillis()}"

        // Upload custom photo if URI provided and storage service available
        val uploadedImageUrl = if (params.imageUri != null && storageService != null && context != null) {
            storageService.uploadPropertyPhoto(propId, params.imageUri, context).getOrNull()
        } else null

        val finalImageName = uploadedImageUrl
            ?: params.customImageResName
            ?: fallbackImage

        val newProperty = Property(
            id = propId,
            ownerId = uid,
            title = params.title.trim(),
            description = params.description.trim(),
            listingType = listingType,
            sellingSpeed = params.speed,
            category = params.category,
            propertyType = params.propertyType,
            price = params.price,
            originalPrice = params.marketEstimate,
            marketEstimate = params.marketEstimate,
            location = params.location.trim(),
            approximateArea = "Near ${params.location} Center (~500m)",
            distanceKm = 1.0,
            bedrooms = params.bedrooms,
            bathrooms = params.bathrooms,
            areaSqFt = params.areaSqFt,
            urgencyScore = urgencyScore,
            verificationLevel = 0, // Unverified draft/pending review - client NEVER self-approves!
            imageResName = finalImageName,
            featuresList = params.features,
            ownerName = currentUser?.displayName?.ifBlank { "Property Owner" } ?: "Property Owner",
            ownerPhone = currentUser?.phone?.ifBlank { "" } ?: "",
            ownerType = currentUser?.role?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Owner",
            isPrivate = params.isPrivate,
            viewsCount = 0,
            savedCount = 0,
            messagesCount = 0,
            visitRequestsCount = 0,
            interestedBuyersCount = 0
        )

        repository.addProperty(newProperty)
        newProperty
    }
}
