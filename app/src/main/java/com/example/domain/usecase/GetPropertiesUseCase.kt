package com.example.domain.usecase

import com.example.data.model.Property
import com.example.data.repository.PropertyRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase to stream all properties or saved properties from repository.
 */
class GetPropertiesUseCase(
    private val repository: PropertyRepository
) {
    operator fun invoke(): Flow<List<Property>> = repository.allProperties

    fun getSaved(): Flow<List<Property>> = repository.savedProperties

    fun getByCategory(category: String): Flow<List<Property>> =
        repository.getPropertiesByCategory(category)

    fun getByLocation(location: String): Flow<List<Property>> =
        repository.getPropertiesByLocation(location)
}
