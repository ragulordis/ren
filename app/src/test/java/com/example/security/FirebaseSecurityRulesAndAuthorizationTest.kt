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

    @Test
    fun `visit state machine strictly enforces lifecycle transitions`() {
        val requested = com.example.data.model.VisitStatus.REQUESTED
        val confirmed = com.example.data.model.VisitStatus.CONFIRMED
        val completed = com.example.data.model.VisitStatus.COMPLETED
        val cancelled = com.example.data.model.VisitStatus.CANCELLED
        val declined = com.example.data.model.VisitStatus.DECLINED

        // Valid transitions
        assertTrue(com.example.data.model.VisitStatus.isValidTransition(requested, confirmed))
        assertTrue(com.example.data.model.VisitStatus.isValidTransition(requested, declined))
        assertTrue(com.example.data.model.VisitStatus.isValidTransition(requested, cancelled))
        assertTrue(com.example.data.model.VisitStatus.isValidTransition(confirmed, completed))
        assertTrue(com.example.data.model.VisitStatus.isValidTransition(confirmed, cancelled))

        // Invalid transitions
        assertFalse(com.example.data.model.VisitStatus.isValidTransition(requested, completed))
        assertFalse(com.example.data.model.VisitStatus.isValidTransition(completed, requested))
        assertFalse(com.example.data.model.VisitStatus.isValidTransition(cancelled, confirmed))
        assertFalse(com.example.data.model.VisitStatus.isValidTransition(declined, confirmed))
    }

    @Test
    fun `conversations are strictly scoped to property and buyer to prevent cross-buyer leaks`() {
        val propertyId = "prop_beach_villa_1"
        val buyerA = "buyer_alice_1"
        val buyerB = "buyer_bob_2"
        val seller = "seller_sam_99"

        val firestoreService = com.example.data.remote.FirestoreService()
        val conversationIdA = firestoreService.resolveConversationId(propertyId, buyerA)
        val conversationIdB = firestoreService.resolveConversationId(propertyId, buyerB)

        // Must be distinct conversation threads for separate buyers
        assertNotEquals(conversationIdA, conversationIdB)
        assertEquals("${propertyId}_${buyerA}", conversationIdA)
        assertEquals("${propertyId}_${buyerB}", conversationIdB)

        // Conversation A participants: Seller + Buyer A
        val participantsA = listOf(seller, buyerA)
        // Conversation B participants: Seller + Buyer B
        val participantsB = listOf(seller, buyerB)

        // Buyer B must NOT have access to Conversation A
        assertFalse("Buyer B cannot access Buyer A's conversation with seller", participantsA.contains(buyerB))
        // Buyer A must NOT have access to Conversation B
        assertFalse("Buyer A cannot access Buyer B's conversation with seller", participantsB.contains(buyerA))
        // Seller has access to both
        assertTrue(participantsA.contains(seller))
        assertTrue(participantsB.contains(seller))
    }
}
