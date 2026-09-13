package com.example.domain.usecase

import com.example.data.model.Property
import com.example.data.repository.PropertyRepository

/**
 * UseCase to toggle property saved/favorite state in local and remote stores.
 */
class ToggleSavePropertyUseCase(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(property: Property) {
        repository.toggleSave(property.id, property.isSaved)
    }

    suspend operator fun invoke(propertyId: String, currentSaved: Boolean) {
        repository.toggleSave(propertyId, currentSaved)
    }
}
