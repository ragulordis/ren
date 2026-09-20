import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { getStorage } from "firebase-admin/storage";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { onDocumentCreated } from "firebase-functions/v2/firestore";

initializeApp();
const db = getFirestore();

function requireModerator(uid: string, claims: Record<string, unknown>) {
  if (claims.moderator !== true && claims.admin !== true) {
    throw new HttpsError("permission-denied", "Moderator access is required.");
  }
}

/** Approves/rejects a listing from a trusted staff dashboard or Admin SDK client. */
export const moderateListing = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in required.");
  requireModerator(request.auth.uid, request.auth.token);
  const propertyId = String(request.data.propertyId ?? "");
  const decision = String(request.data.decision ?? "");
  if (!propertyId || !["ACTIVE", "REMOVED", "REJECTED"].includes(decision)) {
    throw new HttpsError("invalid-argument", "A property and valid decision are required.");
  }
  await db.collection("properties").doc(propertyId).update({
    moderationStatus: decision,
    status: decision === "ACTIVE" ? "Active" : "Archived",
    moderatedAt: FieldValue.serverTimestamp(),
    moderatedBy: request.auth.uid
  });
  return { ok: true };
});

/** Deletes the caller's account and app data; clients cannot perform this directly. */
export const deleteAccount = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in required.");
  const uid = request.auth.uid;
  const properties = await db.collection("properties").where("ownerId", "==", uid).get();
  const bucket = getStorage().bucket();
  await Promise.all(properties.docs.map(async (property) => {
    await bucket.deleteFiles({ prefix: `property_images/${property.id}/`, force: true });
    await db.recursiveDelete(property.ref);
  }));
  for (const field of ["buyerId", "sellerId"]) {
    const snapshot = await db.collection("visits").where(field, "==", uid).get();
    await Promise.all(snapshot.docs.map((doc) => db.recursiveDelete(doc.ref)));
  }
  const reports = await db.collection("reports").where("reporterId", "==", uid).get();
  await Promise.all(reports.docs.map((doc) => db.recursiveDelete(doc.ref)));
  const conversations = await db.collection("conversations").where("participants", "array-contains", uid).get();
  await Promise.all(conversations.docs.map((doc) => db.recursiveDelete(doc.ref)));
  await db.recursiveDelete(db.doc(`users/${uid}`));
  await getAuth().deleteUser(uid);
  return { ok: true };
});

/** Delivers an owner notification for each new inbound chat message. */
export const notifyConversationParticipant = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
  async (event) => {
    const message = event.data?.data();
    const conversation = await db.doc(`conversations/${event.params.conversationId}`).get();
    const participants = (conversation.get("participants") as string[] | undefined) ?? [];
    const recipients = participants.filter((uid) => uid && uid !== message?.senderId);
    await Promise.all(recipients.map(async (uid) => {
      const tokens = await db.collection(`users/${uid}/devices`).get();
      await Promise.all(tokens.docs.map(async (token) => {
        const value = token.get("fcmToken") as string | undefined;
        if (!value) return;
        try {
          await getMessaging().send({ token: value, notification: { title: "New Ren inquiry", body: String(message?.message ?? "New message") }, data: { conversationId: event.params.conversationId } });
        } catch {
          await token.ref.delete();
        }
      }));
    }));
  }
);
