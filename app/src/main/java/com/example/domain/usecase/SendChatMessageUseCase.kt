package com.example.domain.usecase

import com.example.data.model.ChatMessage
import com.example.data.model.Property
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase to handle real-time chat transmissions without fake bot simulations.
 */
class SendChatMessageUseCase(
    private val repository: PropertyRepository,
    private val authRepository: AuthRepository
) {
    fun getMessages(propertyId: String): Flow<List<ChatMessage>> =
        repository.getChatMessagesForProperty(propertyId)

    suspend fun sendMessage(propertyId: String, text: String): Result<ChatMessage> = runCatching {
        require(text.isNotBlank()) { "Message text cannot be blank" }
        val user = authRepository.getCurrentUser()
        val message = ChatMessage(
            id = "m-${System.currentTimeMillis()}",
            propertyId = propertyId,
            senderName = user.displayName.ifBlank { "Buyer" },
            message = text.trim(),
            time = "Just now",
            isFromMe = true
        )
        repository.insertChatMessage(message)
        message
    }

    suspend fun sendEmailInquiry(
        property: Property,
        subject: String,
        messageBody: String,
        buyerEmail: String,
        buyerPhone: String
    ): Result<ChatMessage> = runCatching {
        val user = authRepository.getCurrentUser()
        val senderLabel = buyerEmail.ifBlank { user.displayName.ifBlank { "Buyer" } }
        val emailLogMsg = ChatMessage(
            id = "m-email-${System.currentTimeMillis()}",
            propertyId = property.id,
            senderName = senderLabel,
            message = "📧 Formal Email Inquiry Sent:\nSubject: $subject\n\n$messageBody\n\nContact: $buyerPhone | $buyerEmail",
            time = "Just now",
            isFromMe = true
        )
        repository.insertChatMessage(emailLogMsg)
        emailLogMsg
    }
}
