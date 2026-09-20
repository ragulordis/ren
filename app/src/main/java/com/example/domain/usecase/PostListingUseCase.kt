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
    val furnishing: String = "",
    val securityDeposit: Long = 0L,
    val maintenanceAmount: Long = 0L,
    val isMaintenanceIncluded: Boolean = false,
    val availableFrom: String = "",
    val nearbyLandmark: String = "",
    val tenantPreferences: List<String> = emptyList(),
    val isPrivate: Boolean = false,
    val customImageResName: String? = null,
    val imageUri: Uri? = null,
    val imageUris: List<Uri> = emptyList()
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
    private val context: Context? = null,
    private val notificationRepository: com.example.data.repository.NotificationRepository? = null
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

        val initialProperty = Property(
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
            imageResName = params.customImageResName ?: fallbackImage,
            furnishing = params.furnishing,
            securityDeposit = params.securityDeposit,
            maintenanceAmount = params.maintenanceAmount,
            isMaintenanceIncluded = params.isMaintenanceIncluded,
            availableFrom = params.availableFrom,
            nearbyLandmark = params.nearbyLandmark,
            tenantPreferences = params.tenantPreferences,
            featuresList = params.features,
            ownerName = currentUser?.displayName?.ifBlank { "Property Owner" } ?: "Property Owner",
            // Contact information belongs to the private profile, not a public listing.
            ownerPhone = "",
            ownerType = currentUser?.role?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Owner",
            isPrivate = params.isPrivate,
            viewsCount = 0,
            savedCount = 0,
            messagesCount = 0,
            visitRequestsCount = 0,
            interestedBuyersCount = 0,
            status = "Pending Review"
        )

        // Create the pending listing first. Firebase Storage then authorizes each upload
        // using this owner-bound Firestore document.
        repository.addProperty(initialProperty)

        val requestedUris = (params.imageUris + listOfNotNull(params.imageUri)).distinct()
        val uploadedUrls = if (requestedUris.isNotEmpty() && storageService != null && context != null) {
            storageService.uploadPropertyPhotos(propId, requestedUris, context).getOrElse { uploadError ->
                // Do not leave a misleading, photo-less pending listing behind when
                // the user explicitly selected photos and their upload fails.
                runCatching { repository.deleteProperty(propId) }
                throw uploadError
            }
        } else {
            emptyList()
        }
        val newProperty = initialProperty.copy(
            imageUrls = uploadedUrls,
            imageResName = uploadedUrls.firstOrNull() ?: initialProperty.imageResName
        )
        if (uploadedUrls.isNotEmpty()) repository.updateProperty(newProperty)
        notificationRepository?.sendNotification(
            title = "Listing submitted for review",
            message = "'${newProperty.title}' is pending review. We will notify you when it is ready to appear on Ren.",
            type = com.example.data.model.NotificationType.NEW_LISTING,
            propertyId = newProperty.id,
            targetLocation = newProperty.location,
            actionText = "View Listing"
        )
        newProperty
    }
}
