# Spec: Verify Vonage contracts for the MVP

> Issue: [#26](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/26) | Branch: `docs/26-verify-voice-messages-contracts`
> Status: done
> Last updated: 2026-11-18

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
- [ ] Deciding restaurant product rules or deployment architecture. The mentor has resolved the MVP booking horizon as unrestricted for future dates, along with the contact-number policy, bounded call flow, and absence of a business-duplicate restriction; this issue verifies provider contracts.

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

This is an initial documentation-only review, checked on 2026-11-17. No Vonage account was accessed and no local integration, call, SMS, or callback test was run. "Not tested" is not evidence of provider behaviour.

| MVP question | Documented behaviour and official source (checked 2026-11-17) | Locally tested and safe evidence | Unverified or decision needed | Impact on implementation |
| --- | --- | --- | --- | --- |
| Incoming call, application, and number | An inbound call to a Vonage number linked to a Voice application triggers its configured `answer_url`. The answer webhook defaults to `GET` and can be configured as `POST`; the application must return an NCCO JSON array. The webhook documents `from`, `to`, `uuid`, and `conversation_uuid`. [Call flow](https://developer.vonage.com/en/voice/voice-api/concepts/call-flow), [Voice webhooks](https://developer.vonage.com/en/voice/voice-api/webhook-reference), [Voice numbers](https://developer.vonage.com/en/voice/voice-api/concepts/numbers), [IVR setup](https://developer.vonage.com/en/voice/voice-api/guides/interactive-voice-response). | Not tested. | Account-specific access to a Spain-capable Voice number, its limitations, and caller-ID presentation have not been checked. RN-08 governs the product policy: collect and confirm the contact number separately; do not rely on caller ID as the reservation or SMS destination. **Next:** mentor confirms a safe test account/number. | A Voice application, an eligible Voice number linked to it, and an answer endpoint are prerequisites. Do not assume caller ID is always usable as the reservation SMS destination. |
| Guided NCCO flow and speech capture | NCCOs are ordered JSON actions. `input` can accept speech, sends a callback to its `eventUrl`, and expects a new NCCO in response to continue the call. The input callback defaults to `POST`; the documented speech result contains recognized text and confidence candidates, plus a timeout reason. [Call flow](https://developer.vonage.com/en/voice/voice-api/concepts/call-flow), [NCCO input](https://developer.vonage.com/en/voice/voice-api/ncco-reference#input), [ASR](https://developer.vonage.com/en/voice/voice-api/concepts/asr). | Not tested. | Practical recognition reliability, result ordering, and the precise no-match/error response for the selected provider are unverified. **Next:** if authorized, exercise representative fictional reservation answers and record only redacted observations. | The backend must treat the callback as input to application validation, not as a validated reservation. The three-attempt rule remains application behavior. |
| Spanish speech capture | Vonage's ASR documentation lists Spanish (Spain), `es-ES`, in its legacy supported-language table. When a specific ASR provider and provider options are used, supported language configuration depends on that provider; the documentation names Google and Deepgram. ASR is chargeable. [ASR language and provider options](https://developer.vonage.com/en/voice/voice-api/concepts/asr), [NCCO speech settings](https://developer.vonage.com/en/voice/voice-api/ncco-reference#speech-recognition-settings). | Not tested. | The selected provider/configuration has not been chosen or validated, and listing `es-ES` is not evidence of useful recognition accuracy for names, dates, times, or party sizes. **Next:** mentor-approved practical test using fictional prompts; confirm provider-specific configuration before implementation. | Spanish capture is documented at the language-list level, but practical viability for this guided journey remains unverified. |
| Spanish voice playback | The `talk` action supports an explicit BCP-47 `language`; its default is `en-US`. The Standard/Premium voice table lists `es-ES` styles, including style 0. Premier voices use provider-specific options instead. [Text to Speech](https://developer.vonage.com/en/voice/voice-api/concepts/text-to-speech), [NCCO talk](https://developer.vonage.com/en/voice/voice-api/ncco-reference#talk). | Not tested. | Pronunciation and intelligibility of Spanish names, dates, times, and confirmation prompts have not been assessed in a call. Premier voice configuration and pricing are not needed to establish basic documented `es-ES` support. **Next:** test representative fictional prompts only if authorized. | Set the language explicitly in a later implementation; do not infer playback quality from the language table. |
| Silence, unrecognized speech, and call events | Speech input settings document `startTimeout`, `endOnSilence`, and `maxDuration`. Input callbacks may report `start_timeout`, `end_on_silence_timeout`, or `max_duration`; speech results can include recognized text/confidence or an error. Event callbacks include call statuses such as `answered`, `completed`, `failed`, and `rejected`; completed events can identify whether the user or platform disconnected the call, while error callbacks include a reason. [NCCO speech settings](https://developer.vonage.com/en/voice/voice-api/ncco-reference#speech-recognition-settings), [Voice input webhook](https://developer.vonage.com/en/voice/voice-api/webhook-reference#input), [Voice events and errors](https://developer.vonage.com/en/voice/voice-api/webhook-reference#event-webhook). | Not tested. | The practical payload for silence and unrecognized Spanish input, and whether retries behave as expected across the MVP prompts, remain unverified. The provider does not decide the MVP's three-attempt policy. **Next:** capture redacted observations during an authorized test; retain the product rule from RN-05. | Map provider input outcomes to application-level recognition/validation errors. A provider timeout must not be confused with a persistence or provider-service failure. |
| Voice callback correlation, authentication, methods, and retries | Answer callbacks default to `GET` and can use `POST`; event callbacks default to `POST` and can use `GET`; input callbacks default to `POST`. The answer callback returns an NCCO; an input event also expects an NCCO to continue the call. Call callbacks expose `uuid`, `conversation_uuid`, status, and timestamp where documented. Signed webhooks use a JWT in `Authorization`; signing is documented as enabled by default, with a caveat that older Voice applications may have it off. The Voice reference describes retry on an unacknowledged event or selected responses (`429`, `502`, `503`, `504`) and fallback handling for answer/NCCO callbacks. [Voice webhooks](https://developer.vonage.com/en/voice/voice-api/webhook-reference), [NCCO input](https://developer.vonage.com/en/voice/voice-api/ncco-reference#input), [Signed webhooks](https://developer.vonage.com/en/getting-started/concepts/webhooks#decoding-signed-webhooks). | Not tested. | The Voice fallback section specifies two attempts for answer/event callbacks and two fallback attempts for listed connection failures, while the general webhook guide says one retry for unreachable or timed-out endpoints and broader non-2xx retry behavior. Exact behavior is unresolved. The reviewed references do not define exactly-once delivery or a stable callback-event idempotency key. **Next:** confirm retry details with Vonage or an authorized controlled callback test. | Return the documented response type for each callback and acknowledge event notifications. Persist a mapping from the provider call identifier to application state and make processing idempotent. Do not treat the signed-JWT `jti` as a retry-stable event key without evidence. |
| Messages SMS authentication and submission | Messages supports either Basic authentication (API key/secret, account-level) or JWT (application-level); Vonage recommends JWT for most production use cases. The SMS send operation returns HTTP `202 Accepted` and a `message_uuid`. Its optional `client_ref` is documented as being included in every status callback. [Messages authentication](https://developer.vonage.com/en/messages/technical-details#authentication), [Messages API reference](https://developer.vonage.com/en/api/messages#SendMessage). | Not tested. | The project has not selected an authentication mode or verified account-level SMS permissions, sender eligibility, or account-specific limits. No idempotency-key contract for resubmitting an SMS request was identified in the reviewed reference. **Next:** confirm account prerequisites with the mentor; do not retry an ambiguous send as though it were guaranteed idempotent. | Keep credentials server-side. Request SMS only after persistence and when RN-08 confirms the contact number can receive SMS; if capability is negative or unknown, do not request a message and record the reason. Treat `202` and `message_uuid` as submission acceptance/identification, not proof of delivery. |
| Messages status callbacks and correlation | Status callbacks are `POST` requests. The API reference lists `submitted`, `delivered`, `rejected`, and `undeliverable`; `submitted` means passed to a messaging provider, not delivered. The callback includes `message_uuid`, status, timestamp, and, when supplied, `client_ref`. The rendered reference says to acknowledge with `200` or `204`; its OpenAPI schema lists `200`, and the webhook guide examples recommend `200`. SMS final status can take up to 72 hours. [Messages status callbacks](https://developer.vonage.com/en/messages/concepts/messages-status-callbacks), [Messages API webhook reference](https://developer.vonage.com/en/api/messages#message-status), [Messages OpenAPI schema](https://developer.vonage.com/api/v1/developer/api/file/messages?format=json&vendorId=vonage), [Webhook configuration](https://developer.vonage.com/en/messages/code-snippets/configure-webhooks). | Not tested. | The documentation differs on accepted success responses (`200`/`204` in the rendered reference versus `200` in the OpenAPI schema). The production SMS callback retry schedule, ordering, and duplicate-delivery behavior are also not specified in the reviewed references. **Next:** confirm the accepted response and retry behavior with Vonage or an authorized controlled callback test. | Use `200` as the conservative documented acknowledgement until the discrepancy is resolved. Persist provider message ID and known status separately from reservation confirmation; an accepted request or `submitted` state is not delivered. |
| Webhook safety and controlled-test prerequisites | Vonage requires reachable webhook URLs; its guides describe HTTPS and a secure tunnel for local development. The IVR guide requires a Voice-enabled application, a Voice-capable rented number linked to it, and configured answer/event URLs. ASR is chargeable. The Messages Sandbox currently lists WhatsApp, Viber, Facebook Messenger, and Instagram, not SMS. [Webhook guide](https://developer.vonage.com/en/getting-started/concepts/webhooks), [IVR prerequisites](https://developer.vonage.com/en/voice/voice-api/guides/interactive-voice-response), [Messages Sandbox](https://developer.vonage.com/en/messages/concepts/messages-api-sandbox), [ASR pricing note](https://developer.vonage.com/en/voice/voice-api/concepts/asr). | No account, call, SMS, or callback test was run. | Whether the team's test account has Voice/SMS access, an eligible number, sufficient credits, required permissions, and a safe callback endpoint is not verifiable from public docs. The documented Messages Sandbox does not list SMS, so it is not established as an SMS test path. **Next:** mentor confirms approved test access, a consenting team test contact or Vonage test number, and callback reachability before any live operation. | No live traffic or credential use in this issue without mentor authorization. Automated checks must not consume credits by default. |

#### Contract trace through the priority MVP journey

1. **Valid reservation:** the answer webhook returns an NCCO; speech input produces a callback; the application validates and confirms, then persists. It requests SMS only when the caller confirms that the contact number can receive SMS under RN-08; if the answer is no or unknown, it skips the request and records why. For an eligible request, Messages returns `202` with a `message_uuid`; a later status callback reports the message state. The provider contracts for these boundaries are documented, but the end-to-end journey has not been tested.
2. **Silence or unrecognized speech:** Voice documents timeouts and speech callback fields. The application must apply RN-05 retries; the exact practical no-match behavior for Spanish remains unverified.
3. **Full service or correction:** the application may return a new NCCO after an input callback. Alternatives, correction rules, and capacity validation are application behavior, not provider decisions.
4. **Capacity race or persistence failure:** final capacity revalidation and atomic persistence are application responsibilities under RN-01/RN-06; Vonage does not provide that guarantee. A backend failure must not result in a false voice confirmation.
5. **Hangup:** Voice documents call completion and whether the user or platform disconnected. RN-05/RN-06 govern whether a reservation is created or retained; integration behavior around persistence failures has not been tested.
6. **SMS failure or delayed delivery:** the reservation remains persisted under RN-06. `202`/`submitted` is not delivery; later callbacks may report a final state, potentially within 72 hours for SMS.
7. **Repeated webhook or confirmation:** Voice/Messages callbacks can be retried, but the reviewed documentation does not establish exactly-once delivery or a stable event idempotency key. Application processing must be idempotent; the required local key/transition rules remain for implementation design.

These are provider-contract boundaries, not an implementation design. Spanish recognition/playback quality, account eligibility, callback retries, and practical webhook behavior remain unverified until an authorized test or provider clarification.

#### Test prerequisites and mentor blockers

- Confirm mentor authorization before any live call, SMS, or credit-consuming test.
- Confirm an account with the required Voice and Messages SMS access, an eligible Voice number linked to a Voice application, a permitted sender, and sufficient credits for the selected test.
- Provide a safe, reachable HTTPS callback endpoint and consenting team test contact or Vonage test number; do not record contact values in this spec. Keep credentials in local configuration only.
- Confirm whether the selected account can test SMS through a non-production mechanism. The reviewed Messages Sandbox documentation does not list SMS.
- RN-08 resolves the hidden/unavailable caller-ID and SMS-eligibility policy: ask the caller for a confirmed contact number and whether it can receive SMS; do not request a message when the answer is no or unknown. Contract research records provider constraints without reopening these product decisions.
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
