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
4. The caller may propose up to two alternative dates after the initial date. On each date, offer the other service if the requested service cannot accommodate the party; switching services on the same date does not consume an alternative date.
5. If no option is accepted after those two alternative dates, end the call politely without creating a reservation.

## RN-03: Reservation arrival windows

| Service | Reservation arrival window |
| --- | --- |
| Lunch | 13:00–16:00 |
| Dinner | 20:00–23:30 |

- These are reservation arrival windows, not restaurant closing hours. Both endpoints are inclusive: lunch accepts arrival times from 13:00 through 16:00, and dinner accepts arrival times from 20:00 through 23:30. Arrival times use minute precision.
- The stored arrival time identifies the service but does not change capacity within that service.
- A requested time outside both windows is invalid. Explain the valid windows and treat the value as an invalid input attempt for the arrival-time field.
- The fictional restaurant uses the `Europe/Madrid` time zone. A reservation date must be between today and 30 calendar days after today, inclusive, in that time zone. The same window applies to telephone and manual reservations and to alternative dates.
- In the telephone flow, interpret "today" and "tomorrow" using the restaurant's local date. For other requests, obtain an unambiguous calendar date, asking for the year when needed. Ask for a concrete date rather than interpreting weekday phrases such as "next Friday". Read back the complete interpreted date before final confirmation. The feasibility of capturing these expressions by speech remains subject to #26.
- For a reservation on the current local date, the arrival time must be strictly later than the current local time at final validation before persistence. A time that is current or already passed is invalid.

## RN-04: Confirmation and corrections

- Before persistence, read the captured details to the caller and require an explicit affirmative confirmation.
- When the caller requests a correction, ask which single field must change and capture only that value.
- Revalidate service hours and availability whenever date, time, or party size changes.
- Revalidate date and time validity whenever the date or arrival time changes.
- Read the updated summary and request confirmation again.
- Allow at most two correction rounds per call after the first summary. Each round changes one field and counts when a new valid value is applied. Invalid, silent, or unrecognised answers while capturing that value follow the per-field attempt limit in RN-05.
- If the caller requests a third correction round, end the call politely without creating a reservation.
- Do not persist a reservation without final confirmation.

## RN-05: Errors, attempts, and abandonment

- Allow one initial question and up to two retries for each field after silence, an unrecognised response, or an invalid value.
- Treat a past reservation date or a same-day arrival time that is current or already passed as an invalid input for the relevant field.
- End the call politely without creating a reservation after the third failed attempt for that field.
- Do not count a database, backend, or external-service failure as a failed customer-input attempt.
- If the caller hangs up before persistence, do not create a reservation. If the caller hangs up after persistence, retain the reservation.
- The implementation must distinguish recognition, validation, backend, and external-provider errors.
- The system must avoid duplicate reservations caused by repeated webhooks or confirmations for the same call. Separate, confirmed calls may create separate reservations with the same name or phone number while capacity remains; no business-duplicate restriction applies in the MVP.

## RN-06: Confirmation messages

- For telephone reservations, after the caller's affirmative confirmation, revalidate capacity and persist the reservation before confirming it by voice.
- If final revalidation finds that capacity is no longer available, do not persist or confirm the reservation. Continue with the alternatives in RN-02.
- Request an SMS confirmation through Vonage Messages API only after persistence and only when the confirmed contact number can receive SMS under RN-08. A confirmed reservation without an SMS-capable contact number remains valid; record that no SMS was requested and why.
- A message failure must not delete or cancel a confirmed reservation.
- Persist the notification's known status and failure reason, when available. Distinguish an SMS deliberately not requested from a request or delivery failure.
- Distinguish a request accepted by Vonage from confirmed delivery to the recipient.
- Initial internal states may include `PENDING`, `SENT`, `DELIVERED`, and `FAILED`; final states must match verified provider behaviour.

## RN-07: Staff dashboard and manual reservations

- Show reservations for a selected date, grouped by lunch and dinner and ordered by arrival time.
- Show name, phone number, date, time, party size, and reservation status.
- Show total, occupied, and available capacity for each service, calculated by the backend.
- Support manual refresh; real-time updates are not required.
- Allow manual reservation creation. It must use the same backend validation, capacity calculation, and persistence rules as the telephone channel.
- Staff should read the reservation details back to the customer and obtain confirmation before submitting a manual reservation. The backend cannot verify that this conversation took place; submission by staff is not evidence of the customer's verbal confirmation.
- Manual reservations follow the same SMS eligibility and post-persistence notification policy as telephone reservations, after the telephone journey is implemented and validated. This does not expand the current manual endpoint or form issues without a reviewed scope change or follow-up issue.
- The MVP does not require login or authentication. It uses fictional data in a controlled demo environment.
- Modification and cancellation of confirmed reservations are post-MVP priorities.

## RN-08: Contact phone and SMS eligibility

- Every reservation requires a confirmed contact phone number. For the MVP demo, accept Spanish contact numbers only; exact formatting and normalization are implementation details to verify before use.
- During a telephone call, ask the caller for the contact number even if caller ID is available. A hidden, missing, or unusable caller ID must not prevent the caller from supplying a number. Do not treat caller ID alone as the confirmed contact number.
- Ask whether the supplied contact number can receive SMS. Do not infer this from caller ID or from the device used to place the call. The customer may provide a different contact number.
- An invalid or unintelligible contact number follows the per-field attempt limit in RN-05. Do not create a reservation without a confirmed contact number.
- Read back the contact number with the other reservation details before final confirmation. A corrected number follows RN-04.
- A confirmed reservation is valid whether or not its contact number can receive SMS. If the customer says it cannot, do not request an SMS and record that reason. If it can, apply RN-06 after persistence; a send or delivery failure does not affect the reservation.
- Do not promise that an SMS has been delivered when confirming the reservation by voice.
- For a manual reservation, staff record whether the contact number can receive SMS. Apply the same post-persistence policy when manual notifications are implemented; the telephone journey has priority for development and demonstration.

## Priority acceptance scenarios

1. A valid call with a contact number that can receive SMS leads to confirmation, persistence, an SMS request, and dashboard visibility.
2. A full dinner service leads to an offer for lunch on the same date, then a request for another date if necessary.
3. An arrival time outside the permitted windows requires a new valid time.
4. Correcting date or party size triggers revalidation and a new confirmation summary.
5. Three invalid attempts for one field end the call without creating a reservation.
6. Two concurrent calls for the last places do not oversell capacity.
7. A persistence failure never results in a false reservation confirmation.
8. A Messages API failure keeps the reservation and records the notification failure.
9. A repeated webhook or confirmation does not create a duplicate reservation.
10. A manually created reservation follows the same capacity validation as a telephone reservation.
11. A reservation with a contact number that cannot receive SMS remains confirmed, with a recorded reason for not requesting a message.
12. Two separately confirmed calls may create reservations with the same contact number while capacity remains; a repeated confirmation for one call creates only one reservation.

## Open functional decisions

The following items are intentionally open and must not be implemented from assumptions:

- Provider-dependent details of Voice speech capture and Messages status mapping remain subject to the mentor-reviewed findings of #26; do not infer provider contracts from these product rules.
- The manual SMS implementation follows the telephone-first priority and needs a reviewed issue scope or follow-up issue after #26; its product policy is approved in RN-06 to RN-08.
- The exact behaviour of post-MVP cancellation and modification, including capacity release.
