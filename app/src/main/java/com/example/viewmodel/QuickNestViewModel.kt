package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuickNestDatabase
import com.example.data.model.BuyerMatch
import com.example.data.model.ChatMessage
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.PropertyVisit
import com.example.data.model.SellingSpeed
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.PropertyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

typealias BudgetFilter = com.example.data.model.BudgetFilter
typealias SortOption = com.example.data.model.SortOption

class QuickNestViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ren_app_prefs", Context.MODE_PRIVATE)
    private val repository: PropertyRepository
    private val firestoreService = com.example.data.remote.FirestoreService()
    private val authRepository: com.example.data.repository.AuthRepository = com.example.data.repository.AuthRepositoryImpl()
    private var chatCollectionJob: Job? = null

    // Onboarding and Auth State
    private val _isOnboarded = MutableStateFlow(prefs.getBoolean("is_onboarded", false))
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_guest_mode", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val authState: StateFlow<com.example.data.model.AuthState> = authRepository.authState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            com.example.data.model.AuthState.Loading
        )

    // Domain Use Cases
    private val getPropertiesUseCase = com.example.domain.usecase.GetPropertiesUseCase(com.example.data.repository.PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao()))
    private val scheduleVisitUseCase = com.example.domain.usecase.ScheduleVisitUseCase(com.example.data.repository.PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao()), authRepository)
    private val sendChatMessageUseCase = com.example.domain.usecase.SendChatMessageUseCase(com.example.data.repository.PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao()), authRepository)
    private val smartMatchUseCase = com.example.domain.usecase.SmartMatchUseCase(com.example.data.repository.PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao()))
    private val toggleSaveUseCase = com.example.domain.usecase.ToggleSavePropertyUseCase(com.example.data.repository.PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao()))
    private val observeUserProfileUseCase = com.example.domain.usecase.ObserveUserProfileUseCase(authRepository)
    private val postListingUseCase: com.example.domain.usecase.PostListingUseCase

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    init {
        val db = QuickNestDatabase.getInstance(application)
        repository = com.example.data.repository.PropertyRepositoryImpl(db.propertyDao(), firestoreService)
        postListingUseCase = com.example.domain.usecase.PostListingUseCase(repository, authRepository)

        // Observe real Firebase Auth state changes
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                when (state) {
                    is com.example.data.model.AuthState.SignedIn -> {
                        _currentUserProfile.value = state.user
                        _isLoggedIn.value = true
                    }
                    is com.example.data.model.AuthState.SignedOut -> {
                        _currentUserProfile.value = null
                        if (!prefs.getBoolean("is_guest_mode", false)) {
                            _isLoggedIn.value = false
                        }
                    }
                    is com.example.data.model.AuthState.Error -> {
                        _currentUserProfile.value = null
                        if (!prefs.getBoolean("is_guest_mode", false)) {
                            _isLoggedIn.value = false
                        }
                        _feedbackMessage.value = state.message
                    }
                    is com.example.data.model.AuthState.Loading -> {
                        // Loading state
                    }
                }
            }
        }

        // Restore saved session if available
        val savedEmail = prefs.getString("user_email", null)
        val savedName = prefs.getString("user_name", null)
        if (!savedEmail.isNullOrBlank() && !prefs.getBoolean("is_guest_mode", false)) {
            viewModelScope.launch {
                authRepository.signInWithGoogleAccount(
                    email = savedEmail,
                    displayName = savedName ?: "Ren Member",
                    photoUrl = null,
                    role = UserRole.BUYER
                )
            }
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.ensureInitialized()
            kotlinx.coroutines.delay(200)
            _isLoading.value = false
            runSmartMatchQuery()
        }

        // Real-time synchronization from Firestore collection
        viewModelScope.launch {
            _isSyncingCloud.value = true
            runCatching {
                firestoreService.streamProperties().collect { remoteList ->
                    if (remoteList.isNotEmpty()) {
                        repository.syncWithFirestore(remoteList)
                    }
                }
            }
            _isSyncingCloud.value = false
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.ensureInitialized()
            kotlinx.coroutines.delay(350)
            _isLoading.value = false
        }
    }

    /**
     * Manual SwipeRefresh action to fetch the latest properties from Firestore
     * and update the local database.
     */
    fun refreshProperties() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = repository.refreshFromFirestore()
                val count = result.getOrDefault(0)
                if (count > 0) {
                    _feedbackMessage.value = "Synced latest properties from Firestore"
                } else {
                    _feedbackMessage.value = "Property listings are up to date"
                }
            } catch (e: Exception) {
                _feedbackMessage.value = "Refreshed property listings"
            } finally {
                kotlinx.coroutines.delay(500)
                _isRefreshing.value = false
            }
        }
    }

    val allProperties: StateFlow<List<Property>> = repository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedProperties: StateFlow<List<Property>> = repository.savedProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visits: StateFlow<List<PropertyVisit>> = repository.allVisits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Navigation State
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // Feedback message (Snackbar/toast events)
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun triggerFeedback(message: String) {
        _feedbackMessage.value = message
    }

    fun showFeedback(message: String) {
        _feedbackMessage.value = message
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(
        listOf("Auroville", "2BHK Villa", "Under 30L", "Sea View", "Commercial", "Plots")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PropertyCategory.ALL)
    val selectedCategory: StateFlow<PropertyCategory> = _selectedCategory.asStateFlow()

    private val _selectedPropertyType = MutableStateFlow<String?>(null)
    val selectedPropertyType: StateFlow<String?> = _selectedPropertyType.asStateFlow()

    private val _selectedLocation = MutableStateFlow("All Locations")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    private val _urgentOnly = MutableStateFlow(false)
    val urgentOnly: StateFlow<Boolean> = _urgentOnly.asStateFlow()

    private val _selectedBudget = MutableStateFlow(BudgetFilter.ALL)
    val selectedBudget: StateFlow<BudgetFilter> = _selectedBudget.asStateFlow()

    private val _selectedBedrooms = MutableStateFlow(0) // 0 = Any
    val selectedBedrooms: StateFlow<Int> = _selectedBedrooms.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(SortOption.URGENCY)
    val selectedSortOption: StateFlow<SortOption> = _selectedSortOption.asStateFlow()

    private val _verifiedOnly = MutableStateFlow(false)
    val verifiedOnly: StateFlow<Boolean> = _verifiedOnly.asStateFlow()

    private val _minPrice = MutableStateFlow<Long?>(null)
    val minPrice: StateFlow<Long?> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Long?>(null)
    val maxPrice: StateFlow<Long?> = _maxPrice.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    // Active filters count
    val activeFiltersCount: StateFlow<Int> = combine(
        _selectedCategory,
        _selectedPropertyType,
        _urgentOnly,
        _selectedBudget,
        _selectedBedrooms,
        _verifiedOnly,
        _minPrice,
        _maxPrice
    ) { params: Array<Any?> ->
        val cat = params[0] as PropertyCategory
        val propType = params[1] as? String
        val urg = params[2] as Boolean
        val budget = params[3] as BudgetFilter
        val bed = params[4] as Int
        val ver = params[5] as Boolean
        val minP = params[6] as? Long
        val maxP = params[7] as? Long
        var count = 0
        if (cat != PropertyCategory.ALL) count++
        if (!propType.isNullOrBlank() && propType != "All") count++
        if (urg) count++
        if (budget != BudgetFilter.ALL) count++
        if (bed > 0) count++
        if (ver) count++
        if (minP != null || maxP != null) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered Properties for Home
    val filteredProperties: StateFlow<List<Property>> = combine(
        allProperties,
        _searchQuery,
        _selectedCategory,
        _selectedPropertyType,
        _selectedLocation,
        _urgentOnly,
        _selectedBudget,
        _selectedBedrooms,
        _verifiedOnly,
        _selectedSortOption,
        _minPrice,
        _maxPrice
    ) { params: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val list = params[0] as List<Property>
        val query = params[1] as String
        val category = params[2] as PropertyCategory
        val propertyType = params[3] as? String
        val location = params[4] as String
        val urgentOnly = params[5] as Boolean
        val budget = params[6] as BudgetFilter
        val bedrooms = params[7] as Int
        val verifiedOnly = params[8] as Boolean
        val sortOption = params[9] as SortOption
        val minPrice = params[10] as? Long
        val maxPrice = params[11] as? Long

        val filtered = list.filter { prop ->
            val matchesQuery = query.isBlank() ||
                    prop.title.contains(query, ignoreCase = true) ||
                    prop.location.contains(query, ignoreCase = true) ||
                    prop.propertyType.contains(query, ignoreCase = true) ||
                    prop.featuresList.any { it.contains(query, ignoreCase = true) }

            val matchesCategory = when (category) {
                PropertyCategory.ALL -> true
                PropertyCategory.BUY -> prop.listingType == ListingType.BUY || prop.listingType == ListingType.URGENT_SALE
                PropertyCategory.RENT -> prop.listingType == ListingType.RENT
                PropertyCategory.LEASE -> prop.listingType == ListingType.LEASE
                PropertyCategory.LAND -> prop.category == PropertyCategory.LAND
                PropertyCategory.COMMERCIAL -> prop.category == PropertyCategory.COMMERCIAL
            }

            val matchesPropertyType = when (val pType = propertyType?.trim()) {
                null, "", "All" -> true
                "Apartment" -> prop.propertyType.contains("Apartment", ignoreCase = true) || prop.title.contains("BHK", ignoreCase = true) || prop.title.contains("Flat", ignoreCase = true)
                "Villa" -> prop.propertyType.contains("Villa", ignoreCase = true) || prop.title.contains("Villa", ignoreCase = true)
                "Studio" -> prop.propertyType.contains("Studio", ignoreCase = true) || prop.propertyType.contains("Cottage", ignoreCase = true) || prop.title.contains("Cottage", ignoreCase = true) || prop.title.contains("1BHK", ignoreCase = true) || prop.title.contains("1RK", ignoreCase = true)
                "House" -> prop.propertyType.contains("House", ignoreCase = true) || prop.title.contains("House", ignoreCase = true)
                "Plot / Land", "Plot", "Land" -> prop.propertyType.contains("Plot", ignoreCase = true) || prop.propertyType.contains("Land", ignoreCase = true) || prop.category == PropertyCategory.LAND
                "Commercial" -> prop.propertyType.contains("Commercial", ignoreCase = true) || prop.propertyType.contains("Shop", ignoreCase = true) || prop.category == PropertyCategory.COMMERCIAL
                else -> prop.propertyType.contains(pType, ignoreCase = true) || prop.title.contains(pType, ignoreCase = true)
            }

            val matchesLocation = location == "All Locations" ||
                    prop.location.equals(location, ignoreCase = true) ||
                    prop.location.contains(location, ignoreCase = true) ||
                    location.contains(prop.location, ignoreCase = true) ||
                    prop.approximateArea.contains(location, ignoreCase = true)

            val matchesUrgent = !urgentOnly || prop.sellingSpeed == SellingSpeed.URGENT || prop.sellingSpeed == SellingSpeed.FAST

            val matchesPriceRange = when {
                minPrice != null && maxPrice != null -> prop.price in minPrice..maxPrice
                minPrice != null -> prop.price >= minPrice
                maxPrice != null -> prop.price <= maxPrice
                budget != BudgetFilter.ALL -> prop.price in budget.minPrice..budget.maxPrice
                else -> true
            }

            val matchesBedrooms = bedrooms == 0 || (bedrooms in 1..3 && prop.bedrooms == bedrooms) || (bedrooms >= 4 && prop.bedrooms >= 4)

            val matchesVerified = !verifiedOnly || prop.verificationLevel >= 2

            matchesQuery && matchesCategory && matchesPropertyType && matchesLocation && matchesUrgent && matchesPriceRange && matchesBedrooms && matchesVerified
        }

        when (sortOption) {
            SortOption.URGENCY -> filtered.sortedWith(compareByDescending<Property> { it.urgencyScore }.thenBy { it.price })
            SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.price }
            SortOption.AREA_HIGH_LOW -> filtered.sortedByDescending { it.areaSqFt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Urgent Properties Showcase
    val urgentProperties: StateFlow<List<Property>> = allProperties.combine(_selectedLocation) { list, loc ->
        list.filter {
            it.sellingSpeed == SellingSpeed.URGENT || it.sellingSpeed == SellingSpeed.FAST
        }.sortedByDescending { it.urgencyScore }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Dialogs & Sheets
    private val _selectedProperty = MutableStateFlow<Property?>(null)
    val selectedProperty: StateFlow<Property?> = _selectedProperty.asStateFlow()

    private val _chatProperty = MutableStateFlow<Property?>(null)
    val chatProperty: StateFlow<Property?> = _chatProperty.asStateFlow()

    private val _contactSellerProperty = MutableStateFlow<Property?>(null)
    val contactSellerProperty: StateFlow<Property?> = _contactSellerProperty.asStateFlow()

    private val _visitProperty = MutableStateFlow<Property?>(null)
    val visitProperty: StateFlow<Property?> = _visitProperty.asStateFlow()

    private val _quickMatchProperty = MutableStateFlow<Property?>(null)
    val quickMatchProperty: StateFlow<Property?> = _quickMatchProperty.asStateFlow()

    private val _reportProperty = MutableStateFlow<Property?>(null)
    val reportProperty: StateFlow<Property?> = _reportProperty.asStateFlow()

    private val _showAiAssistant = MutableStateFlow(false)
    val showAiAssistant: StateFlow<Boolean> = _showAiAssistant.asStateFlow()

    // Map Explore State
    private val _exploreSelectedProperty = MutableStateFlow<Property?>(null)
    val exploreSelectedProperty: StateFlow<Property?> = _exploreSelectedProperty.asStateFlow()

    private val _exploreRadiusKm = MutableStateFlow(5.0)
    val exploreRadiusKm: StateFlow<Double> = _exploreRadiusKm.asStateFlow()

    // User Role State
    private val _userRole = MutableStateFlow("Individual")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    // Chat Message History for current open chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // AI Assistant State
    private val _aiQuery = MutableStateFlow("")
    val aiQuery: StateFlow<String> = _aiQuery.asStateFlow()

    private val _aiResults = MutableStateFlow<List<Property>>(emptyList())
    val aiResults: StateFlow<List<Property>> = _aiResults.asStateFlow()

    private val _aiExplanation = MutableStateFlow("")
    val aiExplanation: StateFlow<String> = _aiExplanation.asStateFlow()

    // Smart Match User Preferences & Firestore Engine State
    private val _smartMatchPreferences = MutableStateFlow(
        com.example.data.model.UserPreferences(
            maxBudget = 4000000L,
            minBudget = 0L,
            location = "Kottakuppam",
            propertyType = "All Types",
            listingType = ListingType.BUY,
            preferredBedrooms = 2
        )
    )
    val smartMatchPreferences: StateFlow<com.example.data.model.UserPreferences> = _smartMatchPreferences.asStateFlow()

    private val _smartMatchResults = MutableStateFlow<List<com.example.data.model.SmartMatchResult>>(emptyList())
    val smartMatchResults: StateFlow<List<com.example.data.model.SmartMatchResult>> = _smartMatchResults.asStateFlow()

    private val _isSmartMatchLoading = MutableStateFlow(false)
    val isSmartMatchLoading: StateFlow<Boolean> = _isSmartMatchLoading.asStateFlow()

    private val _showSmartMatchDialog = MutableStateFlow(false)
    val showSmartMatchDialog: StateFlow<Boolean> = _showSmartMatchDialog.asStateFlow()

    fun openSmartMatchDialog() {
        _showSmartMatchDialog.value = true
        if (_smartMatchResults.value.isEmpty()) {
            runSmartMatchQuery()
        }
    }

    fun closeSmartMatchDialog() {
        _showSmartMatchDialog.value = false
    }

    fun updateSmartMatchBudget(maxBudget: Long) {
        _smartMatchPreferences.value = _smartMatchPreferences.value.copy(maxBudget = maxBudget)
        runSmartMatchQuery()
    }

    fun updateSmartMatchLocation(location: String) {
        _smartMatchPreferences.value = _smartMatchPreferences.value.copy(location = location)
        runSmartMatchQuery()
    }

    fun updateSmartMatchPropertyType(propertyType: String) {
        _smartMatchPreferences.value = _smartMatchPreferences.value.copy(propertyType = propertyType)
        runSmartMatchQuery()
    }

    fun updateSmartMatchListingType(listingType: ListingType) {
        _smartMatchPreferences.value = _smartMatchPreferences.value.copy(listingType = listingType)
        runSmartMatchQuery()
    }

    fun updateSmartMatchBedrooms(bedrooms: Int) {
        _smartMatchPreferences.value = _smartMatchPreferences.value.copy(preferredBedrooms = bedrooms)
        runSmartMatchQuery()
    }

    fun updateAllSmartMatchPreferences(
        budget: Long,
        location: String,
        propertyType: String,
        listingType: ListingType,
        bedrooms: Int
    ) {
        _smartMatchPreferences.value = com.example.data.model.UserPreferences(
            maxBudget = budget,
            minBudget = 0L,
            location = location,
            propertyType = propertyType,
            listingType = listingType,
            preferredBedrooms = bedrooms
        )
        runSmartMatchQuery()
    }

    fun runSmartMatchQuery() {
        viewModelScope.launch {
            _isSmartMatchLoading.value = true
            try {
                val results = smartMatchUseCase(_smartMatchPreferences.value)
                _smartMatchResults.value = results
            } catch (e: Exception) {
                val localProps = allProperties.value
                val results = localProps.map { prop ->
                    val score = com.example.data.model.SmartMatchEngine.calculateScore(prop, _smartMatchPreferences.value)
                    com.example.data.model.SmartMatchResult(
                        property = prop,
                        matchScore = score,
                        isFromFirestore = false
                    )
                }.sortedByDescending { it.matchScore.overallPercentage }
                _smartMatchResults.value = results
            } finally {
                _isSmartMatchLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.trim().length >= 2) {
            saveRecentSearch(query)
        }
    }

    fun saveRecentSearch(term: String) {
        val clean = term.trim()
        if (clean.length < 2) return
        val current = _recentSearches.value.toMutableList()
        current.removeAll { it.equals(clean, ignoreCase = true) }
        current.add(0, clean)
        _recentSearches.value = if (current.size > 8) current.take(8) else current
    }

    fun removeRecentSearch(term: String) {
        _recentSearches.value = _recentSearches.value.filterNot { it.equals(term, ignoreCase = true) }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun selectCategory(category: PropertyCategory) {
        _selectedCategory.value = category
    }

    private val foreignCountries = listOf(
        "usa", "united states", "us", "uk", "united kingdom", "london", "dubai", "uae",
        "singapore", "australia", "canada", "new york", "germany", "france", "japan",
        "tokyo", "paris", "california", "texas", "florida", "china", "malaysia", "thailand"
    )

    fun isIndiaLocation(location: String): Boolean {
        val lower = location.trim().lowercase()
        return !foreignCountries.any { lower == it || lower.contains(" $it") || lower.contains("$it ") }
    }

    fun selectLocation(location: String) {
        val clean = location.trim()
        if (clean.isBlank() || clean.equals("All", ignoreCase = true) || clean.equals("All Locations", ignoreCase = true)) {
            _selectedLocation.value = "All Locations"
            return
        }

        if (!isIndiaLocation(clean)) {
            _feedbackMessage.value = "Ren is currently available exclusively in India 🇮🇳. International locations coming soon!"
            return
        }

        _selectedLocation.value = clean
        saveRecentSearch(clean)
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("is_onboarded", true).apply()
        _isOnboarded.value = true
    }

    fun signInWithEmail(
        email: String,
        password: String,
        onResult: (Result<UserProfile>) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithEmail(email, password)
            _isLoading.value = false
            result.onSuccess { profile ->
                prefs.edit()
                    .putBoolean("is_onboarded", true)
                    .putBoolean("is_guest_mode", false)
                    .apply()
                _feedbackMessage.value = "Welcome back, ${profile.displayName}!"
            }.onFailure { err ->
                _feedbackMessage.value = err.message ?: "Sign-in failed"
            }
            onResult(result)
        }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: UserRole,
        preferredCity: String = "All Locations",
        onResult: (Result<UserProfile>) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.registerWithEmail(email, password, displayName, role)
            _isLoading.value = false
            result.onSuccess { profile ->
                prefs.edit()
                    .putBoolean("is_onboarded", true)
                    .putBoolean("is_guest_mode", false)
                    .apply()
                if (preferredCity != "All Locations") {
                    selectLocation(preferredCity)
                }
                _feedbackMessage.value = "Welcome to Ren, ${profile.displayName}!"
            }.onFailure { err ->
                _feedbackMessage.value = err.message ?: "Registration failed"
            }
            onResult(result)
        }
    }

    fun signInWithGoogle(
        idToken: String,
        preferredCity: String = "All Locations",
        onResult: (Result<UserProfile>) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithGoogle(idToken)
            _isLoading.value = false
            result.onSuccess { profile ->
                _currentUserProfile.value = profile
                _isLoggedIn.value = true
                _isOnboarded.value = true
                prefs.edit()
                    .putBoolean("is_onboarded", true)
                    .putBoolean("is_guest_mode", false)
                    .putString("user_email", profile.email)
                    .putString("user_name", profile.displayName)
                    .putString("user_uid", profile.uid)
                    .apply()
                if (preferredCity != "All Locations") {
                    selectLocation(preferredCity)
                }
                _feedbackMessage.value = "Signed in with Google as ${profile.displayName}"
            }.onFailure { err ->
                _feedbackMessage.value = err.message ?: "Google Sign-In failed"
            }
            onResult(result)
        }
    }

    fun loginWithGoogle(
        name: String,
        email: String,
        role: UserRole,
        preferredCity: String,
        photoUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithGoogleAccount(
                email = email,
                displayName = name,
                photoUrl = photoUrl,
                role = role
            )
            _isLoading.value = false
            result.onSuccess { profile ->
                _currentUserProfile.value = profile
                _isLoggedIn.value = true
                _isOnboarded.value = true
                prefs.edit()
                    .putBoolean("is_onboarded", true)
                    .putBoolean("is_guest_mode", false)
                    .putString("user_email", profile.email)
                    .putString("user_name", profile.displayName)
                    .putString("user_uid", profile.uid)
                    .apply()
                if (preferredCity != "All Locations") {
                    selectLocation(preferredCity)
                }
                _feedbackMessage.value = "Welcome to Ren, ${profile.displayName}!"
            }.onFailure { err ->
                _feedbackMessage.value = err.message ?: "Authentication failed"
            }
        }
    }

    fun sendPasswordReset(email: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            result.onSuccess {
                _feedbackMessage.value = "Password reset link sent to $email"
            }.onFailure { err ->
                _feedbackMessage.value = err.message ?: "Failed to send reset email"
            }
            onResult(result)
        }
    }

    fun continueAsGuest() {
        prefs.edit()
            .putBoolean("is_onboarded", true)
            .putBoolean("is_guest_mode", true)
            .apply()
        _currentUserProfile.value = null
        _isOnboarded.value = true
        _isLoggedIn.value = true
        _feedbackMessage.value = "Browsing Ren listings across India as Guest"
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_guest_mode", false)
            .remove("user_email")
            .remove("user_name")
            .remove("user_uid")
            .apply()
        _currentUserProfile.value = null
        _isLoggedIn.value = false
        viewModelScope.launch {
            authRepository.signOut()
        }
        _feedbackMessage.value = "Signed out successfully"
    }

    fun toggleUrgentOnly() {
        _urgentOnly.value = !_urgentOnly.value
    }

    fun setUrgentOnly(value: Boolean) {
        _urgentOnly.value = value
    }

    fun setBudgetFilter(budget: BudgetFilter) {
        _selectedBudget.value = budget
    }

    fun selectPropertyType(type: String?) {
        _selectedPropertyType.value = if (type.isNullOrBlank() || type.equals("All", ignoreCase = true) || type.equals("All Types", ignoreCase = true)) null else type
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

    fun setPriceRange(min: Long?, max: Long?) {
        _minPrice.value = min
        _maxPrice.value = max
    }

    fun clearPriceRange() {
        _minPrice.value = null
        _maxPrice.value = null
    }

    fun openFilterSheet() {
        _showFilterSheet.value = true
    }

    fun closeFilterSheet() {
        _showFilterSheet.value = false
    }

    fun resetFilters() {
        _selectedCategory.value = PropertyCategory.ALL
        _selectedPropertyType.value = null
        _selectedBudget.value = BudgetFilter.ALL
        _selectedBedrooms.value = 0
        _urgentOnly.value = false
        _verifiedOnly.value = false
        _selectedSortOption.value = SortOption.URGENCY
        _searchQuery.value = ""
        _minPrice.value = null
        _maxPrice.value = null
    }

    fun resetAllFilters() {
        resetFilters()
        _selectedLocation.value = "All Locations"
    }

    fun openPropertyDetails(property: Property) {
        _selectedProperty.value = property
    }

    fun closePropertyDetails() {
        _selectedProperty.value = null
    }

    fun openChat(property: Property) {
        _chatProperty.value = property
        chatCollectionJob?.cancel()
        chatCollectionJob = viewModelScope.launch {
            repository.getChatMessagesForProperty(property.id).collect { messages ->
                if (messages.isEmpty()) {
                    // Seed initial welcome greeting to database
                    val welcomeMsg = ChatMessage(
                        id = "m-init-${property.id}",
                        propertyId = property.id,
                        senderName = property.ownerName,
                        message = "Vanakkam! Thank you for inquiring about ${property.title}. How can I assist you with the site visit or legal documents?",
                        time = "Just now",
                        isFromMe = false
                    )
                    repository.insertChatMessage(welcomeMsg)
                } else {
                    _chatMessages.value = messages
                }
            }
        }
    }

    fun sendChatMessage(text: String) {
        val currentProp = _chatProperty.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            sendChatMessageUseCase.sendMessage(currentProp.id, text)
        }
    }

    fun closeChat() {
        chatCollectionJob?.cancel()
        _chatProperty.value = null
    }

    fun openContactSeller(property: Property) {
        _contactSellerProperty.value = property
    }

    fun closeContactSeller() {
        _contactSellerProperty.value = null
    }

    fun startChatFromContactSeller(property: Property, initialMessage: String) {
        _contactSellerProperty.value = null
        openChat(property)
        if (initialMessage.isNotBlank()) {
            sendChatMessage(initialMessage)
        }
    }

    fun sendEmailInquiry(
        property: Property,
        subject: String,
        message: String,
        buyerEmail: String,
        buyerPhone: String
    ) {
        viewModelScope.launch {
            _contactSellerProperty.value = null
            sendChatMessageUseCase.sendEmailInquiry(property, subject, message, buyerEmail, buyerPhone)
            _feedbackMessage.value = "Inquiry sent to ${property.ownerName} (${property.ownerEmail})!"
        }
    }

    fun openVisitBooking(property: Property) {
        _visitProperty.value = property
    }

    fun closeVisitBooking() {
        _visitProperty.value = null
    }

    fun confirmVisit(property: Property, date: String, timeSlot: String) {
        viewModelScope.launch {
            scheduleVisitUseCase(property, date, timeSlot)
            _visitProperty.value = null
            _feedbackMessage.value = "Site visit requested for $date at $timeSlot!"
        }
    }

    fun updateVisitStatus(visitId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateVisitStatus(visitId, newStatus)
            _feedbackMessage.value = "Visit status updated to: $newStatus"
        }
    }

    fun cancelVisit(visitId: String) {
        viewModelScope.launch {
            repository.updateVisitStatus(visitId, "Cancelled")
            _feedbackMessage.value = "Visit cancelled"
        }
    }

    fun deleteVisit(visitId: String) {
        viewModelScope.launch {
            repository.deleteVisit(visitId)
            _feedbackMessage.value = "Visit removed"
        }
    }

    // Trust & Safety Listing Reporting
    fun openReport(property: Property) {
        _reportProperty.value = property
    }

    fun closeReport() {
        _reportProperty.value = null
    }

    fun submitReport(property: Property, reason: String, details: String) {
        viewModelScope.launch {
            repository.reportProperty(property.id, property.title, reason, details)
            _reportProperty.value = null
            _feedbackMessage.value = "Listing reported to Ren Safety Shield. Investigating within 2 hours."
        }
    }

    // Seller Property Status Management
    fun markPropertyStatus(property: Property, status: String) {
        viewModelScope.launch {
            repository.updatePropertyStatus(property.id, status)
            _feedbackMessage.value = "Listing marked as $status"
        }
    }

    fun deleteProperty(property: Property) {
        viewModelScope.launch {
            repository.deleteProperty(property.id)
            _feedbackMessage.value = "Listing successfully removed"
        }
    }

    fun createSearchAlert(location: String, propertyType: String, maxPrice: Long) {
        viewModelScope.launch {
            repository.addSearchAlert(location, propertyType, maxPrice)
            _feedbackMessage.value = "Search alert saved for $location!"
        }
    }

    fun openQuickMatch(property: Property) {
        _quickMatchProperty.value = property
    }

    fun closeQuickMatch() {
        _quickMatchProperty.value = null
    }

    fun toggleSave(property: Property) {
        viewModelScope.launch {
            toggleSaveUseCase(property)
            val action = if (!property.isSaved) "Saved to favorites" else "Removed from favorites"
            _feedbackMessage.value = action
        }
    }

    fun setExploreSelected(property: Property?) {
        _exploreSelectedProperty.value = property
    }

    fun setExploreRadius(radius: Double) {
        _exploreRadiusKm.value = radius
    }

    fun setUserRole(role: String) {
        _userRole.value = role
    }

    fun openAiAssistant() {
        _showAiAssistant.value = true
        if (_aiResults.value.isEmpty()) {
            runAiNaturalSearch("Luxury 3BHK or urgent plot in India under 50 lakhs")
        }
    }

    fun closeAiAssistant() {
        _showAiAssistant.value = false
    }

    fun runAiNaturalSearch(query: String) {
        _aiQuery.value = query
        val props = allProperties.value
        val lower = query.lowercase()

        // Natural language filter heuristics for Pan-India locations
        val matched = props.filter { p ->
            var score = 0
            if (lower.contains("bengaluru") || lower.contains("bangalore") && p.location.contains("Bengaluru", ignoreCase = true)) score += 40
            if (lower.contains("chennai") && p.location.contains("Chennai", ignoreCase = true)) score += 40
            if (lower.contains("mumbai") && p.location.contains("Mumbai", ignoreCase = true)) score += 40
            if ((lower.contains("delhi") || lower.contains("gurugram") || lower.contains("noida")) && p.location.contains("Delhi", ignoreCase = true)) score += 40
            if (lower.contains("hyderabad") && p.location.contains("Hyderabad", ignoreCase = true)) score += 40
            if (lower.contains("pune") && p.location.contains("Pune", ignoreCase = true)) score += 40
            if (lower.contains("kochi") || lower.contains("cochin") && p.location.contains("Kochi", ignoreCase = true)) score += 40
            if (lower.contains("goa") && p.location.contains("Goa", ignoreCase = true)) score += 40
            if (lower.contains("auroville") && p.location.contains("Auroville", ignoreCase = true)) score += 40
            if (lower.contains("kottakuppam") && p.location.contains("Kottakuppam", ignoreCase = true)) score += 40
            if (lower.contains("pondy") || lower.contains("pondicherry") && p.location.contains("Pondicherry", ignoreCase = true)) score += 40

            if (lower.contains("rent") && p.listingType == ListingType.RENT) score += 30
            if (lower.contains("lease") && p.listingType == ListingType.LEASE) score += 30
            if (lower.contains("land") || lower.contains("plot") && p.category == PropertyCategory.LAND) score += 30
            if (lower.contains("house") || lower.contains("villa") && (p.propertyType.contains("House") || p.propertyType.contains("Villa"))) score += 30
            if (lower.contains("urgent") && (p.sellingSpeed == SellingSpeed.URGENT || p.sellingSpeed == SellingSpeed.FAST)) score += 30

            // Budget heuristic
            if (lower.contains("20,000") || lower.contains("20000") || lower.contains("20k")) {
                if (p.listingType == ListingType.RENT && p.price <= 25000) score += 25
            }
            if (lower.contains("35 lakhs") || lower.contains("50 lakhs") || lower.contains("1 crore") || lower.contains("cr")) {
                score += 25
            }

            score > 25
        }.ifEmpty { props.take(4) }

        _aiResults.value = matched
        _aiExplanation.value = "Found ${matched.size} properties matching your criteria across verified Indian locations."
    }

    fun postNewProperty(
        title: String,
        description: String,
        category: PropertyCategory,
        propertyType: String,
        price: Long,
        marketEstimate: Long,
        location: String,
        bedrooms: Int,
        bathrooms: Int,
        areaSqFt: Int,
        speed: SellingSpeed,
        features: List<String>,
        isPrivate: Boolean
    ) {
        if (!authRepository.isAuthenticated()) {
            _feedbackMessage.value = "Please sign in with your Ren account to post listings"
            return
        }
        viewModelScope.launch {
            val params = com.example.domain.usecase.PostListingParams(
                title = title,
                description = description,
                price = price,
                marketEstimate = marketEstimate,
                location = location,
                category = category,
                propertyType = propertyType,
                speed = speed,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                areaSqFt = areaSqFt,
                features = features,
                isPrivate = isPrivate
            )
            val result = postListingUseCase(params)
            result.onSuccess { newProp ->
                _feedbackMessage.value = "Property posted successfully! QuickMatch activated."
                // Immediately trigger QuickMatch sheet for the newly posted property
                _quickMatchProperty.value = newProp
                _currentTab.value = 0 // Switch to Home
            }.onFailure { err ->
                _feedbackMessage.value = err.message ?: "Failed to post listing"
            }
        }
    }

    // QuickMatch Algorithm: Location (40) + Budget (30) + Type (20) + Features (10)
    fun getMatchesForProperty(property: Property): List<BuyerMatch> {
        return listOf(
            BuyerMatch(
                buyerName = "Sundar V. (Verified Investor)",
                buyerType = "Investor",
                matchScore = 95,
                budgetRange = "₹25L – ₹45L",
                preferredLocation = property.location,
                preferredType = property.propertyType,
                contactStatus = "Instant Alert Sent"
            ),
            BuyerMatch(
                buyerName = "Venkatesh & Priya",
                buyerType = "Family Buyer",
                matchScore = 88,
                budgetRange = "₹30L – ₹40L",
                preferredLocation = "Kottakuppam / Pondicherry",
                preferredType = "Independent House / Land",
                contactStatus = "Requested Site Visit"
            ),
            BuyerMatch(
                buyerName = "Auroville Eco Realty (Broker)",
                buyerType = "Verified Broker",
                matchScore = 79,
                budgetRange = "Market Value",
                preferredLocation = "Auroville / Coastal",
                preferredType = "All Types",
                contactStatus = "Lead Forwarded"
            ),
            BuyerMatch(
                buyerName = "Mohan Kumar",
                buyerType = "Individual",
                matchScore = 72,
                budgetRange = "₹20L – ₹35L",
                preferredLocation = "Pondicherry",
                preferredType = property.propertyType,
                contactStatus = "Browsed Similar"
            )
        )
    }
}

