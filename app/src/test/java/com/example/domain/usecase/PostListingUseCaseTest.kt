package com.example.domain.usecase

import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostListingUseCaseTest {

    private lateinit var fakeRepository: FakePropertyRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var useCase: PostListingUseCase

    @Before
    fun setUp() {
        fakeRepository = FakePropertyRepository()
        authRepository = FakeAuthRepository()
        useCase = PostListingUseCase(fakeRepository, authRepository)
    }

    @Test
    fun `successful listing creation enforces verification level 0`() = runBlocking {
        val params = PostListingParams(
            title = "New East Coast Villa",
            description = "Spacious 3BHK villa close to the beach",
            price = 5_500_000L,
            location = "Kottakuppam",
            category = PropertyCategory.BUY,
            propertyType = "Villa",
            speed = SellingSpeed.URGENT,
            bedrooms = 3,
            bathrooms = 3,
            areaSqFt = 2200,
            features = listOf("Garden", "Solar Power", "Borewell")
        )

        val result = useCase(params)
        assertTrue(result.isSuccess)

        val property = result.getOrThrow()
        assertEquals("New East Coast Villa", property.title)
        assertEquals(5_500_000L, property.price)
        assertEquals(0, property.verificationLevel) // Must be unverified draft
        assertEquals(0, property.viewsCount)
        assertEquals(0, property.savedCount)
        assertEquals(5, property.urgencyScore) // URGENT speed maps to 5
        assertEquals(1, fakeRepository.addedProperties.size)
    }

    @Test
    fun `empty title returns failure`() = runBlocking {
        val params = PostListingParams(
            title = "   ",
            description = "Some description",
            price = 1_000_000L,
            location = "Kottakuppam",
            category = PropertyCategory.BUY,
            propertyType = "House",
            speed = SellingSpeed.NORMAL,
            bedrooms = 2,
            bathrooms = 1,
            areaSqFt = 800,
            features = emptyList()
        )

        val result = useCase(params)
        assertTrue(result.isFailure)
        assertEquals("Title cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `zero or negative price returns failure`() = runBlocking {
        val params = PostListingParams(
            title = "Valid Title",
            description = "Some description",
            price = 0L,
            location = "Kottakuppam",
            category = PropertyCategory.BUY,
            propertyType = "House",
            speed = SellingSpeed.NORMAL,
            bedrooms = 2,
            bathrooms = 1,
            areaSqFt = 800,
            features = emptyList()
        )

        val result = useCase(params)
        assertTrue(result.isFailure)
        assertEquals("Price must be greater than zero", result.exceptionOrNull()?.message)
    }
}
