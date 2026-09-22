package com.example.cuj

import com.example.data.model.ListingStatus
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.PropertyVisit
import com.example.data.model.SellingSpeed
import com.example.data.model.User
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.domain.usecase.FakeAuthRepository
import com.example.domain.usecase.FakePropertyRepository
import com.example.domain.usecase.PostListingParams
import com.example.domain.usecase.PostListingUseCase
import com.example.domain.usecase.ScheduleVisitUseCase
import com.example.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * P5.9 Critical User Journey (CUJ) Certification Test Suite.
 * Certifies Buyer, Seller, Auth, and Failure journeys for Ren 1.0.0 Production Release.
 */
class RenProductionCertificationCujTest {

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var propertyRepository: FakePropertyRepository
    private lateinit var postListingUseCase: PostListingUseCase
    private lateinit var scheduleVisitUseCase: ScheduleVisitUseCase
    private lateinit var sendChatMessageUseCase: SendChatMessageUseCase

    @Before
    fun setup() {
        authRepository = FakeAuthRepository()
        propertyRepository = FakePropertyRepository()
        postListingUseCase = PostListingUseCase(propertyRepository, authRepository)
        scheduleVisitUseCase = ScheduleVisitUseCase(propertyRepository, authRepository)
        sendChatMessageUseCase = SendChatMessageUseCase(propertyRepository, authRepository)
    }

    @Test
    fun `CUJ 1 - Authenticated Seller can post property and transition its lifecycle to SOLD`() = runTest {
        // 1. Seller Signs in
        authRepository.fakeUser = User(uid = "seller_456", email = "seller@example.com")
        authRepository.fakeProfile = UserProfile(
            uid = "seller_456",
            email = "seller@example.com",
            displayName = "Seller Bob",
            role = UserRole.OWNER
        )

        // 2. Seller posts a new listing
        val postResult = postListingUseCase(
            PostListingParams(
                title = "Sea View Villa",
                description = "Luxury beachfront property",
                price = 15000000L,
                marketEstimate = 16000000L,
                location = "East Coast Road",
                category = PropertyCategory.BUY,
                propertyType = "Villa",
                speed = SellingSpeed.FAST,
                bedrooms = 4,
                bathrooms = 4,
                areaSqFt = 3200,
                features = listOf("Pool", "Sea View", "Garden")
            )
        )

        assertTrue("Listing creation should succeed", postResult.isSuccess)
        val createdProperty = postResult.getOrThrow()
        assertEquals("seller_456", createdProperty.ownerId)
        assertEquals("Sea View Villa", createdProperty.title)

        // 3. Verify property exists in repository
        val allProps = propertyRepository.allProperties.first()
        assertTrue(allProps.any { it.id == createdProperty.id })

        // 4. Seller transitions listing from ACTIVE to SOLD
        propertyRepository.updatePropertyStatus(createdProperty.id, ListingStatus.SOLD.value)
        val updatedProps = propertyRepository.allProperties.first()
        val soldProperty = updatedProps.first { it.id == createdProperty.id }
        assertEquals(ListingStatus.SOLD.value, soldProperty.status)
    }

    @Test
    fun `CUJ 2 - Authenticated Buyer can explore, save, schedule visit, and chat with seller`() = runTest {
        // 1. Setup existing property
        val prop = Property(
            id = "prop-200",
            ownerId = "seller_456",
            title = "Heritage French Villa",
            description = "Colonial style villa in White Town",
            listingType = ListingType.BUY,
            sellingSpeed = SellingSpeed.NORMAL,
            category = PropertyCategory.BUY,
            propertyType = "Villa",
            price = 25000000L,
            location = "White Town",
            approximateArea = "Near Promenade",
            distanceKm = 0.5,
            bedrooms = 3,
            bathrooms = 3,
            areaSqFt = 2400,
            imageResName = "prop_villa",
            featuresList = listOf("Heritage", "Terrace"),
            ownerName = "Bob"
        )
        propertyRepository.addProperty(prop)

        // 2. Buyer signs in
        authRepository.fakeUser = User(uid = "buyer_123", email = "buyer@example.com")
        authRepository.fakeProfile = UserProfile(
            uid = "buyer_123",
            email = "buyer@example.com",
            displayName = "Alice Buyer",
            role = UserRole.BUYER
        )

        // 3. Buyer saves property
        propertyRepository.toggleSave(prop.id, false)
        val saved = propertyRepository.savedProperties.first()
        assertTrue("Property must be in saved list", saved.any { it.id == prop.id })

        // 4. Buyer schedules a visit
        val visit = PropertyVisit(
            id = "visit-1",
            propertyId = prop.id,
            propertyTitle = prop.title,
            location = prop.location,
            buyerId = "buyer_123",
            sellerId = prop.ownerId,
            buyerName = "Alice Buyer",
            date = "2026-10-01",
            timeSlot = "10:00 AM",
            status = "Requested"
        )
        propertyRepository.scheduleVisit(visit)
        val visits = propertyRepository.allVisits.first()
        assertEquals(1, visits.size)
        assertEquals("buyer_123", visits.first().buyerId)
        assertEquals("seller_456", visits.first().sellerId)

        // 5. Seller transitions visit status from Requested to Confirmed
        propertyRepository.updateVisitStatus(visit.id, com.example.data.model.VisitStatus.CONFIRMED.value)
        val updatedVisits = propertyRepository.allVisits.first()
        assertEquals(com.example.data.model.VisitStatus.CONFIRMED.value, updatedVisits.first().status)

        // 6. Buyer sends inquiry message
        val chatResult = sendChatMessageUseCase.sendMessage(prop.id, "Is this villa still available?")
        assertTrue("Chat message transmission should succeed", chatResult.isSuccess)
        val sentMsg = chatResult.getOrThrow()
        assertEquals("buyer_123", sentMsg.senderId)
        assertEquals("Is this villa still available?", sentMsg.message)
    }

    @Test
    fun `CUJ 3 - Unauthenticated user cannot post listing`() = runTest {
        // User not signed in
        authRepository.fakeUser = null
        authRepository.fakeProfile = null

        val postResult = postListingUseCase(
            PostListingParams(
                title = "Apartment",
                description = "Test description",
                price = 5000000L,
                location = "Pondicherry",
                category = PropertyCategory.BUY,
                propertyType = "Apartment",
                speed = SellingSpeed.NORMAL,
                bedrooms = 2,
                bathrooms = 2,
                areaSqFt = 900,
                features = emptyList()
            )
        )

        assertTrue("Unauthenticated post must fail", postResult.isFailure)
        assertNotNull(postResult.exceptionOrNull())
    }

    @Test
    fun `CUJ 4 - Moderation First - New listings are flagged PENDING REVIEW to protect public`() = runTest {
        authRepository.fakeUser = User(uid = "seller_789", email = "landowner@example.com")
        authRepository.fakeProfile = UserProfile(uid = "seller_789", email = "landowner@example.com", displayName = "Ravi Landowner")

        val postResult = postListingUseCase(
            PostListingParams(
                title = "DTCP Approved Residential Plot",
                description = "Clear title deed, ready for registration",
                price = 3200000L,
                location = "Kottakuppam, Pondicherry",
                category = PropertyCategory.LAND,
                propertyType = "Residential Plot",
                speed = SellingSpeed.NORMAL,
                bedrooms = 0,
                bathrooms = 0,
                areaSqFt = 2178,
                features = listOf("DTCP Approved", "Tar Road Access", "Facing: North")
            )
        )

        assertTrue(postResult.isSuccess)
        val listing = postResult.getOrThrow()
        assertEquals(ListingStatus.PENDING_REVIEW.value, listing.status)
        assertEquals(PropertyCategory.LAND, listing.category)
        assertEquals(0, listing.bedrooms)
        assertEquals(0, listing.bathrooms)
    }

    @Test
    fun `CUJ 5 - Authenticated Landowner can list Land for Sale with Cents and Approvals`() = runTest {
        authRepository.fakeUser = User(uid = "seller_land_1", email = "land@example.com")
        authRepository.fakeProfile = UserProfile(uid = "seller_land_1", email = "land@example.com", displayName = "Mr. Murugan")

        val postResult = postListingUseCase(
            PostListingParams(
                title = "Prime 5 Cents Plot on 40ft Main Road",
                description = "Clear Patta, boundary wall constructed, sweet drinking water source.",
                price = 4500000L,
                location = "Auroville Road, Pondicherry",
                category = PropertyCategory.LAND,
                propertyType = "Residential Plot",
                speed = SellingSpeed.NORMAL,
                bedrooms = 0,
                bathrooms = 0,
                areaSqFt = 2178, // ~5 cents
                features = listOf("DTCP Approved", "Clear Patta / EC", "Tar Road Access", "Compound Wall / Fenced", "Facing: East", "40 ft Road")
            )
        )

        assertTrue(postResult.isSuccess)
        val landListing = postResult.getOrThrow()
        assertEquals(ListingType.BUY, landListing.listingType) // Land is for sale
        assertEquals(PropertyCategory.LAND, landListing.category)
        assertEquals(2178, landListing.areaSqFt)
        assertTrue(landListing.featuresList.contains("DTCP Approved"))
        assertTrue(landListing.featuresList.contains("Facing: East"))
    }

    @Test
    fun `CUJ 6 - Authenticated Homeowner can list Home for Rent with Deposit and Maintenance`() = runTest {
        authRepository.fakeUser = User(uid = "seller_rent_1", email = "landlord@example.com")
        authRepository.fakeProfile = UserProfile(uid = "seller_rent_1", email = "landlord@example.com", displayName = "Sita Landlord")

        val postResult = postListingUseCase(
            PostListingParams(
                title = "Spacious 2 BHK Apartment for Rent near Serenity Beach",
                description = "East facing, 24/7 water supply, covered car parking, calm residential neighborhood.",
                price = 18000L,
                location = "Kottakuppam, Pondicherry",
                category = PropertyCategory.RENT,
                propertyType = "Apartment",
                speed = SellingSpeed.NORMAL,
                bedrooms = 2,
                bathrooms = 2,
                areaSqFt = 1150,
                features = listOf("24/7 Water", "EB Connection", "Car Parking", "Balcony"),
                furnishing = "Semi-furnished",
                securityDeposit = 100000L,
                maintenanceAmount = 1500L,
                isMaintenanceIncluded = false,
                availableFrom = "Available now",
                nearbyLandmark = "Near Beach Arch",
                tenantPreferences = listOf("Family", "Working professionals")
            )
        )

        assertTrue(postResult.isSuccess)
        val rentalListing = postResult.getOrThrow()
        assertEquals(ListingType.RENT, rentalListing.listingType)
        assertEquals(PropertyCategory.RENT, rentalListing.category)
        assertEquals(18000L, rentalListing.price)
        assertEquals(100000L, rentalListing.securityDeposit)
        assertEquals(1500L, rentalListing.maintenanceAmount)
        assertEquals(false, rentalListing.isMaintenanceIncluded)
        assertEquals("Semi-furnished", rentalListing.furnishing)
        assertEquals(2, rentalListing.bedrooms)
        assertEquals(2, rentalListing.bathrooms)
        assertEquals(2, rentalListing.tenantPreferences.size)
        assertTrue(rentalListing.tenantPreferences.contains("Family"))
    }
}
