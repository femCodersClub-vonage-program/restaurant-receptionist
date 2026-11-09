# Local development setup

This guide describes the initial technical setup for Restaurant Receptionist. It contains commands verified on Windows 11 and macOS Sequoia. Linux equivalents have not yet been verified by the team.

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

**Windows (PowerShell):**
```powershell
git clone https://github.com/femCodersClub-vonage-program/restaurant-receptionist.git
Set-Location restaurant-receptionist
```

**macOS (Terminal):**
```bash
git clone https://github.com/femCodersClub-vonage-program/restaurant-receptionist.git
cd restaurant-receptionist
```

## Configure local PostgreSQL

Copy the committed example file. The resulting `.env` file is ignored by Git and must never be committed or shared.

**Windows (PowerShell):**
```powershell
Copy-Item -LiteralPath '.\.env.example' -Destination '.\.env'
```

**macOS (Terminal):**
```bash
cp .env.example .env
```

Open `.env` and replace only `POSTGRES_PASSWORD` with a local-only password. Do not use a production credential or put real personal data in this file.

The commands for both PowerShell (Windows) and Terminal (macOS) provided in this guide treat the `.env` file as a series of simple `KEY=value` pairs. When following the instructions for both PowerShell (Windows) or Terminal (macOS), the `POSTGRES_PASSWORD` must be a string containing no quotation marks, no spaces, and none of the following characters: `#`, `$`, backticks, `!`, `;`, `&`, or `|`. Since the loader on macOS uses the `source` command, these special shell characters could otherwise be expanded or interpreted by the terminal. Therefore, to ensure consistency, use this same restricted format for the password in Docker Compose, as well as in the instructions for PowerShell (Windows) and Terminal (macOS).

The default `POSTGRES_PORT` is `5432`. If that port is already used by a local PostgreSQL installation or another service, choose an available port such as `5433`. The same value is used by Docker Compose and the backend local profile.

Start PostgreSQL:

**For both Windows and macOS**:
```powershell
docker compose config --quiet
docker compose up -d
docker compose ps
```

Wait until the `postgres` service status is `healthy` before starting the backend. To stop the local service while retaining its data volume:

**For both Windows and macOS:**
```powershell
docker compose down
```

PostgreSQL applies `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` only when it initializes an empty volume. Changing those values later does not update an existing database. Recreating the volume with `docker compose down -v` applies new initialization values, but permanently deletes all local database data; use it only when that deletion is intended.

## Run the backend locally

Docker Compose reads `.env` automatically. A Spring Boot process started directly from a shell or terminal does not, so load the variables into the current shell/terminal session first:

**Windows (PowerShell):**
```powershell
Get-Content -LiteralPath '.\.env' |
  Where-Object { $_.Trim() -and -not $_.Trim().StartsWith('#') } |
  ForEach-Object {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), 'Process')
  }

$env:SPRING_PROFILES_ACTIVE = 'local'
```

**macOS (Terminal):**
```bash
set -a
source ./.env
set +a
export SPRING_PROFILES_ACTIVE='local'
```

Then start the backend:

**Windows (PowerShell):**
```powershell
Set-Location .\backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

**macOS (Terminal):**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The backend starts at `http://localhost:8080`. The initial setup intentionally exposes no product endpoints, so there are no application routes to use yet. Stop the process with `Ctrl+C` on both Windows and macOS.

The environment-loading commands must be repeated in every new PowerShell or macOS Terminal session before starting the backend with the `local` profile. The variables are available only for the current shell/terminal session.

## Run backend tests

From `backend/`, run:

**Windows (PowerShell):**
```powershell
.\mvnw.cmd test
```

**macOS (Terminal):**
```bash
./mvnw test
```

The integration test uses Testcontainers and a separate PostgreSQL container. It does not use the PostgreSQL instance started by Docker Compose, but Docker Desktop must be running.

## Run the frontend locally

From the repository root, install locked dependencies and start Next.js:

**Windows (PowerShell):**
```powershell
Set-Location .\frontend
npm ci
npm run dev
```

**macOS (Terminal):**
```bash
cd frontend/
npm ci
npm run dev
```

Open `http://localhost:3000`. Stop the development server with `Ctrl+C`.

Validate the frontend before opening a pull request:

**For both Windows and macOS:**
```powershell
npm run lint
npm run build
```

## Environment and security notes

- The root `.env` configures Docker Compose. It is not automatically read by Spring Boot when the backend runs directly from PowerShell or macOS Terminal. Therefore, the environment variables must be loaded again in each new shell or terminal session before starting the backend.
- Do not create a frontend environment file until the frontend has an approved API configuration requirement. Frontend environment files must never contain database, Vonage, or backend secrets.
- Do not run `npm audit fix` or upgrade dependencies automatically. Review and approve dependency changes through an issue and pull request.
- Do not commit `.env`, credentials, tokens, private keys, phone numbers, or real personal data. See the [Security Policy](../SECURITY.md).

## Verified commands

The following initial checks have been completed on Windows 11 and on macOS Sequoia 15.6.1:

- Backend integration test: `.\mvnw.cmd test` (PowerShell) or `./mvnw test` (Terminal)
- Backend local startup against Docker Compose PostgreSQL 17.11
- Frontend lint: `npm run lint`
- Frontend production build: `npm run build`
- Frontend development server: `npm run dev` at `http://localhost:3000`
