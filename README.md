Contributor: @Naseem Win

# TheLittleThings

TheLittleThings is a goal-tracking and social motivation platform built for our Advanced Software Development course. Users can create goals, jot journal entries, celebrate wins, and stay accountable with friends through leaderboards and challenges. The solution is fully functional end to end, with both the hosted Azure deployment and the local workflow delivering the same experience.

## Repository Map

```
TheLittleThings/
├── frontend/            React 19 + TypeScript client (Vite, Tailwind, React Router)
├── backend/             Spring Boot 3.3.3 REST API (Java 17, PostgreSQL, Maven)
├── azure-pipelines.yml  Azure DevOps CI/CD pipeline
├── package.json         Root workspace definition for VS Code tooling
└── README.md            Project handbook (this file)
```

### Frontend layout

```
frontend/src/
├── api/           Typed API clients (fetch wrappers and DTO helpers)
├── components/    Reusable UI widgets grouped by domain (buttons, goals, journals…)
├── layouts/       Shared page chrome such as `RootLayout`
├── lib/           Utilities (`mapServerErrors`, formatting helpers)
├── pages/         Route-aligned screens (Goals, Journal, Leaderboard, etc.)
├── routes/        Router configuration and lazy loading helpers
└── styles/        Global Tailwind and design tokens
```

### Backend layout

```
backend/src/main/java/com/project/thelittlethings/
├── config/          Cross-cutting Spring configuration
├── controller/      REST controllers per aggregate (Goals, Wins, Journal, …)
├── dto/             Request/response transfer objects
├── entities/        JPA entities and persistence mappings
├── repositories/    Spring Data repositories
├── security/        Authentication & authorization components
├── services/        Business logic and orchestration layer
├── view/            Database view projections
└── materialisedview/ Materialized view bindings
```

Test sources live under `backend/src/test/java` mirroring the production package structure. Build artefacts land in `backend/target/` and should not be checked in.

## Technology Stack

- **Frontend:** React 19, TypeScript 5.9, Vite 7, Tailwind CSS 4, React Router 7, lucide-react icon set.
- **Backend:** Spring Boot 3.3.3, Java 17, Spring Data JPA, Bean Validation, PostgreSQL driver, Lombok (compile-time), Maven 3.
- **Testing:** JUnit 5, Spring Boot Test, H2 in-memory database for backend unit tests, Vitest (optional via Vite) and ESLint for frontend validation.
- **DevOps:** Azure Pipelines for CI/CD, GitHub Actions optional, Docker (recommended) for local database.
- **External services:** A shared Postgres instance runs on our Azure for Students subscription; that subscription expires **13 Nov 2025**, at which point the hosted database will be decommissioned. Plan to migrate or create a local database before then.

## Getting Started (Tutor Machine)

### Hosted Deployment

- **Public URL:** https://thelittlethings.azurewebsites.net/
- **Deployment pipeline:** `azure-pipelines.yml` builds the React frontend, copies the static assets into the backend, packages `app.jar`, and deploys the ZIP artifact to the App Service using the `TheLittleThings` service connection.
- **Database:** Production points to the managed Azure PostgreSQL instance inside the Azure for Students subscription (expires 13 Nov 2025). After that date, redeploy with a new connection string.
- **Environment variables:** Managed through the App Service configuration blade (`SPRING_DATASOURCE_*` keys mirror the local setup below).

You can verify a successful deployment by visiting the URL above and signing in with a seeded demo account (if enabled) or creating a new user account.

### Local Run (Fully Functional Build)

The cloud deployment is feature-complete; the steps below reproduce the exact stack on a local machine.

### 1. Prerequisites

- Node.js 18+ and npm 10+ (https://nodejs.org)
- Java 17 JDK (Temurin or Oracle)
- Maven Wrapper included (`mvnw.cmd`); Maven 3.9+ if you prefer a system install
- PostgreSQL 16 installed and running locally



### 2. Provision PostgreSQL 16

Make sure a local PostgreSQL 16 server is running before you start the backend.

- Create a `thelittlethings` database.
- Create a user `thelittlethings` with password `thelittlethings` and grant it ownership of the database.
- If you choose different names or credentials, update the environment variables in the next step to match.

**Initialize the database schema**

After creating the database and user, run the schema file to set up all tables and seed demo data:

1. Open a terminal and navigate to the project root.
2. Run the following command (replace with your actual path to `psql` if needed):

	```powershell
	psql -U thelittlethings -d thelittlethings -f backend/src/main/resources/db/schema.sql
	```

This will create all required tables and insert sample users.

### 3. Configure environment variables

The backend reads its datasource settings from environment variables. In PowerShell, set them in the session where you will start the server:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/thelittlethings"
$env:SPRING_DATASOURCE_USERNAME="thelittlethings"
$env:SPRING_DATASOURCE_PASSWORD="thelittlethings"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO="update"
```

For persistent configuration, create a `.env` file and source it, or add the variables through the Windows Environment Variables UI.

### 4. Start the backend API

```powershell
cd backend
./mvnw.cmd spring-boot:run
```

The application serves REST endpoints at `http://localhost:8080`. Logs are emitted at INFO by default; adjust via `application.properties` for deeper debugging.

### 5. Start the frontend client

Open a new PowerShell window:

```powershell
cd frontend
npm install
npm run dev
```

Vite hosts the UI at `http://localhost:5173`, proxying API calls to the backend according to the configuration in `frontend/src/api/http.ts`.

### 6. Run the automated test suites

- Backend: `cd backend; ./mvnw.cmd clean test`
- Frontend lint: `cd frontend; npm run lint`
- (Optional) Frontend unit tests: `npm run test` (configure Vitest if not already set up)

## Debugging & Developer Notes

- **Backend:** Enable SQL logs by setting `spring.jpa.properties.hibernate.show_sql=true`. For remote debugging, run `./mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"` and attach from your IDE.
- **Frontend:** Vite offers hot-module replacement. Use React DevTools and the browser network tab to inspect API calls. ESLint is configured to flag common issues—run `npm run lint` before committing.
- **Comments:** Complex service methods and non-obvious UI interactions include succinct inline comments explaining domain logic. Maintain this standard when extending the codebase.
- **CI pipeline:** See `azure-pipelines.yml` for build steps. The pipeline installs dependencies, runs tests, and produces artefacts for deployment.

## Team & Ownership

- **Ilker Paken (25527603):** Wins tracking & leaderboard (frontend `Leaderboard.tsx`, `Wins.tsx`; backend `WinService`, `LeaderboardService`, controllers).
- **Maxim Tabachuk (24615078):** Streaks and social features (`FriendsPage.tsx`, streak widgets; backend `FriendshipService`, `ChallengeService`).
- **Ali Idrees (24545790):** User management and journaling (`Register.tsx`, `Journal.tsx`, auth controllers, `UserService`, `JournalService`).
- **Naseem Win (24964684):** Goal and category management (`Goals.tsx`, `Categories.tsx`, `GoalService`, `CategoryService`).
- **Nasser Al Mughairi (24605154):** Settings and notifications (`Settings.tsx`, notification components, notification services).

Every source file begins with a contributor attribution comment to keep ownership transparent for tutors and collaborators.

## External Dependencies & Deadlines

- **Azure for Students subscription:** Hosts the shared Postgres instance and CI/CD secrets. Subscription owner: @Maxim Tabachuk. 

***Expiry:*** 13 Nov 2025. After that date, the hosted database and secrets will be removed—set up a replacement beforehand.

For further clarifications, reach out to @Naseem Win.


