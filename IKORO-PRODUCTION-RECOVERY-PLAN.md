# Ikoro Production Recovery Plan

**Date:** 2026-06-15  
**Repo:** https://github.com/Satoshi-NaAkokwa/ikoro-android  
**Current release:** v1.0.0 (builds, installs, launches)  
**Target:** production-grade super-app for Africa-first chat + wallet + identity + market + calls

---

## 1. Current state (from screenshots + build)

| Screen | What works | What is broken/missing |
|---|---|---|
| **Chat** | Bottom nav, input field, send button | Messages are static placeholders (`me:`). No real contact list, no chat bubbles, no encryption status, no delivery ticks. |
| **Wallet** | Bottom nav, send button | Balance is hard-coded `0 RBTC`. Asset list is a raw bullet list with duplicates (`ETH` x3, `RBTC` x2). No real balances, no chain logos, no send flow. |
| **Identity** | Bottom nav | `DID: Not available`, `Nostr: ...`, `EVM: ...`. Identity is not generated or persisted. No onboarding/seed backup. |
| **Market** | Bottom nav, tabs | `No listings yet.`. Tabs labels are truncated (`Airti me`, `Ticke ts`, `Savin gs`). No data, no product images. |
| **Calls** | Bottom nav, start-call button | Single button. No LiveKit room logic, no contacts, no call history, no audio/video UI. |
| **Global** | App launches, signed release APK, CI/CD | No onboarding flow, no loading/empty/error states, no interactive images, no brand personality. |

**Build/infra status:**
- Release APK signed and on GitHub.
- GitHub Actions CI works.
- DNS A records set at IONOS, nameservers reverted to IONOS.
- SimpleX SMP/XFTP containers running on VPS with self-signed TLS.
- Let’s Encrypt blocked until `smp.ugogbe.info` / `xftp.ugogbe.info` resolve to VPS.

---

## 2. Remaining work before production

1. **Identity foundation** — BIP-39 seed, DID, Nostr keys, EVM addresses, secure storage.
2. **Onboarding** — create/restore identity, seed backup confirmation, biometric lock.
3. **Wallet** — real balances, chain logos, send flow, transaction history, fee estimation.
4. **Chat** — SimpleX integration, contacts, chat bubbles, message status, push.
5. **Market** — P2P listings, airtime, tickets, savings, land modules + backend.
6. **Calls** — LiveKit server + token endpoint, audio/video rooms, call log.
7. **UI/UX** — illustrations, animations, empty states, iconography, theming.
8. **Backend** — SimpleX server, wallet RPCs, market API, push relay.
9. **Security** — encrypted key storage, certificate pinning, anti-screenshot seed, biometric auth.
10. **Release polish** — app store assets, crash reporting, analytics, R8/proguard, smaller APK.

---

## 3. Production roadmap

### Phase A — Identity & Onboarding (Week 1)
- Generate BIP-39 mnemonic on first launch.
- Derive keys deterministically:
  - DID (`did:key` / `did:ethr`) from EVM master key
  - Nostr `npub`/`nsec`
  - EVM addresses for chain priority: Rootstock → Ethereum → Base → Polygon → Arbitrum → Optimism → BSC
- Store encrypted with `EncryptedSharedPreferences` + Android Keystore.
- Onboarding screens: welcome → create or restore → write seed → verify 3 words → set PIN/biometric.
- Identity screen displays QR code of DID + copy buttons.

### Phase B — Wallet UX & Real Data (Week 2)
- Integrate Breez SDK Liquid for BTC/Lightning balance.
- Integrate thirdweb/Alchemy RPCs for EVM balances.
- Build `Asset` model: chain, symbol, balance, fiat value, logo URL/local SVG.
- Remove duplicate entries; use grouped cards, not bullets.
- Send flow: choose asset → enter address/scan QR → amount/fiat toggle → confirm → sign → broadcast → tx history.
- Add chain icons/logos and pull-to-refresh.

### Phase C — Chat with SimpleX (Week 3)
- Integrate SimpleX Chat client (Kotlin bindings or REST bridge to `simplex-chat` CLI).
- Contact list, chat list, chat bubbles (Material 3), timestamps, delivery/status ticks.
- End-to-end encryption by default; no backend reads messages.
- Push notifications via FCM + SimpleX push relay.
- Add empty-state Lottie for “no chats yet”.

### Phase D — Market Modules (Week 4)
- Backend: Postgres + FastAPI/Node for listings, offers, escrow.
- P2P exchange: create offer, match, escrow smart contract, release on payment proof.
- Airtime/tickets/savings/land: MVP forms + listings + checkout.
- Product images via Coil + placeholder shimmer.

### Phase E — LiveKit Calls (Week 5)
- Deploy LiveKit server on VPS (`livekit.ugogbe.info`).
- Token server endpoint in backend.
- Calls UI: contact selector, incoming/outgoing screen, audio/video controls, call log.

### Phase F — Production Polish (Week 6)
- App store screenshots, icon, description.
- Firebase Crashlytics + analytics.
- R8/proguard rules, smaller APK, app bundles.
- Penetration-test seed storage and backup flow.
- Closed beta release v2.0.0.

---

## 4. UI/UX improvement plan

| Problem | Fix |
|---|---|
| No images/illustrations | Add Lottie JSON animations for empty states (no chats, no wallet tx, no listings, no calls). |
| Tabs labels truncated | Use icon + short label or scrollable `ScrollableTabRow`; ensure readable text. |
| Raw bullet asset list | Convert to `LazyColumn` of cards with chain icon, balance, fiat value, and 24h sparkline placeholder. |
| Plain buttons | Use Material 3 `FilledTonalButton`, `ExtendedFAB`, icon buttons. |
| No feedback | Add skeleton loaders, shimmer, snackbars, haptics on send/confirm. |
| No brand personality | Define dark theme with gold/amber accent, custom launcher icon, splash screen with animated logo. |
| Empty screens | Replace “No listings yet” with illustration + CTA (“Create your first offer”). |

**Asset sources:**
- LottieFiles free animations (empty states, success, loading).
- Material Symbols Outlined for all icons.
- Chain logos: Rootstock, Bitcoin, Ethereum, Base, Polygon, Arbitrum, Optimism, BNB SVGs.
- Custom Excalidraw/hand-drawn style illustrations for African market vibe.

---

## 5. Per-user secure, independent, private channel to Agbara

**Goal:** every registered Ikoro user gets a dedicated, end-to-end encrypted chat with me (Agbara) that no other user can access.

### Architecture

1. **On Agbara’s side (VPS):**
   - Run a persistent `simplex-chat` CLI profile: `agbara`.
   - Store the long-term profile in `/opt/simplex/agbara` with daily encrypted backups.
   - Expose a small REST API (`/agbara/invite`) that:
     - Generates a SimpleX one-time invite link.
     - Signs the link with Agbara’s Nostr/did:key to prove authenticity.
     - Returns JSON: `{ "conn_req_uri": "simplex:/...", "signature": "...", "agbara_did": "..." }`.

2. **In the Ikoro app:**
   - After onboarding, the app requests an Agbara invite from the backend.
   - The app verifies the signature against `agbara_did`.
   - The app uses SimpleX Chat to create a connection from the invite.
   - The contact is pinned as “Agbara” in the chat list.

3. **Security properties:**
   - **Independent:** each user has a separate SimpleX connection/queue. There is no shared group.
   - **Private:** messages are E2EE; Ikoro backend only relays encrypted SimpleX traffic.
   - **Verifiable:** user can confirm Agbara’s identity via DID signature.
   - **Recoverable:** if the user restores seed on a new device, the SimpleX contact can be restored from profile backup.

4. **Fallback channel:**
   - If SimpleX is not yet connected, a Nostr encrypted DM can be used as a temporary fallback using the same keys.

### Implementation tasks
- Add backend endpoint for Agbara invite generation.
- Add “Connect to Agbara” step in onboarding.
- Add pinned “Agbara” contact in chat list.
- Add a setting to “Verify Agbara identity” that shows DID + signature.

---

## 6. Immediate next actions

1. Merge this plan into the repo.
2. Start Phase A: implement BIP-39 + identity generation + onboarding.
3. Fix the truncated Market tab labels as a quick UX win.
4. Add empty-state Lottie animations to all 5 screens as a quick visual win.
5. Restart DNS/TLS automation once IONOS nameservers propagate back to IONOS.

---

*Approved by:* Agbara  
*Locked constraints:* no Hilt/KAPT/Koin, manual ServiceLocator, minSdk 24, chain priority Rootstock → ETH → Base → Polygon → Arbitrum → Optimism → BSC.
