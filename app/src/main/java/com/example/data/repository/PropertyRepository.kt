package com.example.data.repository

import com.example.data.local.DefaultProperties
import com.example.data.local.PropertyDao
import com.example.data.local.PropertyEntity
import com.example.data.local.VisitEntity
import com.example.data.model.Property
import com.example.data.model.PropertyVisit
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PropertyRepository(
    private val dao: PropertyDao,
    private val firestoreService: FirestoreService = FirestoreService()
) {

    val allProperties: Flow<List<Property>> = dao.getAllProperties().map { entities ->
        entities.map { it.toDomain() }
    }

    val savedProperties: Flow<List<Property>> = dao.getSavedProperties().map { entities ->
        entities.map { it.toDomain() }
    }

    val allVisits: Flow<List<PropertyVisit>> = dao.getAllVisits().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun ensureInitialized() {
        val count = dao.getCount()
        if (count == 0) {
            dao.insertProperties(DefaultProperties.sampleList.map { PropertyEntity.fromDomain(it) })
            dao.insertVisit(
                VisitEntity(
                    id = "visit-init-1",
                    propertyId = "prop-1",
                    propertyTitle = "2BHK Independent House with Covered Parking",
                    location = "Kottakuppam",
                    buyerName = "Ragul",
                    date = "Tomorrow",
                    timeSlot = "10:00 AM",
                    status = "Confirmed"
                )
            )
        }
        // Seed default properties to Firestore if remote collection is empty
        firestoreService.seedDefaultPropertiesIfEmpty(DefaultProperties.sampleList)
    }

    suspend fun syncWithFirestore(remoteProperties: List<Property>) {
        if (remoteProperties.isNotEmpty()) {
            dao.insertProperties(remoteProperties.map { PropertyEntity.fromDomain(it) })
        }
    }

    suspend fun refreshFromFirestore(): Result<Int> = runCatching {
        val remoteProperties = firestoreService.fetchPropertiesOnce()
        if (remoteProperties.isNotEmpty()) {
            dao.insertProperties(remoteProperties.map { PropertyEntity.fromDomain(it) })
            remoteProperties.size
        } else {
            // Keep existing local seed/cached data intact
            dao.getCount()
        }
    }

    suspend fun toggleSave(propertyId: String, currentSaved: Boolean) {
        dao.updateSavedStatus(propertyId, !currentSaved)
    }

    suspend fun addProperty(property: Property) {
        dao.insertProperty(PropertyEntity.fromDomain(property))
        firestoreService.saveProperty(property)
    }

    suspend fun scheduleVisit(visit: PropertyVisit) {
        dao.insertVisit(VisitEntity.fromDomain(visit))
        firestoreService.saveVisit(visit)
    }

    suspend fun updateVisitStatus(visitId: String, status: String) {
        dao.updateVisitStatus(visitId, status)
    }

    suspend fun deleteVisit(visitId: String) {
        dao.deleteVisit(visitId)
    }

    fun getChatMessagesForProperty(propertyId: String): Flow<List<com.example.data.model.ChatMessage>> {
        return dao.getMessagesForProperty(propertyId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insertChatMessage(message: com.example.data.model.ChatMessage) {
        dao.insertMessage(com.example.data.local.ChatMessageEntity.fromDomain(message))
    }

    suspend fun reportProperty(propertyId: String, propertyTitle: String, reason: String, details: String) {
        val report = com.example.data.local.PropertyReportEntity(
            id = "rep-${System.currentTimeMillis()}",
            propertyId = propertyId,
            propertyTitle = propertyTitle,
            reason = reason,
            details = details
        )
        dao.insertReport(report)
    }

    suspend fun updatePropertyStatus(propertyId: String, status: String) {
        dao.updatePropertyStatus(propertyId, status)
    }

    suspend fun deleteProperty(propertyId: String) {
        dao.deleteProperty(propertyId)
        firestoreService.deleteProperty(propertyId)
    }

    suspend fun updateProperty(property: Property) {
        dao.updateProperty(PropertyEntity.fromDomain(property))
        firestoreService.saveProperty(property)
    }

    fun getPropertiesByCategory(category: String): Flow<List<Property>> =
        dao.getPropertiesByCategory(category).map { list -> list.map { it.toDomain() } }

    fun getPropertiesByListingType(listingType: String): Flow<List<Property>> =
        dao.getPropertiesByListingType(listingType).map { list -> list.map { it.toDomain() } }

    fun getPropertiesByLocation(location: String): Flow<List<Property>> =
        dao.getPropertiesByLocation(location).map { list -> list.map { it.toDomain() } }

    fun getPropertiesByPriceRange(minPrice: Long, maxPrice: Long): Flow<List<Property>> =
        dao.getPropertiesByPriceRange(minPrice, maxPrice).map { list -> list.map { it.toDomain() } }

    fun getPropertiesFiltered(
        location: String?,
        category: String?,
        minPrice: Long = 0L,
        maxPrice: Long = Long.MAX_VALUE
    ): Flow<List<Property>> =
        dao.getPropertiesFiltered(location, category, minPrice, maxPrice).map { list -> list.map { it.toDomain() } }

    fun searchProperties(query: String): Flow<List<Property>> =
        dao.searchProperties(query).map { list -> list.map { it.toDomain() } }

    suspend fun addSearchAlert(location: String, propertyType: String, maxPrice: Long) {
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
    suspend fun getSmartMatchSuggestions(
        preferences: com.example.data.model.UserPreferences
    ): List<com.example.data.model.SmartMatchResult> {
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
            val score = com.example.data.model.SmartMatchEngine.calculateScore(prop, preferences)
            com.example.data.model.SmartMatchResult(
                property = prop,
                matchScore = score,
                isFromFirestore = isDirectFromFirestore
            )
        }.sortedByDescending { it.matchScore.overallPercentage }
    }
}
