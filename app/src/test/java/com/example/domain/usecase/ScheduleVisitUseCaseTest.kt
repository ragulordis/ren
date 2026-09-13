package com.example.domain.usecase

import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScheduleVisitUseCaseTest {

    private lateinit var fakeRepository: FakePropertyRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var useCase: ScheduleVisitUseCase
    private lateinit var sampleProperty: Property

    @Before
    fun setUp() {
        fakeRepository = FakePropertyRepository()
        authRepository = FakeAuthRepository()
        useCase = ScheduleVisitUseCase(fakeRepository, authRepository)
        sampleProperty = Property(
            id = "prop-123",
            title = "Beachside Villa",
            description = "Near shoreline",
            listingType = ListingType.BUY,
            sellingSpeed = SellingSpeed.URGENT,
            category = PropertyCategory.BUY,
            propertyType = "Villa",
            price = 4_500_000L,
            location = "Kottakuppam",
            approximateArea = "Beach Road",
            distanceKm = 0.5,
            bedrooms = 3,
            bathrooms = 2,
            areaSqFt = 1500,
            imageResName = "prop_house_kottakuppam",
            featuresList = listOf("Sea view"),
            ownerName = "Owner"
        )
    }

    @Test
    fun `successful visit scheduling initializes with Requested status`() = runBlocking {
        val result = useCase(
            property = sampleProperty,
            date = "Tomorrow",
            timeSlot = "10:00 AM - 11:00 AM"
        )

        assertTrue(result.isSuccess)
        val visit = result.getOrThrow()
        assertEquals("prop-123", visit.propertyId)
        assertEquals("Beachside Villa", visit.propertyTitle)
        assertEquals("Requested", visit.status)
        assertEquals(1, fakeRepository.visits.size)
    }
}
