# Restaurant Receptionist

Restaurant Receptionist is a FemCoders Club pilot project developed in collaboration with Vonage.

## Problem

During peak service hours, restaurant staff may be unable to answer incoming calls. Each missed call can become a missed reservation and lost revenue.

The same problem can occur before opening hours, when staff may be preparing the restaurant, cleaning, preparing food, or completing operational tasks and cannot answer the phone.

## Goal

Build a telephone receptionist for a fictional restaurant. The MVP will handle reservation calls, collect reservation details, validate capacity, persist confirmed reservations, request an SMS confirmation, and provide a basic dashboard for restaurant staff.

## MVP scope

- Guided telephone flow using Vonage Voice API.
- Reservation data: customer name, phone number, date, arrival time, and party size.
- Capacity managed independently for lunch and dinner services.
- Explicit customer confirmation before a reservation is persisted.
- Duplicate prevention and safe handling of concurrent reservation attempts.
- Reservation message requests through Vonage Messages API. A messaging failure must not cancel a confirmed reservation.
- Basic dashboard showing reservations and capacity by date and service, with manual reservation creation that uses the same backend validation rules as telephone reservations.
- Automated tests for critical business rules.

The MVP excludes conversational AI, table allocation, multiple restaurants, RCS, Verify/Silent Authentication, and real customer data.

## Technology stack

| Area | Technology and initial version |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.2, Maven Wrapper |
| Data | PostgreSQL 17.11, Spring Data JPA / Hibernate, Flyway |
| Frontend | Node.js 22 LTS, npm 10, Next.js 16.4.4, Tailwind CSS |
| Local environment | Docker, Docker Compose |
| Testing | Testcontainers for integration tests |
| Communications | Vonage Voice API and Vonage Messages API (SMS) |

Framework-managed library versions, API contracts, deployment and CI configuration are recorded as they are approved. See [ADR-005](docs/adr/005-pin-initial-runtime-and-tooling-versions.md) for the initial version policy.

## Project status

Initial technical setup is in progress. The backend and frontend scaffolding, local PostgreSQL configuration, and verified local commands are available. The initial CI workflow awaits its first GitHub execution. No product feature has been implemented yet.

## Clone the repository

```bash
git clone https://github.com/femCodersClub-vonage-program/restaurant-receptionist.git
cd restaurant-receptionist
```

Follow the verified [local development setup guide](docs/setup.md) to configure PostgreSQL, run the backend and frontend, and execute the checks.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before contributing. Contributors work from approved issues, create branches from `develop`, and open pull requests to `develop`.

## Documentation

- [MVP specification](docs/mvp-specification.md)
- [Business rules](docs/business-rules.md)
- [Architecture](docs/architecture.md)
- [Vonage integration](docs/vonage-integration.md)
- [Local development setup](docs/setup.md)
- [Security policy](SECURITY.md)
- [Documentation index](docs/README.md)
- [AI agent instructions](AGENTS.md)

## Team

| Name | Role | GitHub |
| --- | --- | --- |
| Gabriela Bustamante | Project mentor and tech lead | [GabyB73](https://github.com/GabyB73) |
| To be confirmed | Participant | — |
| To be confirmed | Participant | — |
| To be confirmed | Participant | — |

## License

This project is licensed under the [MIT License](LICENSE).
