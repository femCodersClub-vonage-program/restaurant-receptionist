# Restaurant Receptionist MVP Specification

## Status

This document defines the initial MVP baseline for a pilot application. The current release is limited to a fictional restaurant and test data; it is not yet intended for production use. It does not prescribe an application architecture or technology stack.

Third-party API contracts, Spanish speech-recognition capability, webhook behaviour, and implementation choices must be verified against current official documentation before development decisions are made.

## Problem

During peak service hours, restaurant staff may be unable to answer incoming calls. Each missed call can become a missed reservation and lost revenue.

The same problem can occur before opening hours, when staff may be preparing the restaurant, cleaning, preparing food, or completing operational tasks and cannot answer the phone.

## Goal

Demonstrate a complete telephone reservation journey for one fictional restaurant:

1. Receive a reservation call.
2. Collect the customer's name, phone number, date, arrival time, and party size.
3. Validate the requested time and capacity.
4. Obtain explicit confirmation from the customer.
5. Persist the reservation without duplicates or overselling capacity.
6. Request an RCS or SMS confirmation.
7. Display the reservation and capacity in a basic staff dashboard.

## Scope

### In scope for the MVP

- Guided IVR flow using Vonage Voice API.
- Speech-recognition-based data capture, subject to validation of Spanish support and reliability.
- One fictional restaurant.
- Lunch and dinner services with independent capacity measured in guests, not number of reservations.
- Explicit confirmation before persistence.
- A maximum of three attempts for each captured field.
- Guided correction of one captured field at a time.
- Capacity revalidation and atomic persistence for concurrent reservation attempts.
- Idempotent handling of repeated webhooks or confirmations.
- Persistent notification status and error recording.
- A dashboard that shows reservations by date and service, ordered by arrival time, plus calculated capacity.
- Tests for the critical business rules.

### Out of scope for the MVP

- Conversational AI agent.
- Table allocation, table rotation, or duration management.
- Multiple restaurants.
- Automatic search for alternative dates.
- Production use, real customer data, or public dashboard access.
- Login and authentication.
- Reservation cancellation or modification.
- Manual reservation creation, retries, RCS-to-SMS fallback, DTMF input, and advanced natural-language date handling.

## Business rules

### Reservation data

Each reservation captures:

- Customer first and last name.
- Phone number.
- Reservation date.
- Arrival time.
- Party size.

### Service hours

The initial fictional restaurant schedule is:

| Service | Reservation arrival window |
| --- | --- |
| Lunch | 13:00–16:00 |
| Dinner | 20:00–23:30 |

These windows determine the service and are not restaurant closing hours. The inclusivity of the endpoints must be confirmed during implementation.

### Capacity

- Each date has independent lunch and dinner capacity.
- Capacity is calculated in guests, not reservations.
- Available places equal service capacity minus the sum of guests in confirmed reservations for that date and service.
- Arrival time does not change capacity within a service.
- The system must revalidate capacity and persist the reservation atomically before confirming it to the customer.
- A reservation must not be confirmed when the requested party exceeds available capacity.

### Alternatives

When the requested service is full for the requested party size:

1. Offer the other service on the same date.
2. If the customer declines or it is also full, ask for another date.

The MVP does not automatically search for available dates or offer another time within the same full service.

### Confirmation and corrections

- Read the captured details to the customer and require an explicit affirmative confirmation before persisting a reservation.
- When a customer requests a correction, ask which single field must change and capture only that value.
- Revalidate service hours and capacity whenever date, time, or party size changes.
- Read the updated summary and request confirmation again.

### Errors and abandonment

- Allow one initial question plus up to two retries for each field after silence, an unrecognised response, or an invalid value.
- End the call politely without creating a reservation after the third failed attempt for that field.
- Do not count a database or external-service failure as a failed customer input attempt.
- If the caller hangs up before persistence, do not create a reservation. If the caller hangs up after persistence, keep the reservation.

### Notifications

- After persistence, request a reservation confirmation through Vonage Messages API.
- A notification failure must not delete or cancel the reservation.
- Persist the notification's known state and any failure reason.
- Distinguish a request accepted by Vonage from confirmed delivery to the recipient.
- Initial internal states may include `PENDING`, `SENT`, `DELIVERED`, and `FAILED`; final states must match verified provider behaviour.

### Dashboard

The dashboard must:

- Show reservations for a selected date, grouped by service and ordered by arrival time.
- Show name, phone number, date, time, party size, and reservation status.
- Show total, occupied, and available capacity for each service.
- Calculate capacity on the backend.
- Support manual refresh; real-time updates are not required.

## Priority validation scenarios

1. A valid call leads to confirmation, persistence, a notification request, and dashboard visibility.
2. A full dinner service leads to an offer for lunch on the same date, then a request for another date if necessary.
3. An arrival time outside the permitted windows requires a new valid time.
4. Correcting date or party size triggers revalidation and a new confirmation summary.
5. Three invalid attempts for one field end the call without creating a reservation.
6. Two concurrent calls for the last places do not oversell capacity.
7. A persistence failure never results in a false reservation confirmation.
8. A Messages API failure keeps the reservation and records the notification failure.
9. A repeated webhook or confirmation does not create a duplicate reservation.

## Decisions still required

- Technology stack and repository structure.
- Official Vonage Voice and Messages API contracts, credentials, test numbers, credits, webhooks, and Spanish speech-recognition viability.
- Interpretation of relative dates, past dates, maximum booking horizon, and a same-day weekday reference.
- Exact endpoint inclusivity for 13:00, 16:00, 20:00, and 23:30.
- Data model, integration tests, deployment approach, and post-MVP scope.
