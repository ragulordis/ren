package com.example.domain.usecase

import com.example.data.model.BudgetFilter
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.data.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FilterPropertiesUseCaseTest {

    private lateinit var useCase: FilterPropertiesUseCase
    private lateinit var sampleProperties: List<Property>

    @Before
    fun setUp() {
        useCase = FilterPropertiesUseCase()
        sampleProperties = listOf(
            createTestProperty(
                id = "p1",
                title = "2BHK Kottakuppam Villa",
                price = 3_000_000L,
                location = "Kottakuppam",
                category = PropertyCategory.BUY,
                propertyType = "Villa",
                bedrooms = 2,
                urgencyScore = 5,
                verificationLevel = 3,
                sellingSpeed = SellingSpeed.URGENT
            ),
            createTestProperty(
                id = "p2",
                title = "3BHK Beach House",
                price = 7_000_000L,
                location = "Auroville",
                category = PropertyCategory.BUY,
                propertyType = "House",
                bedrooms = 3,
                urgencyScore = 2,
                verificationLevel = 1,
                sellingSpeed = SellingSpeed.NORMAL
            ),
            createTestProperty(
                id = "p3",
                title = "Studio Beach Serenity",
                price = 12_000L,
                location = "Pondicherry Beach",
                category = PropertyCategory.RENT,
                propertyType = "Studio",
                bedrooms = 1,
                urgencyScore = 4,
                verificationLevel = 2,
                sellingSpeed = SellingSpeed.FAST
            )
        )
    }

    @Test
    fun `test filter by category BUY`() {
        val criteria = FilterCriteria(category = PropertyCategory.BUY)
        val result = useCase(sampleProperties, criteria)

        assertEquals(2, result.size)
        assertTrue(result.all { it.category == PropertyCategory.BUY })
    }

    @Test
    fun `test filter by location`() {
        val criteria = FilterCriteria(location = "Auroville")
        val result = useCase(sampleProperties, criteria)

        assertEquals(1, result.size)
        assertEquals("p2", result.first().id)
    }

    @Test
    fun `test filter by verified only`() {
        val criteria = FilterCriteria(verifiedOnly = true)
        val result = useCase(sampleProperties, criteria)

        // p1 (level 3) and p3 (level 2) are verified, p2 (level 1) is not
        assertEquals(2, result.size)
        assertTrue(result.all { it.isVerified })
    }

    @Test
    fun `test filter by urgent only`() {
        val criteria = FilterCriteria(urgentOnly = true)
        val result = useCase(sampleProperties, criteria)

        // p1 (urgency 5) and p3 (urgency 4) are urgent
        assertEquals(2, result.size)
        assertTrue(result.all { it.isUrgent })
    }

    @Test
    fun `test search query matches title or features`() {
        val criteria = FilterCriteria(query = "Villa")
        val result = useCase(sampleProperties, criteria)

        assertEquals(1, result.size)
        assertEquals("p1", result.first().id)
    }

    @Test
    fun `test sort by price low to high`() {
        val criteria = FilterCriteria(sortOption = SortOption.PRICE_LOW_HIGH)
        val result = useCase(sampleProperties, criteria)

        assertEquals(3, result.size)
        assertEquals("p3", result[0].id) // 12,000
        assertEquals("p1", result[1].id) // 30,00,000
        assertEquals("p2", result[2].id) // 70,00,000
    }

    private fun createTestProperty(
        id: String,
        title: String,
        price: Long,
        location: String,
        category: PropertyCategory,
        propertyType: String,
        bedrooms: Int,
        urgencyScore: Int,
        verificationLevel: Int,
        sellingSpeed: SellingSpeed
    ): Property {
        return Property(
            id = id,
            ownerId = "owner-test",
            title = title,
            description = "Test description",
            listingType = if (category == PropertyCategory.RENT) ListingType.RENT else ListingType.BUY,
            sellingSpeed = sellingSpeed,
            category = category,
            propertyType = propertyType,
            price = price,
            location = location,
            approximateArea = "Test Area",
            distanceKm = 1.0,
            bedrooms = bedrooms,
            bathrooms = 1,
            areaSqFt = 1000,
            urgencyScore = urgencyScore,
            verificationLevel = verificationLevel,
            imageResName = "prop_house_kottakuppam",
            featuresList = listOf("Water Facility", "Power Backup"),
            ownerName = "Test Owner"
        )
    }
}
