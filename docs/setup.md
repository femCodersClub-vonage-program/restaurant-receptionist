# Local development setup

This guide describes the initial technical setup for Restaurant Receptionist. It contains commands verified on Windows 11. macOS and Linux equivalents have not yet been verified by the team.

The project has two independently runnable applications:

- `backend/`: Spring Boot API, Java 21, Maven Wrapper, Flyway, and PostgreSQL-backed integration tests.
- `frontend/`: Next.js dashboard, TypeScript, Tailwind CSS, and npm.

## Prerequisites

Install the following before starting:

| Tool | Required version |
| --- | --- |
| Git | A current supported version |
| Java | 21 LTS |
| Docker Desktop and Docker Compose | Docker Compose v2-compatible installation |
| Node.js | 22 LTS |
| npm | 10 |

Verify the installation:

```powershell
java --version
node --version
npm --version
docker --version
docker compose version
docker info
```

The backend uses Maven Wrapper, so a separately installed Maven version is not required to run the project commands.

## Clone the repository

```powershell
git clone https://github.com/femCodersClub-vonage-program/restaurant-receptionist.git
Set-Location restaurant-receptionist
```

## Configure local PostgreSQL

Copy the committed example file. The resulting `.env` file is ignored by Git and must never be committed or shared.

```powershell
Copy-Item -LiteralPath '.\.env.example' -Destination '.\.env'
```

Open `.env` and replace only `POSTGRES_PASSWORD` with a local-only password. Do not use a production credential or put real personal data in this file.

The default `POSTGRES_PORT` is `5432`. If that port is already used by a local PostgreSQL installation or another service, choose an available port such as `5433`. The same value is used by Docker Compose and the backend local profile.

Start PostgreSQL:

```powershell
docker compose config --quiet
docker compose up -d
docker compose ps
```

Wait until the `postgres` service status is `healthy` before starting the backend. To stop the local service while retaining its data volume:

```powershell
docker compose down
```

## Run the backend locally

Docker Compose reads `.env` automatically. A Spring Boot process started directly from PowerShell does not, so load the variables into the current PowerShell session first:

```powershell
Get-Content -LiteralPath '.\.env' |
  Where-Object { $_.Trim() -and -not $_.Trim().StartsWith('#') } |
  ForEach-Object {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), 'Process')
  }

$env:SPRING_PROFILES_ACTIVE = 'local'
```

Then start the backend:

```powershell
Set-Location .\backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

The backend starts at `http://localhost:8080`. The initial setup intentionally exposes no product endpoints. Stop the process with `Ctrl+C`.

Every new PowerShell session needs the environment-loading step again before starting the backend with the `local` profile.

## Run backend tests

From `backend/`, run:

```powershell
.\mvnw.cmd test
```

The integration test uses Testcontainers and a separate PostgreSQL container. It does not use the PostgreSQL instance started by Docker Compose, but Docker Desktop must be running.

## Run the frontend locally

From the repository root, install locked dependencies and start Next.js:

```powershell
Set-Location .\frontend
npm ci
npm run dev
```

Open `http://localhost:3000`. Stop the development server with `Ctrl+C`.

Validate the frontend before opening a pull request:

```powershell
npm run lint
npm run build
```

## Environment and security notes

- The root `.env` configures Docker Compose. It is not automatically read by Spring Boot when the backend runs directly from PowerShell.
- Do not create a frontend environment file until the frontend has an approved API configuration requirement. Frontend environment files must never contain database, Vonage, or backend secrets.
- Do not run `npm audit fix` or upgrade dependencies automatically. Review and approve dependency changes through an issue and pull request.
- Do not commit `.env`, credentials, tokens, private keys, phone numbers, or real personal data. See the [Security Policy](../SECURITY.md).

## Verified commands

The following initial checks have been completed on Windows 11:

- Backend integration test: `.\mvnw.cmd test`
- Backend local startup against Docker Compose PostgreSQL 17.11
- Frontend lint: `npm run lint`
- Frontend production build: `npm run build`
- Frontend development server: `npm run dev` at `http://localhost:3000`
