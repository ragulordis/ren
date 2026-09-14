package com.example.domain.usecase

import com.example.data.model.Property
import com.example.data.model.PropertyVisit
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository

/**
 * UseCase to schedule an on-site property inspection with authenticated buyer identity.
 * Populates buyerId (auth UID) and sellerId (property ownerId) so that Firestore
 * security rules can validate the write: request.auth.uid == buyerId.
 */
class ScheduleVisitUseCase(
    private val repository: PropertyRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        property: Property,
        date: String,
        timeSlot: String
    ): Result<PropertyVisit> = runCatching {
        val user = authRepository.currentUser()
        val visit = PropertyVisit(
            id = "visit-${System.currentTimeMillis()}",
            propertyId = property.id,
            propertyTitle = property.title,
            location = property.location,
            buyerId = user?.uid ?: "",          // Firebase Auth UID — validated by Firestore rules
            sellerId = property.ownerId,         // Property owner — validated by Firestore rules
            buyerName = user?.displayName?.ifBlank { "Buyer" } ?: "Buyer",
            date = date,
            timeSlot = timeSlot,
            status = "Requested"
        )
        repository.scheduleVisit(visit)
        visit
    }
}
