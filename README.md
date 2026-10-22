# Restaurant Receptionist

Restaurant Receptionist is an educational pilot project by FemCoders Club in collaboration with Vonage.

## Problem

During peak service hours, restaurant staff may be unable to answer incoming calls. Each missed call can become a missed reservation and lost revenue.

The same problem can occur before opening hours, when staff may be preparing the restaurant, cleaning, preparing food, or completing operational tasks and cannot answer the phone.

## Goal

Build a telephone receptionist for a fictional restaurant. The MVP will handle reservation calls, collect reservation details, validate capacity, request a reservation confirmation message, and provide a basic dashboard for restaurant staff.

## MVP scope

- Guided telephone flow using Vonage Voice API.
- Reservation data: customer name, phone number, date, arrival time, and party size.
- Capacity managed independently for lunch and dinner services.
- Explicit customer confirmation before a reservation is persisted.
- Duplicate prevention and safe handling of concurrent reservation attempts.
- Reservation message requests through Vonage Messages API. A messaging failure must not cancel a confirmed reservation.
- Basic dashboard showing reservations and capacity by date and service.
- Automated tests for critical business rules.

The MVP excludes conversational AI, table allocation, multiple restaurants, and real customer data.

## Technology status

The technology stack has not been selected yet. Vonage Voice API and Messages API are planned integrations. Any proposal involving a backend framework, frontend framework, database, deployment platform, or additional dependency remains subject to an explicit project decision.

## Project status

Repository setup and team onboarding are in progress. Application scaffolding has not been initialized.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before contributing. Contributors work from approved issues, create branches from `develop`, and open pull requests to `develop`.

## Documentation

- [MVP specification](docs/mvp-specification.md)
- [Documentation index](docs/README.md)
- [AI agent instructions](AGENTS.md)

## License

This project is licensed under the [MIT License](LICENSE).
