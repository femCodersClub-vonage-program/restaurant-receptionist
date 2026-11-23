# Spec: Guided telephone reservation journey

> Related issues: [#35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35), [#36](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/36), [#37](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/37)
> Status: #35 approved for implementation; see the delivery map for #36 and #37.
> Last updated: 2026-11-23

---

## Problem

The guided journey must preserve capacity and require explicit confirmation before creating a reservation. [#26](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/26) records the provider research.

## Proposed solution

Connect a guided Voice flow to the existing reservation services. Capture and validate one field at a time, support bounded retries and one-field corrections, offer the other service on the same date when capacity is insufficient, and persist only after explicit final confirmation and atomic revalidation. Keep call and webhook processing safe against repeated events and abandonment. Fill in provider-specific contracts only from the completed, mentor-reviewed [Vonage research spec](vonage-01-contract-verification.spec.md).

## Delivery map

| Issue | Deliverable | Readiness and dependencies |
| --- | --- | --- |
| [#56](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/56) | Provide the shared signature verifier for incoming Vonage Voice webhooks. | #35 can use a verifier double during development and must integrate the real verifier before closure. |
| [#35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35) | Handle an incoming call, maintain the minimal call session, and deduplicate repeated events. | Entry and PostgreSQL session design approved. Start with a verifier double and integrate #56 before closure. |
| [#36](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/36) | Ask for the required fields, validate them, handle silence/invalid input, three attempts per field, and one-field correction. | Depends on #35. Agree the validated handoff to #37 with its owner. |
| [#37](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/37) | Read back details, require affirmative confirmation, call #7 for atomic creation, and use #19 for the same-date alternative when needed. | Start after the #35 session model and the validated data and state from #36 are agreed and recorded. Close after integration with the merged #36 flow and verification that a confirmation creates at most one reservation. |

The delivery map distinguishes prerequisites for starting each slice from integration requirements for closing it.

## Scope

### In scope

- [ ] Handle the approved incoming-call journey using verified Voice API contracts.
- [ ] Capture first and last name, a caller-confirmed Spanish contact number and its SMS eligibility, reservation date, arrival time, and party size through guided prompts. Ask for the contact number even when caller ID is present.
- [ ] Interpret "today" and "tomorrow" using `Europe/Madrid`; request a concrete, unambiguous date for other expressions, asking for the year if necessary. Accept today or a future date without a maximum booking horizon and read back the full interpreted date.
- [ ] Validate date, time, service, and capacity using the shared backend rules; do not make the Voice channel a second source of truth.
- [ ] Allow one initial question and up to two retries per field. Permit at most two successful one-field correction rounds after the initial summary, each followed by revalidation and another summary; end without persistence on a third correction request.
- [ ] Offer the other service on the same date when the requested service cannot fit the party; allow at most two caller-proposed alternative dates after the initial date. A same-date service offer does not consume an alternative date.
- [ ] Require explicit affirmative confirmation before calling the #7 confirmed-reservation service. Handle final capacity loss without false confirmation.
- [ ] Avoid duplicate creation from repeated webhooks or confirmations and avoid creation after pre-persistence abandonment.
- [ ] After successful persistence, hand off to the separately specified notification journey without treating SMS failure as reservation failure.

### Out of scope

- [ ] Conversational AI, DTMF, automatic date search, complex natural-language date handling, and post-MVP cancellation or modification.
- [ ] Reimplementing the capacity or transactional persistence logic owned by #6 and #7.
- [ ] SMS request or status-callback implementation, which belong to the [notification journey](notifications-01-persistence.spec.md).
- [ ] Inventing a fallback for unsupported Spanish speech before mentor review of #26 findings. Caller-ID and contact-number policy is approved in RN-08.

## Acceptance scenarios to refine after #26

- [ ] A caller supplies valid details, confirms the readback, and receives an accurate result only after #7 persists one confirmed reservation.
- [ ] A full requested service leads to the RN-02 same-date alternative through #19; final confirmation still revalidates capacity.
- [ ] An invalid field, silence, or unrecognised input consumes only that field's allowed attempts; the third failed attempt ends the call without persistence.
- [ ] A single-field correction updates the summary and revalidates affected rules before another explicit confirmation.
- [ ] Two correction rounds are allowed; a third request ends without persistence. Two alternative dates are allowed after the initial date; no accepted option ends without persistence.
- [ ] A hidden or incorrect caller ID does not block a reservation when the caller supplies and confirms a valid contact number. An unknown or negative SMS-capability answer does not block confirmation or trigger a message; correcting the number requires a new capability answer.
- [ ] A repeated webhook or confirmation does not create a second reservation; a hang-up before persistence creates none.
- [ ] Backend or provider failure is distinguished from invalid caller input and never produces a false confirmation.

## Business rules and decisions

- Business rules: [RN-01 to RN-06 and RN-08](../docs/business-rules.md), especially RN-02 to RN-05 and RN-08.
- Related services: [#6](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/6), [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7), and [#19](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/19).
- The mentor has approved no maximum booking horizon for the MVP, bounded date expressions, contact-number and SMS eligibility policy, two correction rounds, two alternative dates, and no business-duplicate restriction. These are separate from provider-contract verification and do not make a blocked slice ready for implementation.
- Open provider questions belong to #26: Spanish speech capture/playback, webhook authentication and retries, input/error payloads, event identifiers, and test prerequisites. Distinguish documented behaviour from local testing.

## Technical design

### Backend and Voice integration

The [decision in #35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35#issuecomment-7099986230) defines the answer and event contracts. #35 returns the greeting, #36 adds speech input, and #37 uses #7 for final reservation creation.

### Data and migrations

Use PostgreSQL for minimal call-session state with a unique call-leg UUID and migration `V3__create_voice_call_sessions.sql`. Review terminal-session retention and cleanup in the #35 PR.

### Frontend

No dashboard change is included in this feature.

## Security and data handling

Keep credentials and private keys in local configuration. Use fictional caller data in tests and documentation. Do not log full webhook payloads, names, or phone numbers. Verify webhook authenticity using the provider-supported mechanism selected after #26.

## Testing and validation

- [ ] Test the guided state transitions, attempt counts, corrections, alternatives, final confirmation, abandonment, and repeated events with provider doubles.
- [ ] Use PostgreSQL-backed integration tests for any persisted call state, idempotency constraints, and the final handoff to #7.
- [ ] Validate a controlled call manually only after the required account, number, consent, and test access are approved; record what was actually tested.

## Dependencies and notes

The completed #26 research is the provider baseline. See the delivery map for the remaining prerequisites.
