package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BudgetFilter
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.data.model.SmartMatchResult
import com.example.data.model.SortOption
import com.example.data.model.UserPreferences
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository
import com.example.domain.usecase.FilterCriteria
import com.example.domain.usecase.FilterPropertiesUseCase
import com.example.domain.usecase.GetPropertiesUseCase
import com.example.domain.usecase.SmartMatchUseCase
import com.example.domain.usecase.ToggleSavePropertyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val propertyRepository: PropertyRepository,
    private val getPropertiesUseCase: GetPropertiesUseCase,
    private val filterPropertiesUseCase: FilterPropertiesUseCase = FilterPropertiesUseCase(),
    private val toggleSavePropertyUseCase: ToggleSavePropertyUseCase = ToggleSavePropertyUseCase(propertyRepository),
    private val smartMatchUseCase: SmartMatchUseCase = SmartMatchUseCase(propertyRepository),
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PropertyCategory.ALL)
    val selectedCategory: StateFlow<PropertyCategory> = _selectedCategory.asStateFlow()

    private val _selectedPropertyType = MutableStateFlow<String?>(null)
    val selectedPropertyType: StateFlow<String?> = _selectedPropertyType.asStateFlow()

    private val _selectedLocation = MutableStateFlow("All")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    private val _selectedBudget = MutableStateFlow(BudgetFilter.ALL)
    val selectedBudget: StateFlow<BudgetFilter> = _selectedBudget.asStateFlow()

    private val _selectedBedrooms = MutableStateFlow(0)
    val selectedBedrooms: StateFlow<Int> = _selectedBedrooms.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(SortOption.URGENCY)
    val selectedSortOption: StateFlow<SortOption> = _selectedSortOption.asStateFlow()

    private val _verifiedOnly = MutableStateFlow(false)
    val verifiedOnly: StateFlow<Boolean> = _verifiedOnly.asStateFlow()

    private val _urgentOnly = MutableStateFlow(false)
    val urgentOnly: StateFlow<Boolean> = _urgentOnly.asStateFlow()

    private val _minPrice = MutableStateFlow<Long?>(null)
    val minPrice: StateFlow<Long?> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Long?>(null)
    val maxPrice: StateFlow<Long?> = _maxPrice.asStateFlow()

    private val _recentSearches = MutableStateFlow(
        listOf("Pondicherry", "Kottakuppam", "East Coast Villa", "Under 50 Lakhs", "Urgent Plot")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // Smart Match State
    private val _smartMatchPreferences = MutableStateFlow(
        UserPreferences(
            preferredListingType = ListingType.BUY,
            maxBudget = 5000000L,
            minBedrooms = 2,
            location = "All",
            propertyType = "All"
        )
    )
    val smartMatchPreferences: StateFlow<UserPreferences> = _smartMatchPreferences.asStateFlow()

    private val _smartMatchResults = MutableStateFlow<List<SmartMatchResult>>(emptyList())
    val smartMatchResults: StateFlow<List<SmartMatchResult>> = _smartMatchResults.asStateFlow()

    private val _isSmartMatchLoading = MutableStateFlow(false)
    val isSmartMatchLoading: StateFlow<Boolean> = _isSmartMatchLoading.asStateFlow()

    val currentUserProfile: StateFlow<UserProfile?> = MutableStateFlow(authRepository?.currentUser())

    // Properties Data
    val allProperties: StateFlow<List<Property>> = getPropertiesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val urgentProperties: StateFlow<List<Property>> = allProperties.map { list ->
        list.filter { it.isUrgent || it.sellingSpeed == SellingSpeed.URGENT || it.urgencyScore >= 4 }
            .sortedByDescending { it.urgencyScore }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProperties: StateFlow<List<Property>> = combine(
        allProperties,
        _searchQuery,
        _selectedCategory,
        _selectedPropertyType,
        _selectedLocation,
        _selectedBudget,
        _selectedBedrooms,
        _selectedSortOption,
        _verifiedOnly,
        _urgentOnly,
        _minPrice,
        _maxPrice
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val properties = args[0] as List<Property>
        val criteria = FilterCriteria(
            query = args[1] as String,
            category = args[2] as PropertyCategory,
            propertyType = args[3] as? String,
            location = args[4] as String,
            budget = args[5] as BudgetFilter,
            bedrooms = args[6] as Int,
            sortOption = args[7] as SortOption,
            verifiedOnly = args[8] as Boolean,
            urgentOnly = args[9] as Boolean,
            minPrice = args[10] as? Long,
            maxPrice = args[11] as? Long
        )
        filterPropertiesUseCase(properties, criteria)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFiltersCount: StateFlow<Int> = combine(
        _selectedCategory,
        _selectedPropertyType,
        _selectedLocation,
        _selectedBudget,
        _selectedBedrooms,
        _verifiedOnly,
        _urgentOnly
    ) { args: Array<Any?> ->
        var count = 0
        if ((args[0] as PropertyCategory) != PropertyCategory.ALL) count++
        if (args[1] != null) count++
        if ((args[2] as String) != "All") count++
        if ((args[3] as BudgetFilter) != BudgetFilter.ALL) count++
        if ((args[4] as Int) > 0) count++
        if (args[5] as Boolean) count++
        if (args[6] as Boolean) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            propertyRepository.ensureInitialized(seedLocalDevIfEmpty = true)
            runSmartMatchQuery()
        }
    }

    fun runSmartMatchQuery() {
        viewModelScope.launch {
            _isSmartMatchLoading.value = true
            val results = smartMatchUseCase(_smartMatchPreferences.value)
            _smartMatchResults.value = results
            _isSmartMatchLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveRecentSearch(term: String) {
        if (term.isBlank()) return
        val current = _recentSearches.value.toMutableList()
        current.remove(term)
        current.add(0, term)
        _recentSearches.value = current.take(10)
    }

    fun removeRecentSearch(term: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(term)
        _recentSearches.value = current
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun selectCategory(category: PropertyCategory) {
        _selectedCategory.value = category
    }

    fun selectPropertyType(type: String?) {
        _selectedPropertyType.value = type
    }

    fun selectLocation(location: String) {
        _selectedLocation.value = location
    }

    fun setBudgetFilter(budget: BudgetFilter) {
        _selectedBudget.value = budget
    }

    fun setBedroomsFilter(bedrooms: Int) {
        _selectedBedrooms.value = bedrooms
    }

    fun setSortOption(sort: SortOption) {
        _selectedSortOption.value = sort
    }

    fun setVerifiedOnly(value: Boolean) {
        _verifiedOnly.value = value
    }

    fun toggleUrgentOnly() {
        _urgentOnly.value = !_urgentOnly.value
    }

    fun setUrgentOnly(value: Boolean) {
        _urgentOnly.value = value
    }

    fun setPriceRange(min: Long?, max: Long?) {
        _minPrice.value = min
        _maxPrice.value = max
    }

    fun clearPriceRange() {
        _minPrice.value = null
        _maxPrice.value = null
    }

    fun resetFilters() {
        _selectedCategory.value = PropertyCategory.ALL
        _selectedPropertyType.value = null
        _selectedLocation.value = "All"
        _selectedBudget.value = BudgetFilter.ALL
        _selectedBedrooms.value = 0
        _selectedSortOption.value = SortOption.URGENCY
        _verifiedOnly.value = false
        _urgentOnly.value = false
        _minPrice.value = null
        _maxPrice.value = null
        _searchQuery.value = ""
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            propertyRepository.refreshFromFirestore()
            _isRefreshing.value = false
        }
    }

    fun refreshProperties() {
        refreshData()
    }

    fun toggleSave(property: Property) {
        viewModelScope.launch {
            toggleSavePropertyUseCase(property)
        }
    }
}
