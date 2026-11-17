# Spec: Guided telephone reservation journey

> Related issues: [#35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35), [#36](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/36), [#37](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/37)
> Status: draft; all related issues are planned and blocked pending provider contracts and implementation review
> Last updated: 2026-11-17

---

## Problem

The MVP needs an incoming caller to complete a guided reservation without overselling capacity or creating a reservation before explicit confirmation. The approved business rules describe the journey, but the project-specific Vonage Voice contracts are still being researched in [#26](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/26).

## Proposed solution

Connect a guided Voice flow to the existing reservation services. Capture and validate one field at a time, support bounded retries and one-field corrections, offer the other service on the same date when capacity is insufficient, and persist only after explicit final confirmation and atomic revalidation. Keep call and webhook processing safe against repeated events and abandonment. Fill in provider-specific contracts only from the completed, mentor-reviewed [Vonage research spec](vonage-01-contract-verification.spec.md).

## Delivery map

| Issue | Deliverable | Readiness and dependencies |
| --- | --- | --- |
| [#35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35) | Handle an incoming call, maintain the minimal call session, and deduplicate repeated events. | Planned and blocked by #26 evidence for Voice webhooks, identifiers, authentication, and response behaviour; persistence is undecided. |
| [#36](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/36) | Ask for the required fields, validate them, handle silence/invalid input, three attempts per field, and one-field correction. | Planned and blocked by #26, #35, and mentor decisions that affect capture. |
| [#37](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/37) | Read back details, require affirmative confirmation, call #7 for atomic creation, and use #19 for the same-date alternative when needed. | Planned and blocked by #26, #7, #19, #36, and relevant mentor decisions. |

These published issues define provisional delivery boundaries. Review and refine their contracts and acceptance criteria after #26; do not mark them ready for implementation until the relevant findings and decisions are approved. One issue may be split if its scope proves too large without duplicating this spec.

## Scope

### In scope

- [ ] Handle the approved incoming-call journey using verified Voice API contracts.
- [ ] Capture first and last name, a caller-confirmed Spanish contact number and its SMS eligibility, reservation date, arrival time, and party size through guided prompts. Ask for the contact number even when caller ID is present.
- [ ] Interpret "today" and "tomorrow" using `Europe/Madrid`; request a concrete, unambiguous date for other expressions, asking for the year if necessary. Accept dates through day 30 inclusive and read back the full interpreted date.
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
- [ ] A hidden or incorrect caller ID does not block a reservation when the caller supplies and confirms a valid contact number; a non-SMS-capable contact number does not block confirmation.
- [ ] A repeated webhook or confirmation does not create a second reservation; a hang-up before persistence creates none.
- [ ] Backend or provider failure is distinguished from invalid caller input and never produces a false confirmation.

## Business rules and decisions

- Business rules: [RN-01 to RN-06 and RN-08](../docs/business-rules.md), especially RN-02 to RN-05 and RN-08.
- Related services: [#6](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/6), [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7), and [#19](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/19).
- The mentor has approved the 30-day horizon, bounded date expressions, contact-number and SMS eligibility policy, two correction rounds, two alternative dates, and no business-duplicate restriction. These are separate from provider-contract verification and do not make a blocked slice ready for implementation.
- Open provider questions belong to #26: Spanish speech capture/playback, webhook authentication and retries, input/error payloads, event identifiers, and test prerequisites. Distinguish documented behaviour from local testing.

## Technical design pending research

### Backend and Voice integration

Define the exact answer/event/input webhook routes, methods, payloads, responses, NCCO actions, authentication, correlation keys, and error handling after #26. Retain only the call state required for safe continuation and idempotency; decide whether it needs persistence, its lifetime, and its cleanup policy during slice review. Keep final reservation creation in #7.

### Data and migrations

Assess call-session persistence and uniqueness constraints after the verified identifier and retry behaviour are known. Do not reserve a Flyway version or add fields from unverified provider assumptions.

### Frontend

No dashboard change is included in this feature.

## Security and data handling

Keep credentials and private keys in local configuration. Use fictional caller data in tests and documentation. Do not log full webhook payloads, names, or phone numbers. Verify webhook authenticity using the provider-supported mechanism selected after #26.

## Testing and validation

- [ ] Test the guided state transitions, attempt counts, corrections, alternatives, final confirmation, abandonment, and repeated events with provider doubles.
- [ ] Use PostgreSQL-backed integration tests for any persisted call state, idempotency constraints, and the final handoff to #7.
- [ ] Validate a controlled call manually only after the required account, number, consent, and test access are approved; record what was actually tested.

## Dependencies and notes

This draft is a feature boundary for planning. #26 owns the provider research and updates only its own research spec. The mentor reviews those findings and product decisions before this Voice spec and its linked issues are marked ready for implementation.
