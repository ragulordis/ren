# Ren / QuickNest — Real Estate Marketplace Architecture

## Environment Strategy
- **Development (DEV)**: Local Room cache with offline previews. Never pushes sample fixtures to remote Firestore.
- **Staging (STAGING)**: Firebase project with controlled test fixtures and security rules emulator.
- **Production (PROD)**: Live Firestore, Cloud Storage, Firebase Auth. Zero fake data insertion. Server-authoritative state machine for listings and verification.

## Zero Fake Data Policy (P0)
1. **No Hardcoded Identities**: All user actions (posting, messaging, visit requests) derive from `FirebaseAuth.getInstance().currentUser?.uid`.
2. **No Automatic Sample Production Seeding**: Client never pushes mock properties to the remote Firestore `properties` collection on empty query.
3. **No Simulated Bot Replies**: Chat interactions are real messages between buyer and seller accounts.
4. **No Client-Fabricated Verification**: Property listings start with verification level 0 (Unverified / Pending review). Only authorized moderation workflows can verify properties.
5. **No Synthetic Stats**: Views, saves, inquiries, and matched buyer counts start at real values (0) and are calculated accurately.

## Security & Rules
- Firestore security rules: `firebase/firestore.rules`
- Firestore composite indexes: `firebase/firestore.indexes.json`
- Storage security rules: `firebase/storage.rules`
