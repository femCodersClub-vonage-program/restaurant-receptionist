# Controlled demo runbook

This guide describes how to run the fictional Restaurant Receptionist MVP demo in a controlled local environment, and how to record what was actually verified. It is written for a teammate who has her own authorized local configuration.

Status: draft. Pending steps include the integrated Voice journey (#35 to #37), the SMS request flow (#33), and the manual reservation form (#29). Notification persistence (#30) and status-callback processing (#34) are merged, but end-to-end demo verification remains pending. Do not treat proposals as approved or pending steps as verified.

Related documents: [MVP specification](../mvp-specification.md), [Business rules](../business-rules.md), [Local development setup](../setup.md), [Vonage integration](../vonage-integration.md), [Vonage contract research](../../specs/vonage-01-contract-verification.spec.md).

## Scope

In scope: the controlled local demo, its fictional data, its reset path, and the evidence recorded in this file.

Out of scope: production deployment, real restaurant operation, real customer data, and live calls or SMS without separate authorization.

## Ownership

The runbook follows the responsibilities in [issue #39](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/39):

| Participant | Demo responsibility |
| --- | --- |
| Inés (isalvama) | Incoming call, Spanish greeting and guided capture; explain what the caller hears during a retry or correction and how the call reaches confirmation. |
| Andrea (androsrivas) | Final confirmation and persistence, the same-date alternative, and notification delivery status; distinguish an accepted request from a delivered SMS. |
| Joana (JoneJoana) | Manual reservation entry, dashboard refresh, and show whether the SMS request for an eligible telephone booking was accepted or failed. |

Shared setup and cleanup are not assigned to a participant by #39. The verification log names the owner for each demo step; shared setup is marked accordingly.

## Evidence rules

- Every command and demo step is marked **verified** or **pending verification**. A step becomes verified only after someone ran it and observed the expected result.
- Evidence is recorded in the [verification log](#verification-log) in this file. Screenshots are optional; if used, they must show fictional data only.
- Evidence queries select identifiers, dates, times, services, and party sizes. Do not publish names, contact numbers, or raw provider payloads.
- Documented provider behaviour is not evidence of provider behaviour. Only an observed call, SMS, or callback counts.

## Prerequisites

### Tools

Install and verify the tools listed in [Local development setup](../setup.md#prerequisites): Git, Java 21, Docker Desktop with Compose, Node.js 22, npm 10. The backend uses Maven Wrapper.

### Vonage access

`vonage-01` records these as the technical prerequisites for a controlled test. The proposed project account and Voice-enabled number for the integrated demo have not yet been confirmed by the mentor.

**Proposal, pending mentor confirmation:** use a project Vonage account and a Voice-enabled number for the integrated demo. Do not treat this as the agreed demo setup until the decision is recorded.

- An account with Voice API access and Messages API SMS access.
- A Voice-capable number linked to a Voice application, and a permitted SMS sender.
- Sufficient credits for the session.
- A publicly reachable HTTPS callback URL for the application's answer, event, and message-status webhooks.
- A consenting test contact number that can receive the SMS. Keep it in local configuration; never write it in this repository, in logs, or in a screenshot.

### Authorization

A live call and a live SMS consume credits and send real traffic. They require authorization from the project mentor for the session. Automated checks and rehearsals must not consume credits.

## Safe configuration locations

| Value | Where it lives | Committed |
| --- | --- | --- |
| Database credentials | root `.env`, copied from `.env.example` | No, `.env` is git-ignored |
| Vonage API key, secret, application ID, private key | your personal tool or application configuration outside the repository | No |
| Consenting test number | your local session or personal configuration | No |
| Public tunnel URL | local session; it changes every time the tunnel restarts | No |

Two rules that matter:

- Credentials belong to the backend and the local environment only. They must never reach the Next.js application or anything the browser receives, and must never be committed.
- Test numbers must not appear in committed files, seed commands, logs, screenshots, or the dashboard. The fictional number `+34600000000` from the approved API example is the only contact value this guide uses.

## Start the demo environment

Run from the repository root. Follow [Local development setup](../setup.md) for the full explanation of each command.

1. Start PostgreSQL and wait until the service is healthy.

   ```powershell
   docker compose config --quiet
   docker compose up -d
   docker compose ps
   ```

   Status: **verified on Windows 11** (see the verification log); macOS verification is **pending**.

2. Load `.env` into the current shell, set the `local` profile, and start the backend.

   **Windows (PowerShell):**

   ```powershell
   Get-Content -LiteralPath '.\.env' |
     Where-Object { $_.Trim() -and -not $_.Trim().StartsWith('#') } |
     ForEach-Object {
       $name, $value = $_ -split '=', 2
       [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), 'Process')
     }
   $env:SPRING_PROFILES_ACTIVE = 'local'
   Set-Location .\backend
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
   ```

   **macOS (Terminal):**

   ```bash
   set -a
   source ./.env
   set +a
   export SPRING_PROFILES_ACTIVE='local'
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

   If port 8080 is already in use, check whether the process is our backend before stopping it: `curl` the read API. A running backend is not a failure. Expected: the backend starts at `http://localhost:8080` and Flyway applies the current migrations, `V1__create_reservations.sql` and `V2__create_reservation_notifications.sql`, without error.

   Status: backend startup and V1 are **verified on Windows 11**; V2 application was not recorded and remains **pending verification**. macOS verification is **pending**.

3. In a second terminal, start the dashboard.

   **Windows (PowerShell):**

   ```powershell
   Set-Location .\frontend
   npm ci
   npm run dev
   ```

   **macOS (Terminal):**

   ```bash
   cd frontend
   npm ci
   npm run dev
   ```

   Expected: `http://localhost:3000/dashboard` shows the staff dashboard. The page opens on today's date in `Europe/Madrid`.

   Status: the API rewrite through `:3000` is **verified** (see the verification log); a clean startup and visual check are **pending**.

4. Live mode only: expose the backend to Vonage with a tunnel.

   ```powershell
   ngrok http 8080
   ```

   Expected: ngrok prints a public HTTPS URL for the webhook configuration. If the URL changes after a restart, update the configured webhooks.

   **Proposal, pending mentor confirmation:** use `ngrok` for the local demo tunnel. Vonage recommends ngrok for local webhook testing in its [Testing with ngrok guide](https://developer.vonage.com/en/getting-started/tools/ngrok#testing-with-ngrok). This documented recommendation does not mean the project's tunnel choice is approved.

   **Pending decision and setup verification.**

## Prepare the demo data

**Proposed setup, pending mentor confirmation:** create the fictional lunch and dinner bookings at demo time with two `POST /api/reservations` calls. The Windows commands have previously returned `201` responses (see the verification log), but that verifies the API behavior only, not approval to use these calls as the standard demo setup. They are not a migration: a Flyway seed would also apply to the test database and could not seed a date that is valid on the demo day.

1. Choose a date at least one day ahead, so the demo does not depend on the current time. The restaurant uses `Europe/Madrid`.

   **Windows (PowerShell):**

   ```powershell
   $madridTimeZone = [TimeZoneInfo]::FindSystemTimeZoneById('Romance Standard Time')
   $madridDate = [TimeZoneInfo]::ConvertTime([DateTimeOffset]::UtcNow, $madridTimeZone).Date
   $demoDate = $madridDate.AddDays(1).ToString('yyyy-MM-dd')
   ```

   **macOS (Terminal):**

   ```bash
   demoDate=$(TZ='Europe/Madrid' date -v+1d +%F)
   ```

   Status: **pending verification** for both date-generation commands.

2. **Proposed setup, pending mentor confirmation:** create one lunch booking and one dinner booking. Replace `DEMO_DATE` with the date you computed.

   **Windows (PowerShell):**

   ```powershell
   $body = @{ customerFirstName='Demo'; customerLastName='Lunch'; phoneNumber='+34600000000'; reservationDate='DEMO_DATE'; arrivalTime='13:30'; partySize=2 } | ConvertTo-Json
   Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/reservations' -ContentType 'application/json' -Body $body
   ```

   ```powershell
   $body = @{ customerFirstName='Demo'; customerLastName='Dinner'; phoneNumber='+34600000000'; reservationDate='DEMO_DATE'; arrivalTime='21:00'; partySize=4 } | ConvertTo-Json
   Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/reservations' -ContentType 'application/json' -Body $body
   ```

   **macOS (Terminal):**

   ```bash
   curl -sS -X POST http://localhost:8080/api/reservations -H 'Content-Type: application/json' -d '{"customerFirstName":"Demo","customerLastName":"Lunch","phoneNumber":"+34600000000","reservationDate":"DEMO_DATE","arrivalTime":"13:30","partySize":2}'
   ```

   ```bash
   curl -sS -X POST http://localhost:8080/api/reservations -H 'Content-Type: application/json' -d '{"customerFirstName":"Demo","customerLastName":"Dinner","phoneNumber":"+34600000000","reservationDate":"DEMO_DATE","arrivalTime":"21:00","partySize":4}'
   ```

   Expected: `201 Created` with an `id` and a `diningSession` (`LUNCH`, then `DINNER`). Record both ids; the reset path uses them.

   Status: **verified on Windows 11** (see the verification log); macOS verification is **pending**.

3. Confirm the read model.

   **Windows (PowerShell):**

   ```powershell
   (Invoke-RestMethod 'http://localhost:8080/api/reservations?date=DEMO_DATE') | ConvertTo-Json -Depth 4
   ```

   **macOS (Terminal):**

   ```bash
   curl -sS 'http://localhost:8080/api/reservations?date=DEMO_DATE'
   ```

   Expected: lunch `occupiedCapacity: 2` with `availableCapacity: 28`, dinner `occupiedCapacity: 4` with `availableCapacity: 26`, both at `totalCapacity: 30`.

   Status: **verified on Windows 11** (see the verification log); macOS verification is **pending**.

## Demonstration sequence

The sequence below is the demo script. Voice and notification interactions (steps 1–3), the dashboard visual check (step 4), the manual form (step 5), and reset (step 6) remain pending where marked. Previously verified backend checks are recorded in the log.

### 1. Confirmed telephone reservation

Owners: **Inés** leads the incoming call and guided capture; **Andrea** leads final confirmation and persistence; **Joana** refreshes the dashboard and shows the SMS request outcome for the eligible booking.

Proposed action: call from the consenting phone and provide fictional reservation details and the consenting contact number. Demonstrate a retry or correction and confirm the readback. Inés and Andrea will complete and verify the call steps and prompts as their flows become available.

Expected: the call follows the approved journey in RN-04, RN-05, and RN-08; the voice confirms only after the reservation is persisted. The dashboard shows the new dinner row after Joana refreshes it. The SMS request outcome is distinct from delivery status. The exact prompts are defined by #36 and #37.

Evidence: the reservation `id`, the dashboard row, and the notification record from step 3. The SMS itself is observed on the phone; do not publish the number or the message content.

Status: **pending verification** (blocked by #37).

### 2. Capacity and same-date alternative

Owners: **Inés** leads the caller's guided flow; **Andrea** leads the same-date alternative and reservation outcome.

Action: fill dinner to capacity with one booking of the remaining places, then call again and ask for dinner for 2 on the same date.

**Windows (PowerShell):**

```powershell
$body = @{ customerFirstName='Demo'; customerLastName='Filler'; phoneNumber='+34600000000'; reservationDate='DEMO_DATE'; arrivalTime='22:00'; partySize=REMAINING } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/reservations' -ContentType 'application/json' -Body $body
```

**macOS (Terminal):**

```bash
curl -sS -X POST http://localhost:8080/api/reservations -H 'Content-Type: application/json' -d '{"customerFirstName":"Demo","customerLastName":"Filler","phoneNumber":"+34600000000","reservationDate":"DEMO_DATE","arrivalTime":"22:00","partySize":REMAINING}'
```

Replace `REMAINING` with the dinner `availableCapacity` reported by the dashboard or the read API.

Expected: the backend rejects a party that does not fit with `409 Conflict` and persists nothing; in the call, dinner is offered as full and lunch on the same date is offered instead (RN-02). Accepting lunch creates a lunch reservation. Declining leads to a request for another date; at most two alternative dates; no accepted option ends the call without a reservation.

Evidence: the `409` response for the rejected booking, the lunch row created by the call, and no new dinner row.

Status: the `409` case is verified (see the log); the voice alternative is **pending verification** (blocked by #37).

### 3. Notification outcome

Owners: **Joana** shows whether the eligible telephone booking's request was accepted, failed, or has an unknown outcome; **Andrea** explains the notification's delivery status and distinguishes acceptance from delivery.

Proposed action: after step 1, check the notification record and, if delivered, the SMS on the consenting phone. In the step 2 call, answer that the number cannot receive SMS.

**Proposed query, pending verification:** replace `POSTGRES_USER` and `POSTGRES_DB` with the values from the root `.env`, and `RESERVATION_ID` with the id from step 1. This query reads only the reservation id, status, and safe failure reason; do not include contact data or raw provider payloads.

**Windows (PowerShell) and macOS (Terminal):**

```text
docker compose exec postgres psql -U POSTGRES_USER -d POSTGRES_DB -c "SELECT reservation_id, status, failure_reason FROM reservation_notifications WHERE reservation_id = 'RESERVATION_ID';"
```

Expected: show whether the request was accepted, failed, or has an unknown outcome (`REQUEST_OUTCOME_UNKNOWN`); acceptance is not delivery. A later callback may report the final status, which can take up to 72 hours. Do not resend an unknown outcome. If SMS capability is negative or unknown, record why no message was requested. A request or delivery failure never cancels the reservation.

Evidence: the notification state and reason, without contact data or raw payloads.

Status: **pending verification** (the request flow in #33 remains open; the callback work in #34 is merged but still needs integrated verification).

### 4. Dashboard visibility

Owner: **Joana**.

Action: open `http://localhost:3000/dashboard`, select the demo date, and press **Refresh**.

Expected: a Lunch section (`13:00–16:00`) and a Dinner section (`20:00–23:30`), each with total, occupied, and available capacity from the backend and reservations ordered by arrival time. A date with no reservations shows an empty state. Stopping the backend and refreshing shows the error alert with a retry path.

Status: **pending verification** (visual check; the API and proxy path are verified in the log).

### 5. Manual reservation creation

Owner: **Joana**.

Action: until #29 merges, create the manual reservation through the API, then refresh the dashboard.

**Windows (PowerShell):**

```powershell
$body = @{ customerFirstName='Demo'; customerLastName='Manual'; phoneNumber='+34600000000'; reservationDate='DEMO_DATE'; arrivalTime='13:30'; partySize=2 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/reservations' -ContentType 'application/json' -Body $body
```

```powershell
$body = @{ customerFirstName='Demo'; customerLastName='TooLarge'; phoneNumber='+34600000000'; reservationDate='DEMO_DATE'; arrivalTime='21:00'; partySize=30 } | ConvertTo-Json
try { Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/reservations' -ContentType 'application/json' -Body $body } catch { $_.Exception.Response.GetResponseStream() | ForEach-Object { (New-Object System.IO.StreamReader($_)).ReadToEnd() } }
```

**macOS (Terminal):**

```bash
curl -sS -X POST http://localhost:8080/api/reservations -H 'Content-Type: application/json' -d '{"customerFirstName":"Demo","customerLastName":"Manual","phoneNumber":"+34600000000","reservationDate":"DEMO_DATE","arrivalTime":"13:30","partySize":2}'
```

```bash
curl -sS -X POST http://localhost:8080/api/reservations -H 'Content-Type: application/json' -d '{"customerFirstName":"Demo","customerLastName":"TooLarge","phoneNumber":"+34600000000","reservationDate":"DEMO_DATE","arrivalTime":"21:00","partySize":30}'
```

Then attempt a party of 30 on a service that already has bookings.

Expected: `201 Created` for the valid booking and the dashboard row and capacity updated after a refresh. The oversized party returns `409 Conflict` with `diningSession`, `availableCapacity`, and `requestedPartySize`, and no row is created.

Status: the manual API fallback is **pending verification** because the prior run's response evidence was not recorded. The dashboard form and its refresh behaviour are **pending verification** (blocked by #29).

### 6. Reset

Owner: shared demo operator.

**Proposed cleanup, pending verification:** capture the notification status and failure reason before deleting a booking. The notification row references its reservation without cascade deletion, so remove the notification row first.

**Windows (PowerShell) and macOS (Terminal):**

```powershell
docker compose exec postgres psql -U POSTGRES_USER -d POSTGRES_DB -c "SELECT reservation_id, status, failure_reason FROM reservation_notifications WHERE reservation_id = 'RESERVATION_ID';"
```

After recording the output in the verification log, delete the notification and reservation in one transaction. Repeat for each recorded reservation id.

```text
docker compose exec postgres psql -v ON_ERROR_STOP=1 -U POSTGRES_USER -d POSTGRES_DB -c "BEGIN; DELETE FROM reservation_notifications WHERE reservation_id = 'RESERVATION_ID'; DELETE FROM reservations WHERE id = 'RESERVATION_ID'; COMMIT;"
```

Replace `POSTGRES_USER` and `POSTGRES_DB` with the values from your `.env`, and `RESERVATION_ID` with a recorded id. To discard everything instead:

```bash
docker compose down -v
docker compose up -d
```

The full reset permanently deletes all local reservations and notification evidence. Use it between rehearsals, not during a demonstration, and capture the evidence first.

Expected: the deleted rows disappear from the dashboard and capacity returns to its previous value.

Status: **pending verification**. Confirm the recorded ids and preserve evidence before running either reset command.

## Recovery from a failed or interrupted demonstration

- **Caller hangs up before persistence.** No reservation is created. Nothing to clean; repeat the call. Do not create a manual reservation to "complete" the missing step.
- **Call ends after persistence.** The reservation stays, including when the SMS fails or is never requested. Do not delete it; record the notification state instead.
- **A step half-succeeds.** Confirm the persisted state with the read API before doing anything else. If the reservation exists, the demo continues from the dashboard step; do not create it a second time.
- **The dashboard looks stale.** Press **Refresh**. If it is still stale, query the read API. A stale view is not evidence that persistence failed.
- **The tunnel or the backend restarts mid-demo.** Re-register the application URLs if the tunnel URL changed, then verify with one read request that the backend and database are still reachable before resuming.
- **An SMS request has an ambiguous outcome.** Do not resend it. `vonage-01` found no idempotency key for a repeated SMS request, so a retry may send a second message. Record the unknown state and continue.
- **Between rehearsals.** Delete the recorded reservation ids or run the full reset. Never hand-edit rows to make a step pass, and never delete a notification record that is the evidence for a step.

Preserve evidence before any reset: copy a redacted read API result containing only required identifiers, date, time, service, and party-size data; record reservation ids, notification status, and failure reason from the query in step 3. Resetting first destroys the evidence.

Status: **pending verification** in the integrated walkthrough; do not report these recovery paths as observed until exercised.

## Known limitations and fallback wording

- In the controlled test in [#51](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/51), a fixed `es-ES` prompt was heard and speech-input callbacks arrived for fictional date, time, and party-size scenarios. The harness recorded only callback receipt, not transcripts. This does not prove unrestricted recognition, name capture, semantic validation, retry behaviour, or production readiness.
- The Messages Sandbox does not list SMS, so SMS testing uses the account's real SMS access and credits.
- SMS final delivery status can take up to 72 hours. A request accepted by Vonage is not proof of delivery, and the voice confirmation must never promise delivery.
- The manual reservation form (#29) is not merged; manual creation is demonstrated through the API.
- Notification state is persisted but not displayed in the dashboard (#30 excludes dashboard display).
- The demo has no login or authentication (RN-07). It runs on a local machine and must not be exposed beyond the tunnel used for webhooks.
- Capacity is fixed at 30 guests per service. There is no cancellation or modification in the MVP, so releasing capacity means deleting rows.
- Signed webhook verification is integrated for Messages (#34, #56); Voice integration (#35) remains in review. Use a public tunnel only for an authorized, temporary session.

## Verification log

Record every run here. Do not mark a step verified without an observed result.

| Step | Owner(s) | Environment | Expected | Observed | Status |
| --- | --- | --- | --- | --- | --- |
| Start PostgreSQL | Shared setup | Windows 11, Docker Compose | `postgres` healthy | healthy | verified |
| Start backend, `local` profile | Shared setup | Windows 11 | Starts on `:8080` | Already running on `:8080`; API returns 200 | verified |
| Apply current Flyway migrations | Shared setup | Windows 11 | V1 reservations and V2 notification migrations apply | V1 validated and applied; V2 result not recorded | pending verification |
| Proposed seed setup: two `POST /api/reservations` calls | Shared setup | Windows 11 | `201` with `id` and `diningSession`; mentor decision to use these calls as standard setup recorded | `201 LUNCH`, `201 DINNER`; setup decision pending | API behavior verified; setup proposal pending |
| Read API for the demo date | Shared setup | Windows 11 | Capacity 2/28 and 4/26 of 30 | as expected | verified |
| Capacity rejection via API | Shared setup | Windows 11 | `409` with capacity extensions, nothing persisted | `409`, available 26, no row | verified |
| Frontend rewrite to the backend | Joana | Windows 11 | `GET /api/reservations` through `:3000` returns the same JSON | JSON with matching capacity returned | verified |
| Controlled Spanish Voice proof (#51) | Project mentor (GabyB73) | Authorized controlled test | Fixed `es-ES` prompt; callbacks for fictional date, time, and party-size scenarios; fixed next NCCO returned | Three short calls; no unexpected cost; no transcripts or recordings; temporary harness and tunnel stopped | verified with stated limitations |
| Project account and Voice number decision | Shared / mentor decision | Integrated demo | Project account and Voice-enabled number agreed | pending | pending decision |
| Dashboard visual check | Joana | Windows 11 and macOS | Sections, ordering, capacity, empty and error states; refresh shows the selected date | pending | pending |
| Confirmed telephone reservation | Inés (call and capture); Andrea (confirmation and persistence); Joana (dashboard and SMS request) | Authorized live access | Spanish greeting and guided capture; correction/retry path; voice confirms after persistence; dashboard row; SMS request outcome shown | pending | pending |
| Same-date alternative by voice | Inés (call); Andrea (alternative) | Authorized live access | Lunch offered when dinner is full; accepted option persists, or no reservation if declined | pending | pending |
| Notification outcomes | Joana (request); Andrea (delivery status) | Authorized live access | Request accepted, failed, or unknown (`REQUEST_OUTCOME_UNKNOWN`) is shown separately from delivered status; skipped reason recorded when applicable | pending | pending |
| Manual reservation API fallback | Joana | Windows 11 | Valid reservation is created; oversized party is rejected without a row | Prior run lacks recorded response evidence; rerun required | pending |
| Manual creation form | Joana | Windows 11 and macOS | Form creates a reservation and refreshes the dashboard | pending | pending |
| Date-generation commands | Shared setup | Windows 11 and macOS | Date at least one day ahead in `Europe/Madrid` | pending | pending |
| Proposed ngrok tunnel setup | Shared setup / mentor decision | Authorized live session | Mentor confirms ngrok; HTTPS callback URL registered for required webhooks | pending | pending decision and verification |
| Reset and cleanup | Shared demo operator | Windows 11 and macOS | Recorded demo rows removed; capacity returns; full reset restores local database | pending | pending |
| Recovery paths | Shared demo operator | Integrated walkthrough | Interrupted/failed steps recover without duplicate reservations or lost evidence | pending | pending |
| Clean or separately configured walkthrough | Shared | Teammate's authorized local configuration | Teammate follows setup, fictional data, demo, and reset steps without machine-specific secrets | pending | pending |
| macOS commands | Shared setup | macOS Sequoia | Same results as Windows | pending | pending |

## Open items

- Record the mentor's decision on the proposed project account and Voice number, ngrok tunnel, and two `POST /api/reservations` seed calls before treating them as the agreed demo setup.
- Confirm the consenting contact, credits, and callback reachability for the live session before running steps 1 to 3.
- Record only the reservation id, status, and failure reason from the notification query; do not include contact data or raw payloads.
- Replace pending markers only after the integrated journeys merge and the steps have actually run. Complete and record a clean or separately configured teammate walkthrough before closing #39.
- Cite the automated evidence from #38 for scenarios that are covered by tests and not repeated by hand.
