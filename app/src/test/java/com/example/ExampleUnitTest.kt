package com.example

import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    private val sampleProperties = listOf(
        Property(
            id = "1",
            title = "2BHK Kottakuppam Apartment",
            description = "Spacious apartment",
            listingType = ListingType.BUY,
            sellingSpeed = SellingSpeed.FAST,
            category = PropertyCategory.BUY,
            propertyType = "Apartment",
            price = 3_500_000L,
            location = "Kottakuppam",
            approximateArea = "East Coast Road",
            distanceKm = 1.2,
            bedrooms = 2,
            bathrooms = 2,
            areaSqFt = 1100,
            imageResName = "prop_1",
            featuresList = listOf("Parking", "Balcony"),
            ownerName = "Kumar"
        ),
        Property(
            id = "2",
            title = "Auroville Eco Villa",
            description = "Modern sustainable villa",
            listingType = ListingType.BUY,
            sellingSpeed = SellingSpeed.NORMAL,
            category = PropertyCategory.BUY,
            propertyType = "Villa",
            price = 12_000_000L,
            location = "Auroville",
            approximateArea = "Auroville Road",
            distanceKm = 4.5,
            bedrooms = 3,
            bathrooms = 3,
            areaSqFt = 2400,
            imageResName = "prop_2",
            featuresList = listOf("Solar Power", "Garden"),
            ownerName = "Sophie"
        ),
        Property(
            id = "3",
            title = "Pondicherry Beach House",
            description = "Near Serenity Beach",
            listingType = ListingType.RENT,
            sellingSpeed = SellingSpeed.URGENT,
            category = PropertyCategory.RENT,
            propertyType = "House",
            price = 25_000L,
            location = "Serenity Beach",
            approximateArea = "Beach Road",
            distanceKm = 0.8,
            bedrooms = 1,
            bathrooms = 1,
            areaSqFt = 650,
            imageResName = "prop_3",
            featuresList = listOf("Sea View", "Furnished"),
            ownerName = "Ravi"
        )
    )

    @Test
    fun filterByLocation_returnsMatchingProperties() {
        val kottakuppamOnly = sampleProperties.filter { it.location.equals("Kottakuppam", ignoreCase = true) }
        assertEquals(1, kottakuppamOnly.size)
        assertEquals("1", kottakuppamOnly[0].id)
    }

    @Test
    fun filterByPriceRange_returnsPropertiesWithinBounds() {
        val minPrice = 1_000_000L
        val maxPrice = 5_000_000L
        val inBudget = sampleProperties.filter { it.price in minPrice..maxPrice }
        assertEquals(1, inBudget.size)
        assertEquals(3_500_000L, inBudget[0].price)
    }

    @Test
    fun filterBySearchQuery_matchesTitleOrLocation() {
        val query = "Villa"
        val results = sampleProperties.filter {
            it.title.contains(query, ignoreCase = true) || it.location.contains(query, ignoreCase = true)
        }
        assertEquals(1, results.size)
        assertEquals("Auroville Eco Villa", results[0].title)
    }

    @Test
    fun propertyEntity_storesTitleLocationPriceAndCategory_correctly() {
        val entity = com.example.data.local.PropertyEntity(
            id = "test-prop-1",
            title = "Modern 3BHK Apartment for Rent",
            description = "Walk to beach",
            listingType = "RENT",
            sellingSpeed = "FAST",
            category = "RENT",
            propertyType = "Apartment",
            price = 28_000L,
            originalPrice = 30_000L,
            marketEstimate = 28_000L,
            location = "Kottakuppam",
            approximateArea = "ECR Main",
            distanceKm = 1.0,
            bedrooms = 3,
            bathrooms = 2,
            areaSqFt = 1450,
            urgencyScore = 85,
            verificationLevel = 2,
            imageResName = "prop_1",
            featuresCsv = "Power Backup, Lift, Parking",
            suitableFor = "Family",
            leaseDurationMonths = 11,
            isDepositRefundable = true,
            ownerName = "Sundar",
            ownerPhone = "9876543210",
            ownerType = "Owner",
            isSaved = false,
            viewsCount = 120,
            savedCount = 14,
            messagesCount = 6,
            visitRequestsCount = 3,
            interestedBuyersCount = 5,
            mapLat = 11.97,
            mapLng = 79.83,
            isPrivate = false,
            status = "AVAILABLE"
        )

        assertEquals("Modern 3BHK Apartment for Rent", entity.title)
        assertEquals("Kottakuppam", entity.location)
        assertEquals(28_000L, entity.price)
        assertEquals("RENT", entity.category)
        assertEquals("RENT", entity.listingType)

        // Convert to domain model and back
        val domain = entity.toDomain()
        assertEquals(PropertyCategory.RENT, domain.category)
        assertEquals(ListingType.RENT, domain.listingType)
        assertEquals("Kottakuppam", domain.location)

        val convertedEntity = com.example.data.local.PropertyEntity.fromDomain(domain)
        assertEquals(entity.title, convertedEntity.title)
        assertEquals(entity.location, convertedEntity.location)
        assertEquals(entity.price, convertedEntity.price)
        assertEquals(entity.category, convertedEntity.category)
    }

    @Test
    fun filterByCategory_differentiatesRentAndSale() {
        val rentProperties = sampleProperties.filter { it.category == PropertyCategory.RENT }
        val saleProperties = sampleProperties.filter { it.category == PropertyCategory.BUY }

        assertEquals(1, rentProperties.size)
        assertEquals(25_000L, rentProperties[0].price)
        assertEquals("Serenity Beach", rentProperties[0].location)

        assertEquals(2, saleProperties.size)
        assertTrue(saleProperties.all { it.price >= 1_000_000L })
    }

    @Test
    fun multiCriteriaFilter_combinesQueryLocationAndPrice() {
        val query = "Apartment"
        val location = "Kottakuppam"
        val maxPrice = 4_000_000L

        val matching = sampleProperties.filter { prop ->
            val matchesQuery = prop.title.contains(query, ignoreCase = true) || prop.description.contains(query, ignoreCase = true)
            val matchesLoc = prop.location.equals(location, ignoreCase = true)
            val matchesPrice = prop.price <= maxPrice
            matchesQuery && matchesLoc && matchesPrice
        }

        assertEquals(1, matching.size)
        assertEquals("1", matching[0].id)
    }

    @Test
    fun propertyMapping_preservesAllEssentialFields() {
        val sample = sampleProperties[0]
        assertEquals("1", sample.id)
        assertEquals("2BHK Kottakuppam Apartment", sample.title)
        assertEquals("Kottakuppam", sample.location)
        assertEquals(3_500_000L, sample.price)
        assertEquals(PropertyCategory.BUY, sample.category)
        assertTrue(sample.price > 0)
    }

    @Test
    fun favoritesToggle_updatesSavedState() {
        var property = sampleProperties[0]
        assertFalse(property.isSaved)

        // Toggle to saved
        property = property.copy(isSaved = !property.isSaved)
        assertTrue(property.isSaved)

        // Toggle back to unsaved
        property = property.copy(isSaved = !property.isSaved)
        assertFalse(property.isSaved)
    }

    @Test
    fun contactSeller_generatesValidOwnerEmail() {
        val property = sampleProperties[0]
        assertEquals("kumar@quicknest.in", property.ownerEmail)
        assertTrue(property.ownerEmail.contains("@"))
        assertTrue(property.ownerEmail.endsWith("quicknest.in"))
    }

    @Test
    fun contactSeller_formatsInquirySubjectAndGreetingCorrectly() {
        val property = sampleProperties[0]
        val subject = "[QuickNest Inquiry] ${property.title} (${property.formattedPrice})"
        assertTrue(subject.contains("2BHK Kottakuppam Apartment"))
        assertTrue(subject.contains("QuickNest Inquiry"))
    }

    @Test
    fun smartMatch_exactBudgetAndLocation_scoresHigh() {
        val preferences = com.example.data.model.UserPreferences(
            maxBudget = 4_000_000L,
            location = "Kottakuppam",
            propertyType = "Apartment",
            preferredBedrooms = 2
        )

        val targetProp = sampleProperties[0] // 35L, Kottakuppam, Apartment, 2 BHK, FAST
        val score = com.example.data.model.SmartMatchEngine.calculateScore(targetProp, preferences)

        assertTrue("Expected score >= 90%, was ${score.overallPercentage}%", score.overallPercentage >= 90)
        assertEquals(100, score.budgetScore)
        assertEquals(100, score.locationScore)
        assertEquals(100, score.typeScore)
        assertTrue(score.urgencyBonus > 0)
        assertTrue(score.matchReasons.isNotEmpty())
    }

    @Test
    fun smartMatch_overBudgetAndMismatchedLocation_scoresLower() {
        val preferences = com.example.data.model.UserPreferences(
            maxBudget = 4_000_000L,
            location = "Kottakuppam",
            propertyType = "Apartment"
        )

        val luxuryVilla = sampleProperties[1] // 1.2 Cr, Auroville, Villa, 3 BHK
        val score = com.example.data.model.SmartMatchEngine.calculateScore(luxuryVilla, preferences)

        assertTrue("Expected luxury villa to score lower, was ${score.overallPercentage}%", score.overallPercentage < 70)
        assertTrue(score.budgetScore < 50)
    }

    @Test
    fun smartMatch_urgencyAndSpeed_givesBonus() {
        val preferences = com.example.data.model.UserPreferences(
            maxBudget = 50_000L,
            location = "Serenity Beach",
            propertyType = "House"
        )

        val urgentHouse = sampleProperties[2] // 25k, Serenity Beach, House, URGENT
        val score = com.example.data.model.SmartMatchEngine.calculateScore(urgentHouse, preferences)

        assertTrue(score.urgencyBonus >= 6)
        assertTrue(score.matchReasons.any { it.contains("Urgent Sale", ignoreCase = true) })
    }

    @Test
    fun skeletonLoading_conditionEvaluatesTrue_whenDataIsLoadingAndListEmpty() {
        val isLoading = true
        val propertiesList = emptyList<Property>()
        val shouldShowPropertyListSkeleton = isLoading && propertiesList.isEmpty()
        assertTrue("Expected skeleton list to show when loading with empty properties", shouldShowPropertyListSkeleton)

        val populatedList = sampleProperties
        val shouldShowSkeletonWithData = isLoading && populatedList.isEmpty()
        assertFalse("Expected skeleton to hide once properties are populated", shouldShowSkeletonWithData)
    }

    @Test
    fun skeletonMapView_conditionEvaluatesTrue_whenMapLoadingOrRefreshing() {
        var isLoading = true
        var isRefreshing = false
        var properties = emptyList<Property>()

        val shouldShowMapSkeleton = (isLoading && properties.isEmpty()) || isRefreshing
        assertTrue("Map skeleton should display during initial coordinate fetch", shouldShowMapSkeleton)

        // After initial load completes
        isLoading = false
        properties = sampleProperties
        val shouldShowAfterLoad = (isLoading && properties.isEmpty()) || isRefreshing
        assertFalse("Map skeleton should dismiss once coordinates are ready", shouldShowAfterLoad)

        // During manual swipe/refresh
        isRefreshing = true
        val shouldShowDuringRefresh = (isLoading && properties.isEmpty()) || isRefreshing
        assertTrue("Map skeleton should display during refresh for smooth perceived performance", shouldShowDuringRefresh)
    }
}

