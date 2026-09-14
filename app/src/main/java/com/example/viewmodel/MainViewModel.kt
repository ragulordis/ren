package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BuyerMatch
import com.example.data.model.ChatMessage
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.PropertyVisit
import com.example.data.model.SellingSpeed
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository
import com.example.domain.usecase.ScheduleVisitUseCase
import com.example.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val propertyRepository: PropertyRepository,
    private val authRepository: AuthRepository,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val aiSearchUseCase: com.example.domain.usecase.AiSearchUseCase = com.example.domain.usecase.AiSearchUseCase(),
    private val scheduleVisitUseCase: ScheduleVisitUseCase = ScheduleVisitUseCase(propertyRepository, authRepository)
) : ViewModel() {

    // Tab Navigation State (0: Home, 1: Explore, 2: Post, 3: Saved, 4: Profile)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // Snackbar / Feedback Messages
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun showFeedback(message: String) {
        _feedbackMessage.value = message
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    // Modal Sheets & Dialogs State
    private val _selectedProperty = MutableStateFlow<Property?>(null)
    val selectedProperty: StateFlow<Property?> = _selectedProperty.asStateFlow()

    private val _contactSellerProperty = MutableStateFlow<Property?>(null)
    val contactSellerProperty: StateFlow<Property?> = _contactSellerProperty.asStateFlow()

    private val _chatProperty = MutableStateFlow<Property?>(null)
    val chatProperty: StateFlow<Property?> = _chatProperty.asStateFlow()

    private val _visitProperty = MutableStateFlow<Property?>(null)
    val visitProperty: StateFlow<Property?> = _visitProperty.asStateFlow()

    private val _quickMatchProperty = MutableStateFlow<Property?>(null)
    val quickMatchProperty: StateFlow<Property?> = _quickMatchProperty.asStateFlow()

    private val _reportProperty = MutableStateFlow<Property?>(null)
    val reportProperty: StateFlow<Property?> = _reportProperty.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    private val _showAiAssistant = MutableStateFlow(false)
    val showAiAssistant: StateFlow<Boolean> = _showAiAssistant.asStateFlow()

    private val _showSmartMatchDialog = MutableStateFlow(false)
    val showSmartMatchDialog: StateFlow<Boolean> = _showSmartMatchDialog.asStateFlow()

    // Chat Message Flow
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    private var chatCollectionJob: Job? = null

    // AI Natural Search State
    private val _aiQuery = MutableStateFlow("")
    val aiQuery: StateFlow<String> = _aiQuery.asStateFlow()

    private val _aiResults = MutableStateFlow<List<Property>>(emptyList())
    val aiResults: StateFlow<List<Property>> = _aiResults.asStateFlow()

    private val _aiExplanation = MutableStateFlow("")
    val aiExplanation: StateFlow<String> = _aiExplanation.asStateFlow()

    val allProperties: StateFlow<List<Property>> = propertyRepository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog & Sheet Controls
    fun openPropertyDetails(property: Property) {
        _selectedProperty.value = property
    }

    fun closePropertyDetails() {
        _selectedProperty.value = null
    }

    fun openContactSeller(property: Property) {
        _contactSellerProperty.value = property
    }

    fun closeContactSeller() {
        _contactSellerProperty.value = null
    }

    fun openChat(property: Property) {
        _chatProperty.value = property
        chatCollectionJob?.cancel()
        chatCollectionJob = viewModelScope.launch {
            sendChatMessageUseCase.getMessages(property.id).collect { messages ->
                _chatMessages.value = messages
            }
        }
    }

    fun closeChat() {
        chatCollectionJob?.cancel()
        _chatProperty.value = null
        _chatMessages.value = emptyList()
    }

    fun sendChatMessage(text: String) {
        val currentProp = _chatProperty.value ?: return
        viewModelScope.launch {
            val result = sendChatMessageUseCase.sendMessage(currentProp.id, text)
            result.onFailure {
                showFeedback("Failed to send message: ${it.message}")
            }
        }
    }

    fun startChatFromContactSeller(property: Property, initialMessage: String) {
        closeContactSeller()
        openChat(property)
        if (initialMessage.isNotBlank()) {
            sendChatMessage(initialMessage)
        }
    }

    fun sendEmailInquiry(
        property: Property,
        subject: String,
        messageBody: String,
        buyerEmail: String,
        buyerPhone: String
    ) {
        viewModelScope.launch {
            val result = sendChatMessageUseCase.sendEmailInquiry(
                property = property,
                subject = subject,
                messageBody = messageBody,
                buyerEmail = buyerEmail,
                buyerPhone = buyerPhone
            )
            result.onSuccess {
                showFeedback("Official inquiry sent to ${property.ownerName}")
                closeContactSeller()
            }.onFailure {
                showFeedback("Failed to record inquiry: ${it.message}")
            }
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
            val result = scheduleVisitUseCase(property, date, timeSlot)
            result.onSuccess {
                showFeedback("Visit scheduled for $date at $timeSlot")
                closeVisitBooking()
            }.onFailure {
                showFeedback("Failed to schedule visit: ${it.message}")
            }
        }
    }

    fun openReport(property: Property) {
        _reportProperty.value = property
    }

    fun closeReport() {
        _reportProperty.value = null
    }

    fun submitReport(property: Property, reason: String, details: String) {
        viewModelScope.launch {
            propertyRepository.reportProperty(property.id, property.title, reason, details)
            showFeedback("Report submitted for review")
            closeReport()
        }
    }

    fun openQuickMatch(property: Property) {
        _quickMatchProperty.value = property
    }

    fun closeQuickMatch() {
        _quickMatchProperty.value = null
    }

    fun openFilterSheet() {
        _showFilterSheet.value = true
    }

    fun closeFilterSheet() {
        _showFilterSheet.value = false
    }

    fun openAiAssistant() {
        _showAiAssistant.value = true
    }

    fun closeAiAssistant() {
        _showAiAssistant.value = false
    }

    fun openSmartMatchDialog() {
        _showSmartMatchDialog.value = true
    }

    fun closeSmartMatchDialog() {
        _showSmartMatchDialog.value = false
    }

    fun toggleSave(property: Property) {
        viewModelScope.launch {
            propertyRepository.toggleSave(property.id, property.isSaved)
            val msg = if (!property.isSaved) "Saved to your list" else "Removed from saved"
            showFeedback(msg)
        }
    }

    fun runAiNaturalSearch(query: String) {
        _aiQuery.value = query
        _aiExplanation.value = "Analyzing properties with Ren AI assistant..."
        val props = allProperties.value

        viewModelScope.launch {
            val result = aiSearchUseCase(query, props)
            _aiResults.value = result.matchedProperties
            _aiExplanation.value = result.explanation
        }
    }

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
