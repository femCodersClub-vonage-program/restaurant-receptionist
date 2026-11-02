# Business rules

This document records the approved functional rules for the Restaurant Receptionist MVP. It is the detailed product reference alongside the [MVP specification](mvp-specification.md).

## RN-01: Capacity by service

- Each date has independent lunch and dinner services, each with a fixed maximum capacity measured in guests, not reservations.
- The initial demo capacity is 30 guests for lunch and 30 guests for dinner.
- Available capacity equals service capacity minus the total guests in confirmed reservations for that date and service.
- Party size must be a whole number of at least one guest. A party that exceeds the service's total capacity cannot be accommodated.
- Arrival time identifies the service but does not change its capacity.
- The system must revalidate capacity and persist a reservation atomically when concurrent calls compete for the remaining places.
- A reservation must not be confirmed when the party size exceeds available capacity.
- The MVP does not include table allocation, table rotation, stay duration, or a separate maximum party size.

## RN-02: Alternatives when a service is full

1. When the requested service cannot accommodate the party, offer the other service on the same date.
2. If the caller declines or that service is also full, ask for another date.
3. Do not automatically search for dates or offer another arrival time within a full service.

## RN-03: Reservation arrival windows

| Service | Reservation arrival window |
| --- | --- |
| Lunch | 13:00–16:00 |
| Dinner | 20:00–23:30 |

- These are reservation arrival windows, not restaurant closing hours.
- The stored arrival time identifies the service but does not change capacity within that service.
- A requested time outside both windows is invalid. Explain the valid windows and treat the value as an invalid input attempt for the arrival-time field.
- Whether the endpoints are inclusive remains to be confirmed during implementation.

## RN-04: Confirmation and corrections

- Before persistence, read the captured details to the caller and require an explicit affirmative confirmation.
- When the caller requests a correction, ask which single field must change and capture only that value.
- Revalidate service hours and availability whenever date, time, or party size changes.
- Read the updated summary and request confirmation again.
- Do not persist a reservation without final confirmation.

## RN-05: Errors, attempts, and abandonment

- Allow one initial question and up to two retries for each field after silence, an unrecognised response, or an invalid value.
- End the call politely without creating a reservation after the third failed attempt for that field.
- Do not count a database, backend, or external-service failure as a failed customer-input attempt.
- If the caller hangs up before persistence, do not create a reservation. If the caller hangs up after persistence, retain the reservation.
- The implementation must distinguish recognition, validation, backend, and external-provider errors.
- The system must avoid duplicates caused by repeated webhooks or confirmations. The separate handling of business duplicates remains to be decided.

## RN-06: Confirmation messages

- After affirmative confirmation, revalidate capacity and persist the reservation before confirming it by voice.
- If final revalidation finds that capacity is no longer available, do not persist or confirm the reservation. Continue with the alternatives in RN-02.
- Request an SMS confirmation through Vonage Messages API only after persistence.
- A message failure must not delete or cancel a confirmed reservation.
- Persist the notification's known status and failure reason, when available.
- Distinguish a request accepted by Vonage from confirmed delivery to the recipient.
- Initial internal states may include `PENDING`, `SENT`, `DELIVERED`, and `FAILED`; final states must match verified provider behaviour.

## RN-07: Staff dashboard and manual reservations

- Show reservations for a selected date, grouped by lunch and dinner and ordered by arrival time.
- Show name, phone number, date, time, party size, and reservation status.
- Show total, occupied, and available capacity for each service, calculated by the backend.
- Support manual refresh; real-time updates are not required.
- Allow manual reservation creation. It must use the same backend validation, capacity calculation, and persistence rules as the telephone channel.
- The MVP does not require login or authentication. It uses fictional data in a controlled demo environment.
- Modification and cancellation of confirmed reservations are post-MVP priorities.

## Priority acceptance scenarios

1. A valid call leads to confirmation, persistence, an SMS request, and dashboard visibility.
2. A full dinner service leads to an offer for lunch on the same date, then a request for another date if necessary.
3. An arrival time outside the permitted windows requires a new valid time.
4. Correcting date or party size triggers revalidation and a new confirmation summary.
5. Three invalid attempts for one field end the call without creating a reservation.
6. Two concurrent calls for the last places do not oversell capacity.
7. A persistence failure never results in a false reservation confirmation.
8. A Messages API failure keeps the reservation and records the notification failure.
9. A repeated webhook or confirmation does not create a duplicate reservation.
10. A manually created reservation follows the same capacity validation as a telephone reservation.

## Open functional decisions

The following items are intentionally open and must not be implemented from assumptions:

- Restaurant time zone, past-date handling, same-day times that have already passed, and maximum booking horizon.
- Whether arrival-window endpoints are inclusive.
- Phone-number collection and confirmation policy, including hidden caller IDs and numbers that cannot receive SMS.
- Limits for valid correction rounds and unsuccessful availability-alternative rounds.
- Customer data model and business-duplicate behaviour, which are distinct from technical idempotency for repeated webhooks.
- The exact behaviour of post-MVP cancellation and modification, including capacity release.
