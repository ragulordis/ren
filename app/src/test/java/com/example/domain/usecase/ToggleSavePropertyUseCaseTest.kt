package com.example.domain.usecase

import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleSavePropertyUseCaseTest {

    private lateinit var fakeRepository: FakePropertyRepository
    private lateinit var useCase: ToggleSavePropertyUseCase

    @Before
    fun setUp() {
        fakeRepository = FakePropertyRepository()
        useCase = ToggleSavePropertyUseCase(fakeRepository)
    }

    @Test
    fun `toggling unsaved property saves it`() = runBlocking {
        val property = Property(
            id = "test-prop-1",
            ownerId = "owner-1",
            title = "Test House",
            description = "Description",
            listingType = ListingType.BUY,
            sellingSpeed = SellingSpeed.NORMAL,
            category = PropertyCategory.BUY,
            propertyType = "House",
            price = 2_000_000L,
            location = "Kottakuppam",
            approximateArea = "Center",
            distanceKm = 0.5,
            bedrooms = 2,
            bathrooms = 1,
            areaSqFt = 900,
            imageResName = "prop_house_kottakuppam",
            featuresList = emptyList(),
            ownerName = "Owner",
            isSaved = false
        )

        useCase(property)

        assertTrue(fakeRepository.savedPropertyIds.contains("test-prop-1"))
    }

    @Test
    fun `toggling saved property unsaves it`() = runBlocking {
        val property = Property(
            id = "test-prop-2",
            ownerId = "owner-2",
            title = "Saved House",
            description = "Description",
            listingType = ListingType.BUY,
            sellingSpeed = SellingSpeed.NORMAL,
            category = PropertyCategory.BUY,
            propertyType = "House",
            price = 2_000_000L,
            location = "Kottakuppam",
            approximateArea = "Center",
            distanceKm = 0.5,
            bedrooms = 2,
            bathrooms = 1,
            areaSqFt = 900,
            imageResName = "prop_house_kottakuppam",
            featuresList = emptyList(),
            ownerName = "Owner",
            isSaved = true
        )
        fakeRepository.savedPropertyIds.add("test-prop-2")

        useCase(property)

        assertFalse(fakeRepository.savedPropertyIds.contains("test-prop-2"))
    }
}
