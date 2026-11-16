# Spec: Verify Vonage contracts for the MVP

> Issue: [#26](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/26) | Branch: pending assignment
> Status: ready
> Last updated: 2026-11-15

---

## Problem

The team has completed the Voice API learning path, but the MVP still lacks verified, project-specific Voice and Messages API contracts. Implementation could stall or encode assumptions about Spanish speech recognition, webhooks, message states, or test-account capabilities.

## Proposed solution

Research only the Vonage capabilities needed for the approved reservation journey. Complete this same spec with official sources, any safely performed test results, limitations, and questions for the mentor. Use the findings as input to the shared Voice and notification feature specs and their implementation issues.

## Scope

### In scope

- [ ] Verify incoming-call application and number configuration, answer and event webhooks, NCCO responses, speech input, Spanish speech recognition and text-to-speech options, and call termination/error events relevant to the guided flow.
- [ ] Verify what Voice returns for silence, unrecognised speech, and captured values, including call identifiers needed to correlate and deduplicate webhook processing.
- [ ] Verify Messages API SMS authentication, request/acceptance response, message identifier, status callbacks, delivery and failure states, and how callbacks can be correlated to a reservation.
- [ ] Verify webhook methods, payload shapes, signature or authentication options, retry behaviour, response requirements, and provider failure behaviour for the selected flows.
- [ ] Identify prerequisites for a controlled test: account, application, number, permissions, credits, callback reachability, and safe test data.
- [ ] Record each finding as documented, locally tested, or unverified, with an official source, date checked, and implications for this MVP.

### Out of scope

- [ ] Repeating the general Voice learning path or cataloguing all Vonage products.
- [ ] Production code, SDK or dependency additions, application configuration changes, or sending live traffic without mentor authorization.
- [ ] Deciding open restaurant product rules, deployment architecture, or business-duplicate policy.

## Acceptance criteria

- [ ] The Voice and Messages questions above have an answer with an official source, or an explicit unresolved finding with its impact and next verification step.
- [ ] Spanish speech capture and playback viability for the proposed guided journey is assessed, with documented support distinguished from a practical test.
- [ ] A proposed sequence from incoming call through persisted reservation and SMS status is traceable to verified contracts; unsupported or unknown steps are marked clearly.
- [ ] The spec identifies the minimal data needed for call and message correlation, webhook idempotency, and notification status without inventing payload fields.
- [ ] The mentor can identify decisions and blockers before approving implementation slices in the Voice and notification feature specs.
- [ ] No credentials, private keys, real contact values, or unredacted provider payloads appear in committed evidence.

## Business rules and decisions

- Business rules: [RN-02](../docs/business-rules.md#rn-02-alternatives-when-a-service-is-full), [RN-04](../docs/business-rules.md#rn-04-confirmation-and-corrections), [RN-05](../docs/business-rules.md#rn-05-errors-attempts-and-abandonment), [RN-06](../docs/business-rules.md#rn-06-confirmation-messages).
- Related ADRs: none required for research; propose one only if a significant technical choice emerges.
- Open questions: the product policies listed in [business rules](../docs/business-rules.md#open-functional-decisions) remain for the mentor. Research must expose relevant constraints without choosing those policies.

## Technical design

### Backend

Document the candidate request/response and event boundaries for later Spring Boot adapters. Do not implement endpoints in this issue.

### Frontend

Not applicable.

### Data and migrations

Identify provider identifiers and states that later persistence may need. Do not design or apply a migration here.

### Vonage integration

Use current official [Voice documentation](https://developer.vonage.com/en/voice/voice-api/overview) and [Messages documentation](https://developer.vonage.com/en/messages/overview) as starting points. Link the exact official reference for every eventual claim. Record findings in the following form:

| MVP question | Documented behaviour and official source | Tested behaviour and safe evidence | Unverified or decision needed | Impact on implementation |
| --- | --- | --- | --- | --- |
| Incoming call and speech capture | Pending research | Not tested | Pending research | Voice adapter |
| SMS request and status callback | Pending research | Not tested | Pending research | Notification adapter |

## Security and data handling

Keep credentials, test numbers, and private keys in local configuration only. Use fictional examples and redact captured requests. A controlled call or SMS test requires mentor authorization and a consenting team number or Vonage test number; automated checks must not consume credits by default.

## Accessibility and UX

Record any speech-recognition limitations that affect a caller's ability to complete the guided flow. Product fallback choices require mentor approval and must respect the MVP exclusions.

## Testing

### Automated

- [ ] No product test is required for documentation research; record any local contract checks performed without live credits.

### Manual verification

- [ ] Review each contract claim against its linked official source and date.
- [ ] If authorized, record test setup, observed result, and limitations without sensitive values.
- [ ] Walk through the priority MVP scenarios and list unsupported or still unverified provider steps.

## Dependencies and environment

- [ ] Official documentation is available without credentials. Live verification depends on mentor-approved test access and safe callback reachability.
- [ ] The Voice and notification feature specs and their relevant implementation issues must cite the completed findings.

## Notes

This is one living research spec: the assignee fills in this file rather than creating a second research spec. Completing it does not approve an implementation design or an unverified product rule.
