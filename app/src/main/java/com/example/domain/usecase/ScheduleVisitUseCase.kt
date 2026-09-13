package com.example.domain.usecase

import com.example.data.model.Property
import com.example.data.model.PropertyVisit
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository

/**
 * UseCase to schedule an on-site property inspection with authenticated buyer identity.
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
        val user = authRepository.getCurrentUser()
        val visit = PropertyVisit(
            id = "visit-${System.currentTimeMillis()}",
            propertyId = property.id,
            propertyTitle = property.title,
            location = property.location,
            buyerName = user.displayName.ifBlank { "Authenticated Buyer" },
            date = date,
            timeSlot = timeSlot,
            status = "Requested"
        )
        repository.scheduleVisit(visit)
        visit
    }
}
