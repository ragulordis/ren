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

## Release readiness

Ren v1 is rental-first. New listings are created as **Pending Review** and must
be approved by a trusted moderation workflow before public discovery. Property
owner phone numbers and email addresses are not stored in public listings;
renters contact owners through Ren Chat.

The repository now includes trusted moderation, server-side account deletion,
two-way user blocking, and recipient push notifications. Before a Play Store
release, deploy the Firebase rules, indexes, Storage rules, and functions to
the production project, then validate a signed release bundle with two real
accounts.

## Firebase deployment

The `functions/` directory contains the trusted backend for listing moderation,
account deletion, and inquiry push notifications. From an authenticated Firebase
CLI session, select the production project and run `npm install` then `npm run
build` in `functions/`, followed by `firebase deploy`. Assign moderation staff a
custom Auth claim of `moderator: true` (or `admin: true`) before they use the
`moderateListing` callable function. Also add the production Android app in
Firebase and place its generated `google-services.json` in `app/`; it is
intentionally excluded from source control.
