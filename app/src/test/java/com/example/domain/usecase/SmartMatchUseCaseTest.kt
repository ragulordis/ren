package com.example.domain.usecase

import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.data.model.UserPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmartMatchUseCaseTest {

    private lateinit var fakeRepository: FakePropertyRepository
    private lateinit var useCase: SmartMatchUseCase

    @Before
    fun setUp() {
        fakeRepository = FakePropertyRepository()
        useCase = SmartMatchUseCase(fakeRepository)
    }

    @Test
    fun `smart match ranks higher for exact location and budget match`() = runBlocking {
        val perfectMatch = Property(
            id = "p1",
            title = "Kottakuppam 2BHK Home",
            description = "Near market",
            listingType = ListingType.BUY,
            sellingSpeed = SellingSpeed.URGENT,
            category = PropertyCategory.BUY,
            propertyType = "House",
            price = 3_000_000L,
            location = "Kottakuppam",
            approximateArea = "Center",
            distanceKm = 1.0,
            bedrooms = 2,
            bathrooms = 2,
            areaSqFt = 1200,
            imageResName = "prop_house_kottakuppam",
            featuresList = listOf("Water"),
            ownerName = "Owner"
        )

        val poorMatch = Property(
            id = "p2",
            title = "Auroville Farmland",
            description = "Far away",
            listingType = ListingType.BUY,
            sellingSpeed = SellingSpeed.NORMAL,
            category = PropertyCategory.LAND,
            propertyType = "Plot",
            price = 9_000_000L,
            location = "Auroville",
            approximateArea = "North",
            distanceKm = 15.0,
            bedrooms = 0,
            bathrooms = 0,
            areaSqFt = 5000,
            imageResName = "prop_land_plot",
            featuresList = listOf("Trees"),
            ownerName = "Owner"
        )

        fakeRepository.properties = listOf(poorMatch, perfectMatch)

        val prefs = UserPreferences(
            location = "Kottakuppam",
            maxBudget = 3_500_000L,
            preferredBedrooms = 2,
            propertyType = "House"
        )

        val results = useCase(prefs)

        assertEquals(2, results.size)
        // Highest match percentage should be first
        assertEquals("p1", results.first().property.id)
        assertTrue(results[0].matchScore.overallPercentage > results[1].matchScore.overallPercentage)
    }
}
