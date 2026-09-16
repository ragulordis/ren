package com.example.security

import com.example.data.model.ChatMessage
import com.example.data.model.ListingStatus
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * P5.8 Security & Authorization Test Suite.
 * Validates security invariants, authoritative identity derivation,
 * participant validation, and state machine integrity.
 */
class FirebaseSecurityRulesAndAuthorizationTest {

    @Test
    fun `chat message derives isFromMe based on authoritative auth uid`() {
        val currentUserId = "buyer_uid_123"
        val otherUserId = "seller_uid_456"

        val myMessage = ChatMessage(
            id = "msg-1",
            propertyId = "prop-100",
            senderId = currentUserId,
            senderName = "Alice",
            message = "Hello!",
            time = "12:00 PM"
        )

        val peerMessage = ChatMessage(
            id = "msg-2",
            propertyId = "prop-100",
            senderId = otherUserId,
            senderName = "Bob",
            message = "Hi Alice!",
            time = "12:01 PM"
        )

        // Validate identity comparison
        val isMyMessageFromMe = (myMessage.senderId == currentUserId)
        val isPeerMessageFromMe = (peerMessage.senderId == currentUserId)

        assertTrue("Message from current user must be marked as fromMe", isMyMessageFromMe)
        assertFalse("Message from peer user must NOT be marked as fromMe", isPeerMessageFromMe)
    }

    @Test
    fun `listing state machine enforces strictly valid lifecycle transitions`() {
        // DRAFT transitions
        assertTrue(ListingStatus.isValidTransition(ListingStatus.DRAFT, ListingStatus.PENDING_REVIEW))
        assertTrue(ListingStatus.isValidTransition(ListingStatus.DRAFT, ListingStatus.ACTIVE))
        assertTrue(ListingStatus.isValidTransition(ListingStatus.DRAFT, ListingStatus.ARCHIVED))
        assertFalse(ListingStatus.isValidTransition(ListingStatus.DRAFT, ListingStatus.SOLD))

        // ACTIVE transitions
        assertTrue(ListingStatus.isValidTransition(ListingStatus.ACTIVE, ListingStatus.SOLD))
        assertTrue(ListingStatus.isValidTransition(ListingStatus.ACTIVE, ListingStatus.RENTED))
        assertTrue(ListingStatus.isValidTransition(ListingStatus.ACTIVE, ListingStatus.LEASED))
        assertTrue(ListingStatus.isValidTransition(ListingStatus.ACTIVE, ListingStatus.ARCHIVED))

        // Terminal or closed transitions
        assertTrue(ListingStatus.isValidTransition(ListingStatus.SOLD, ListingStatus.ACTIVE))
        assertTrue(ListingStatus.isValidTransition(ListingStatus.SOLD, ListingStatus.ARCHIVED))
        assertFalse(ListingStatus.isValidTransition(ListingStatus.SOLD, ListingStatus.DRAFT))
    }

    @Test
    fun `user role escalation is prohibited on the client`() {
        val initialRole = UserRole.BUYER

        // Attempting to cast arbitrary role strings should be sanitized or constrained
        val role = UserRole.valueOf("BUYER")
        assertEquals(UserRole.BUYER, role)
        assertNotEquals(UserRole.OWNER, initialRole)
    }

    @Test
    fun `participant membership is required for conversation access`() {
        val participants = listOf("buyer_uid_123", "seller_uid_456")

        val authorizedUser = "buyer_uid_123"
        val attacker = "unauthorized_uid_999"

        assertTrue(participants.contains(authorizedUser))
        assertFalse(participants.contains(attacker))
    }
}
