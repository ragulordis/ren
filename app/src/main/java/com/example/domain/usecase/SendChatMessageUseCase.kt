package com.example.domain.usecase

import com.example.data.model.ChatMessage
import com.example.data.model.Property
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * UseCase to handle real-time chat transmissions without fake bot simulations.
 */
class SendChatMessageUseCase(
    private val repository: PropertyRepository,
    private val authRepository: AuthRepository
) {
    fun getMessages(propertyId: String): Flow<List<ChatMessage>> {
        // Stream from Firestore, caching to local Room, while emitting local Room messages
        return kotlinx.coroutines.flow.channelFlow {
            val myId = authRepository.currentUserId() ?: ""
            val myName = authRepository.currentUser()?.displayName

            // First launch a collector for remote messages to persist them to Room
            val remoteJob = launch {
                // Pass explicit buyerId so FirestoreService uses the scoped conversation path
                repository.streamChatMessages(propertyId, myId).collect { remoteMsgs ->
                    remoteMsgs.forEach { remoteMsg ->
                        val isMine = if (myId.isNotBlank() && remoteMsg.senderId.isNotBlank()) {
                            remoteMsg.senderId == myId
                        } else {
                            remoteMsg.isFromMe || (myId.isNotBlank() && remoteMsg.senderName == myName)
                        }
                        repository.insertChatMessage(remoteMsg.copy(isFromMe = isMine), buyerId = myId)
                    }
                }
            }

            // Emit from local Room as single source of truth
            repository.getChatMessagesForProperty(propertyId).collect { localMsgs ->
                send(localMsgs)
            }

            remoteJob.cancel()
        }
    }

    suspend fun sendMessage(propertyId: String, text: String): Result<ChatMessage> = runCatching {
        require(text.isNotBlank()) { "Message text cannot be blank" }
        val user = authRepository.currentUser()
        val myId = authRepository.currentUserId() ?: user?.uid ?: ""
        val message = ChatMessage(
            id = "m-${System.currentTimeMillis()}",
            propertyId = propertyId,
            senderId = myId,
            senderName = user?.displayName?.ifBlank { "Buyer" } ?: "Buyer",
            message = text.trim(),
            time = "Just now",
            isFromMe = true
        )
        // Pass myId as explicit buyerId so the scoped conversation path is used
        repository.insertChatMessage(message, buyerId = myId)
        message
    }

    suspend fun sendEmailInquiry(
        property: Property,
        subject: String,
        messageBody: String,
        buyerEmail: String,
        buyerPhone: String
    ): Result<ChatMessage> = runCatching {
        val user = authRepository.currentUser()
        val myId = authRepository.currentUserId() ?: user?.uid ?: ""
        val senderLabel = buyerEmail.ifBlank { user?.displayName?.ifBlank { "Buyer" } ?: "Buyer" }
        val emailLogMsg = ChatMessage(
            id = "m-email-${System.currentTimeMillis()}",
            propertyId = property.id,
            senderId = myId,
            senderName = senderLabel,
            message = "📧 Formal Email Inquiry Sent:\nSubject: $subject\n\n$messageBody\n\nContact: $buyerPhone | $buyerEmail",
            time = "Just now",
            isFromMe = true
        )
        repository.insertChatMessage(emailLogMsg)
        emailLogMsg
    }
}
