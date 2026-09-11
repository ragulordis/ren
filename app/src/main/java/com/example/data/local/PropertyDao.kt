package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY urgencyScore DESC, price ASC")
    fun getAllProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties")
    suspend fun getAllPropertiesSync(): List<PropertyEntity>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    fun getPropertyById(id: String): Flow<PropertyEntity?>

    @Query("SELECT * FROM properties WHERE isSaved = 1")
    fun getSavedProperties(): Flow<List<PropertyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<PropertyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity)

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    @Query("UPDATE properties SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedStatus(id: String, isSaved: Boolean)

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun getCount(): Int

    /**
     * Filter properties by Category (e.g. "BUY", "RENT", "COMMERCIAL", "LAND")
     */
    @Query("SELECT * FROM properties WHERE category = :category OR listingType = :category ORDER BY price ASC")
    fun getPropertiesByCategory(category: String): Flow<List<PropertyEntity>>

    /**
     * Filter properties by Listing Type ("BUY" for sale or "RENT" for rental)
     */
    @Query("SELECT * FROM properties WHERE listingType = :listingType ORDER BY price ASC")
    fun getPropertiesByListingType(listingType: String): Flow<List<PropertyEntity>>

    /**
     * Filter properties by location (e.g. "Kottakuppam", "Pondicherry", "Auroville")
     */
    @Query("SELECT * FROM properties WHERE location LIKE '%' || :location || '%' ORDER BY price ASC")
    fun getPropertiesByLocation(location: String): Flow<List<PropertyEntity>>

    /**
     * Filter properties by min and max price range
     */
    @Query("SELECT * FROM properties WHERE price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC")
    fun getPropertiesByPriceRange(minPrice: Long, maxPrice: Long): Flow<List<PropertyEntity>>

    /**
     * Multi-criteria filtering by optional location, category (rent/sale), and price bounds
     */
    @Query("""
        SELECT * FROM properties 
        WHERE (:location IS NULL OR location LIKE '%' || :location || '%')
          AND (:category IS NULL OR category = :category OR listingType = :category)
          AND price BETWEEN :minPrice AND :maxPrice
        ORDER BY price ASC
    """)
    fun getPropertiesFiltered(
        location: String?,
        category: String?,
        minPrice: Long = 0L,
        maxPrice: Long = 9223372036854775807L
    ): Flow<List<PropertyEntity>>

    /**
     * Keyword search querying title, location, or property type
     */
    @Query("""
        SELECT * FROM properties 
        WHERE title LIKE '%' || :query || '%' 
           OR location LIKE '%' || :query || '%'
           OR propertyType LIKE '%' || :query || '%'
        ORDER BY price ASC
    """)
    fun searchProperties(query: String): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM visits ORDER BY id DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity)

    @Query("UPDATE visits SET status = :status WHERE id = :visitId")
    suspend fun updateVisitStatus(visitId: String, status: String)

    @Query("DELETE FROM visits WHERE id = :visitId")
    suspend fun deleteVisit(visitId: String)

    @Query("SELECT * FROM chat_messages WHERE propertyId = :propertyId ORDER BY timestamp ASC")
    fun getMessagesForProperty(propertyId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: PropertyReportEntity)

    @Query("UPDATE properties SET status = :status WHERE id = :propertyId")
    suspend fun updatePropertyStatus(propertyId: String, status: String)

    @Query("DELETE FROM properties WHERE id = :propertyId")
    suspend fun deleteProperty(propertyId: String)

    @Query("SELECT * FROM search_alerts ORDER BY createdAt DESC")
    fun getAllSearchAlerts(): Flow<List<SearchAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchAlert(alert: SearchAlertEntity)
}
