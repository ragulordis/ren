package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuickNestDatabase
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepositoryImpl
import com.example.domain.usecase.PostListingParams
import com.example.domain.usecase.PostListingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostPropertyViewModel(
    application: Application,
    private val postListingUseCase: PostListingUseCase = PostListingUseCase(
        PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao()),
        com.example.data.repository.AuthRepositoryImpl()
    )
) : AndroidViewModel(application) {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _postResult = MutableStateFlow<Result<Property>?>(null)
    val postResult: StateFlow<Result<Property>?> = _postResult.asStateFlow()

    fun submitListing(
        title: String,
        description: String,
        price: Long,
        location: String,
        category: PropertyCategory,
        propertyType: String,
        speed: SellingSpeed,
        bedrooms: Int,
        bathrooms: Int,
        areaSqFt: Int,
        features: List<String>,
        isPrivate: Boolean = false,
        customImageResName: String? = null,
        onSuccess: (Property) -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val params = PostListingParams(
                title = title,
                description = description,
                price = price,
                location = location,
                category = category,
                propertyType = propertyType,
                speed = speed,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                areaSqFt = areaSqFt,
                features = features,
                isPrivate = isPrivate,
                customImageResName = customImageResName
            )
            val result = postListingUseCase(params)
            _postResult.value = result
            _isSubmitting.value = false

            result.onSuccess(onSuccess).onFailure(onError)
        }
    }

    fun clearResult() {
        _postResult.value = null
    }
}
