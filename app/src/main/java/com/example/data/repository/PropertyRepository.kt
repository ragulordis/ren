package com.example.data.repository

import com.example.data.local.DefaultProperties
import com.example.data.local.PropertyDao
import com.example.data.local.PropertyEntity
import com.example.data.local.VisitEntity
import com.example.data.model.ChatMessage
import com.example.data.model.Property
import com.example.data.model.PropertyVisit
import com.example.data.model.SmartMatchEngine
import com.example.data.model.SmartMatchResult
import com.example.data.model.UserPreferences
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Domain-facing repository interface defining contract for Property, Visit, and Chat operations.
 */
interface PropertyRepository {
    val allProperties: Flow<List<Property>>
    val savedProperties: Flow<List<Property>>
    val allVisits: Flow<List<PropertyVisit>>

    suspend fun ensureInitialized(seedLocalDevIfEmpty: Boolean = true)
    suspend fun syncWithFirestore(remoteProperties: List<Property>)
    suspend fun refreshFromFirestore(): Result<Int>
    suspend fun toggleSave(propertyId: String, currentSaved: Boolean)
    suspend fun addProperty(property: Property)
    suspend fun scheduleVisit(visit: PropertyVisit)
    suspend fun updateVisitStatus(visitId: String, status: String)
    suspend fun deleteVisit(visitId: String)
    fun getChatMessagesForProperty(propertyId: String): Flow<List<ChatMessage>>
    suspend fun insertChatMessage(message: ChatMessage)
    suspend fun reportProperty(propertyId: String, propertyTitle: String, reason: String, details: String)
    suspend fun updatePropertyStatus(propertyId: String, status: String)
    suspend fun deleteProperty(propertyId: String)
    suspend fun updateProperty(property: Property)
    fun getPropertiesByCategory(category: String): Flow<List<Property>>
    fun getPropertiesByListingType(listingType: String): Flow<List<Property>>
    fun getPropertiesByLocation(location: String): Flow<List<Property>>
    fun getPropertiesByPriceRange(minPrice: Long, maxPrice: Long): Flow<List<Property>>
    fun getPropertiesFiltered(
        location: String?,
        category: String?,
        minPrice: Long = 0L,
        maxPrice: Long = Long.MAX_VALUE
    ): Flow<List<Property>>
    fun searchProperties(query: String): Flow<List<Property>>
    suspend fun addSearchAlert(location: String, propertyType: String, maxPrice: Long)
    suspend fun getSmartMatchSuggestions(preferences: UserPreferences): List<SmartMatchResult>
}

/**
 * Default implementation of PropertyRepository coordinating Room local cache and Firestore remote store.
 */
class PropertyRepositoryImpl(
    private val dao: PropertyDao,
    private val firestoreService: FirestoreService = FirestoreService()
) : PropertyRepository {

    override val allProperties: Flow<List<Property>> = dao.getAllProperties().map { entities ->
        entities.map { it.toDomain() }
    }

    override val savedProperties: Flow<List<Property>> = dao.getSavedProperties().map { entities ->
        entities.map { it.toDomain() }
    }

    override val allVisits: Flow<List<PropertyVisit>> = dao.getAllVisits().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun ensureInitialized(seedLocalDevIfEmpty: Boolean) {
        val count = dao.getCount()
        if (count == 0 && seedLocalDevIfEmpty) {
            // Local offline preview cache only - NEVER pushes fake listings to production Firestore
            dao.insertProperties(DefaultProperties.sampleList.map { PropertyEntity.fromDomain(it) })
        }
    }

    override suspend fun syncWithFirestore(remoteProperties: List<Property>) {
        if (remoteProperties.isNotEmpty()) {
            dao.insertProperties(remoteProperties.map { PropertyEntity.fromDomain(it) })
        }
    }

    override suspend fun refreshFromFirestore(): Result<Int> = runCatching {
        val remoteProperties = firestoreService.fetchPropertiesOnce()
        if (remoteProperties.isNotEmpty()) {
            dao.insertProperties(remoteProperties.map { PropertyEntity.fromDomain(it) })
            remoteProperties.size
        } else {
            // Keep existing local seed/cached data intact
            dao.getCount()
        }
    }

    override suspend fun toggleSave(propertyId: String, currentSaved: Boolean) {
        dao.updateSavedStatus(propertyId, !currentSaved)
    }

    override suspend fun addProperty(property: Property) {
        dao.insertProperty(PropertyEntity.fromDomain(property))
        firestoreService.saveProperty(property)
    }

    override suspend fun scheduleVisit(visit: PropertyVisit) {
        dao.insertVisit(VisitEntity.fromDomain(visit))
        firestoreService.saveVisit(visit)
    }

    override suspend fun updateVisitStatus(visitId: String, status: String) {
        dao.updateVisitStatus(visitId, status)
    }

    override suspend fun deleteVisit(visitId: String) {
        dao.deleteVisit(visitId)
    }

    override fun getChatMessagesForProperty(propertyId: String): Flow<List<ChatMessage>> {
        return dao.getMessagesForProperty(propertyId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertChatMessage(message: ChatMessage) {
        dao.insertMessage(com.example.data.local.ChatMessageEntity.fromDomain(message))
    }

    override suspend fun reportProperty(propertyId: String, propertyTitle: String, reason: String, details: String) {
        val report = com.example.data.local.PropertyReportEntity(
            id = "rep-${System.currentTimeMillis()}",
            propertyId = propertyId,
            propertyTitle = propertyTitle,
            reason = reason,
            details = details
        )
        dao.insertReport(report)
    }

    override suspend fun updatePropertyStatus(propertyId: String, status: String) {
        dao.updatePropertyStatus(propertyId, status)
    }

    override suspend fun deleteProperty(propertyId: String) {
        dao.deleteProperty(propertyId)
        firestoreService.deleteProperty(propertyId)
    }

    override suspend fun updateProperty(property: Property) {
        dao.updateProperty(PropertyEntity.fromDomain(property))
        firestoreService.saveProperty(property)
    }

    override fun getPropertiesByCategory(category: String): Flow<List<Property>> =
        dao.getPropertiesByCategory(category).map { list -> list.map { it.toDomain() } }

    override fun getPropertiesByListingType(listingType: String): Flow<List<Property>> =
        dao.getPropertiesByListingType(listingType).map { list -> list.map { it.toDomain() } }

    override fun getPropertiesByLocation(location: String): Flow<List<Property>> =
        dao.getPropertiesByLocation(location).map { list -> list.map { it.toDomain() } }

    override fun getPropertiesByPriceRange(minPrice: Long, maxPrice: Long): Flow<List<Property>> =
        dao.getPropertiesByPriceRange(minPrice, maxPrice).map { list -> list.map { it.toDomain() } }

    override fun getPropertiesFiltered(
        location: String?,
        category: String?,
        minPrice: Long,
        maxPrice: Long
    ): Flow<List<Property>> =
        dao.getPropertiesFiltered(location, category, minPrice, maxPrice).map { list -> list.map { it.toDomain() } }

    override fun searchProperties(query: String): Flow<List<Property>> =
        dao.searchProperties(query).map { list -> list.map { it.toDomain() } }

    override suspend fun addSearchAlert(location: String, propertyType: String, maxPrice: Long) {
        dao.insertSearchAlert(
            com.example.data.local.SearchAlertEntity(
                id = "alert-${System.currentTimeMillis()}",
                location = location,
                propertyType = propertyType,
                maxPrice = maxPrice
            )
        )
    }

    /**
     * Executes Smart Match pipeline:
     * 1. Query Firestore properties matching user preferences (budget, location, property type)
     * 2. If Firestore query returns candidates, caches them in Room
     * 3. Fallbacks to local Room properties if Firestore is offline
     * 4. Evaluates Smart Match scoring algorithm on candidate properties
     * 5. Returns ranked list of SmartMatchResults
     */
    override suspend fun getSmartMatchSuggestions(
        preferences: UserPreferences
    ): List<SmartMatchResult> {
        val firestoreCandidates = firestoreService.querySmartMatchProperties(preferences)

        val isDirectFromFirestore = firestoreCandidates.isNotEmpty()

        val candidates = if (isDirectFromFirestore) {
            runCatching {
                dao.insertProperties(firestoreCandidates.map { PropertyEntity.fromDomain(it) })
            }
            firestoreCandidates
        } else {
            // Local fallback when network / Firestore is offline or empty
            dao.getAllPropertiesSync().map { it.toDomain() }
        }

        return candidates.map { prop ->
            val score = SmartMatchEngine.calculateScore(prop, preferences)
            SmartMatchResult(
                property = prop,
                matchScore = score,
                isFromFirestore = isDirectFromFirestore
            )
        }.sortedByDescending { it.matchScore.overallPercentage }
    }
}
