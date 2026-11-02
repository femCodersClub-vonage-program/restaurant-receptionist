# Vonage integration

Restaurant Receptionist uses Vonage Voice API for reservation calls and Vonage Messages API for SMS confirmations. This document records the intended product role of each integration. It is not an API-contract reference.

## Voice API

The MVP uses Voice API to support a guided telephone reservation journey:

1. Receive an incoming call.
2. Ask for and capture the reservation details.
3. Report validation or capacity outcomes.
4. Read the final summary and obtain explicit confirmation.
5. Confirm a persisted reservation by voice.

Spanish speech recognition, text-to-speech behaviour, webhook flow, authentication, payload formats, error handling, caller-number availability, test numbers, and credit requirements must be verified against current official Vonage documentation before implementation.

## Messages API

The MVP uses Messages API to request an SMS reservation confirmation after the reservation has been persisted.

- SMS is the agreed MVP channel.
- RCS is out of scope for the MVP.
- A request accepted by Vonage is not evidence that the message reached the recipient.
- The backend must record known delivery states and errors after their behaviour and webhooks have been verified.
- A failed notification must not cancel a confirmed reservation.

## Integration principles

- The backend owns all Vonage credentials and API requests.
- No secret, API key, private key, test number configuration, or real personal data may be committed.
- Controlled integration or demo testing may use a consenting team member's phone number or a Vonage test number. Keep those numbers in local configuration only; never include them in repository files, logs, seed data, or public screenshots.
- Automated tests must use test doubles or verified local test mechanisms rather than consume real credits by default.
- Documented behaviour, tested behaviour, and unverified hypotheses must remain clearly distinguished.

## Outside the MVP

- Verify API and Silent Authentication.
- RCS and RCS-to-SMS fallback.
- Conversational AI as a replacement for the guided flow.
