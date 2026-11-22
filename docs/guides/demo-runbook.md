# Controlled demo runbook

This guide describes how to run the fictional Restaurant Receptionist MVP demo in a controlled local environment, and how to record what was actually verified. It is written for a teammate who has her own authorized local configuration.

Status: draft. Steps marked **pending** depend on the integrated Voice journey (#35 to #37), the notification journey (#30, #33, #34), and the manual reservation form (#29). Do not treat a pending step as verified.

Related documents: [MVP specification](../mvp-specification.md), [Business rules](../business-rules.md), [Local development setup](../setup.md), [Vonage integration](../vonage-integration.md), [Vonage contract research](../../specs/vonage-01-contract-verification.spec.md).

## Scope

In scope: the controlled local demo, its fictional data, its reset path, and the evidence recorded in this file.

Out of scope: production deployment, real restaurant operation, real customer data, and live calls or SMS without separate authorization.

## Evidence rules

- Every command and demo step is marked **verified** or **pending verification**. A step becomes verified only after someone ran it and observed the expected result.
- Evidence is recorded in the [verification log](#verification-log) in this file. Screenshots are optional; if used, they must show fictional data only.
- Evidence queries select identifiers, dates, times, services, and party sizes. Do not publish names, contact numbers, or raw provider payloads.
- Documented provider behaviour is not evidence of provider behaviour. Only an observed call, SMS, or callback counts.

## Prerequisites

### Tools

Install and verify the tools listed in [Local development setup](../setup.md#prerequisites): Git, Java 21, Docker Desktop with Compose, Node.js 22, npm 10. The backend uses Maven Wrapper.

### Vonage access

`vonage-01` records these as the prerequisites for a controlled test. They are confirmed as requirements; the values belong in local configuration only.

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

   If port 8080 is already in use, check whether the process is our backend before stopping it: `curl` the read API. A running backend is not a failure. Expected: the backend starts at `http://localhost:8080` and Flyway applies `V1__create_reservations.sql` without error.

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

4. Live mode only: expose the backend to Vonage with a tunnel.

   ```powershell
   ngrok http 8080
   ```

   Expected: ngrok prints a public HTTPS URL. Register it as the Voice application's `answer_url` and `event_url` and as the Messages status webhook, then keep the terminal open for the whole session. The URL changes when the tunnel restarts, so re-register it after a restart.

   The tunnel tool is not yet decided by the project; `ngrok` is the option recommended by the Vonage webhook documentation. **Pending verification.**

## Prepare the demo data

Demo bookings are created at demo time through the approved API. They are not a migration: a Flyway seed would also apply to the test database and could not seed a date that is valid on the demo day.

1. Choose a date at least one day ahead, so the demo does not depend on the current time. The restaurant uses `Europe/Madrid`.

   **Windows (PowerShell):**

   ```powershell
   $demoDate = (Get-Date).AddDays(1).ToString('yyyy-MM-dd')
   ```

   **macOS (Terminal):**

   ```bash
   demoDate=$(date -v+1d +%F)
   ```

2. Create one lunch booking and one dinner booking. Replace `DEMO_DATE` with the date you computed.

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

## Demonstration sequence

The sequence below is the demo script. Steps 1, 2, 4, and 5 need the integrated journeys and are marked pending; steps 3 and 6 exercise the merged backend and dashboard.

### 1. Confirmed telephone reservation

Action: call the Vonage number from the consenting phone. Give a fictional name, the consenting contact number, the demo date, dinner at 21:00, and a party of 2. Answer that the number can receive SMS. Confirm the readback.

Expected: the call follows the approved journey in RN-04 and RN-05; the voice confirms only after the reservation is persisted; the dashboard shows the new dinner row after a refresh. The exact prompts are defined by #36 and #37.

Evidence: the reservation `id`, the dashboard row, and the notification record from step 4. The SMS itself is observed on the phone; do not publish the number or the message content.

Status: **pending verification** (blocked by #37).

### 2. Capacity and same-date alternative

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

Action: after step 1, observe the SMS on the consenting phone and read the notification record. In the step 2 call, answer that the number cannot receive SMS.

Expected: an eligible number produces a request accepted by Vonage with a `message_uuid`, recorded as accepted rather than delivered; the final state arrives later, and SMS final status can take up to 72 hours. A number that cannot receive SMS or whose capability is unknown keeps the reservation confirmed, requests no message, and records the reason. A request or delivery failure never cancels the reservation.

Evidence: the notification state and reason, without contact data or raw payloads.

Status: **pending verification** (blocked by #30, #33, #34). The table and column names will be defined by #30; this guide will name the exact query once they exist.

### 4. Dashboard visibility

Action: open `http://localhost:3000/dashboard`, select the demo date, and press **Refresh**.

Expected: a Lunch section (`13:00–16:00`) and a Dinner section (`20:00–23:30`), each with total, occupied, and available capacity from the backend and reservations ordered by arrival time. A date with no reservations shows an empty state. Stopping the backend and refreshing shows the error alert with a retry path.

Status: **pending verification** (visual check; the API and proxy path are verified in the log).

### 5. Manual reservation creation

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

Status: API path verified (see the log). The dashboard form is **pending verification** (blocked by #29).

### 6. Reset

Delete the demo bookings by id, or reset the whole local database.

**Windows (PowerShell):**

```powershell
docker compose exec postgres psql -U POSTGRES_USER -d POSTGRES_DB -c "DELETE FROM reservations WHERE id = 'RESERVATION_ID';"
```

**macOS (Terminal):**

```bash
docker compose exec postgres psql -U POSTGRES_USER -d POSTGRES_DB -c "DELETE FROM reservations WHERE id = 'RESERVATION_ID';"
```

Replace `POSTGRES_USER` and `POSTGRES_DB` with the values from your `.env`, and `RESERVATION_ID` with a recorded id. To discard everything instead:

```bash
docker compose down -v
docker compose up -d
```

The second form permanently deletes all local data. Use it between rehearsals, not during a demonstration.

Expected: the deleted rows disappear from the dashboard and capacity returns to its previous value.

## Recovery from a failed or interrupted demonstration

- **Caller hangs up before persistence.** No reservation is created. Nothing to clean; repeat the call. Do not create a manual reservation to "complete" the missing step.
- **Call ends after persistence.** The reservation stays, including when the SMS fails or is never requested. Do not delete it; record the notification state instead.
- **A step half-succeeds.** Confirm the persisted state with the read API before doing anything else. If the reservation exists, the demo continues from the dashboard step; do not create it a second time.
- **The dashboard looks stale.** Press **Refresh**. If it is still stale, query the read API. A stale view is not evidence that persistence failed.
- **The tunnel or the backend restarts mid-demo.** Re-register the application URLs if the tunnel URL changed, then verify with one read request that the backend and database are still reachable before resuming.
- **An SMS request has an ambiguous outcome.** Do not resend it. `vonage-01` found no idempotency key for a repeated SMS request, so a retry may send a second message. Record the unknown state and continue.
- **Between rehearsals.** Delete the recorded reservation ids or run the full reset. Never hand-edit rows to make a step pass, and never delete a notification record that is the evidence for a step.

Preserve evidence before any reset: copy the read API output and the reservation ids into the verification log. Resetting first destroys the evidence.

## Known limitations and fallback wording

- Spanish speech recognition and playback are documented for `es-ES` but have never been exercised in a call (`vonage-01`). If capture fails during the demo, say so plainly: the provider contract is documented, the practical recognition is unverified.
- The Messages Sandbox does not list SMS, so SMS testing uses the account's real SMS access and credits.
- SMS final delivery status can take up to 72 hours. A request accepted by Vonage is not proof of delivery, and the voice confirmation must never promise delivery.
- The manual reservation form (#29) is not merged; manual creation is demonstrated through the API.
- Notification state is persisted but not displayed in the dashboard (#30 excludes dashboard display).
- The demo has no login or authentication (RN-07). It runs on a local machine and must not be exposed beyond the tunnel used for webhooks.
- Capacity is fixed at 30 guests per service. There is no cancellation or modification in the MVP, so releasing capacity means deleting rows.
- A public tunnel exposes the backend. Webhook signature verification belongs to #35 and #34; until it is implemented, treat the tunnel as a temporary, authorized session only.

## Verification log

Record every run here. Do not mark a step verified without an observed result.

| Step | Environment | Expected | Observed | Status |
| --- | --- | --- | --- | --- |
| Start PostgreSQL | Windows 11, Docker Compose | `postgres` healthy | healthy | verified |
| Start backend, `local` profile | Windows 11 | Starts on `:8080`, Flyway applies V1 | Already running on `:8080`; Flyway validated and applied V1 in the run attempt; API returns 200 | verified |
| Seed: lunch and dinner bookings | Windows 11 | `201` with `id` and `diningSession` | `201 LUNCH`, `201 DINNER` | verified |
| Read API for the demo date | Windows 11 | Capacity 2/28 and 4/26 of 30 | as expected | verified |
| Capacity rejection | Windows 11 | `409` with capacity extensions, nothing persisted | `409`, available 26, no row | verified |
| Frontend rewrite to the backend | Windows 11 | `GET /api/reservations` through `:3000` returns the same JSON | JSON with matching capacity returned | verified |
| Dashboard visual check | Windows 11 and macOS | Sections, ordering, capacity, empty and error states | pending | pending |
| Confirmed telephone reservation | Windows 11, authorized live access | Voice confirms after persistence, SMS requested | pending | pending |
| Same-date alternative by voice | Windows 11, authorized live access | Lunch offered, dinner full | pending | pending |
| Notification outcomes | Windows 11, authorized live access | Accepted vs delivered, skipped reason recorded | pending | pending |
| Manual creation form | Windows 11 and macOS | Form creates and refreshes the dashboard | pending | pending |
| macOS commands | macOS Sequoia | Same results as Windows | pending | pending |

## Open items

- Confirm the account, consenting number, credits, and tunnel tool for the live session before running steps 1, 2, and 3.
- Update the notification evidence step with the exact query once #30 defines the table.
- Replace the pending markers after the integrated journeys merge and the session has actually run.
- Cite the automated evidence from #38 for scenarios that are covered by tests and not repeated by hand.
