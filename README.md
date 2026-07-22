# Donate Web Platform

Donate Web Platform is a full-stack donation system designed for interactions between viewers and content creators. The platform provides account management, streamer profiles, multiple donation methods, wallet operations, real-time notifications, moderation tools, and an administration interface.

This repository contains both the Spring Boot API and the React client.

## 1. System objectives

The system is intended to provide the following capabilities:

- Authenticate users with email credentials, JSON Web Tokens, or Google Sign-In.
- Manage user and streamer profiles, media, biographies, and public donation pages.
- Process donations through wallet balance, bank QR, and payment QR workflows.
- Record donations, payments, wallet transactions, and withdrawal requests.
- Deliver donation and payment updates in real time through WebSocket and STOMP.
- Support following, blocking, notifications, reports, and moderation.
- Provide administrative views for users, streamers, transactions, reports, and payment methods.

## 2. Repository structure

```text
donate-web-BE/
|-- donate-backend/        Spring Boot REST API and WebSocket server
|-- donate-web/            React and TypeScript web application
|-- test/                  Load, concurrency, and WebSocket test scripts
`-- README.md              Project documentation
```

The backend follows a layered architecture:

```text
HTTP or WebSocket client
          |
          v
Controller and security layer
          |
          v
Service and domain logic
          |
          v
Repository and cache layer
          |
          +-------------------+
          v                   v
     PostgreSQL              Redis
```

External integrations are isolated behind service or configuration components, including Cloudinary, Google token verification, VietQR, and FPT AI Text to Speech.

## 3. Technology stack

| Layer | Technology |
|---|---|
| Backend language | Java 17 |
| Backend framework | Spring Boot 3.3.5 |
| Security | Spring Security, JWT |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Cache and shared data | Caffeine, Redis |
| Real-time communication | WebSocket, STOMP, SockJS |
| Frontend | React 19, TypeScript 4.9 |
| Client state | Redux Toolkit |
| HTTP client | Axios |
| Build tools | Gradle Wrapper, npm |
| Observability | Spring Boot Actuator |
| Container support | Docker, Docker Compose |

## 4. Functional modules

| Module | Responsibility |
|---|---|
| Authentication | Registration, login, current-user lookup, Google authentication, and JWT validation |
| User management | Profile updates and avatar upload |
| Streamer management | Public profile, media, biography, settings, statistics, and promoted products |
| Donation | Wallet donations, QR donations, histories, rankings, and streamer donation feeds |
| Wallet | Balance lookup, transaction history, and withdrawal requests |
| Social features | Following, blocking, and notifications |
| Real-time services | Donation events and payment status updates |
| Moderation | Violation reports, penalties, and administrative review |
| Administration | User, streamer, transaction, report, and payment-method management |

## 5. Prerequisites

Install the following software before running the project locally:

- Java Development Kit 17
- Node.js 18 or later
- npm
- PostgreSQL
- Redis

The repository includes a Gradle Wrapper, so a system-wide Gradle installation is not required.

## 6. Configuration

Most backend configuration is located at:

```text
donate-backend/src/main/resources/application.properties
```

Configure the following values for the target environment:

| Configuration group | Required information |
|---|---|
| PostgreSQL | JDBC URL, username, and password |
| Redis | Host and port |
| JWT | Signing secret |
| VietQR | Account credentials, bank information, and endpoint URLs |
| FPT AI | API URL, API key, and voice |
| Cloudinary | Cloud name, API key, and API secret; currently defined in `CloudinaryConfig` |
| Google authentication | OAuth client identifier; currently defined in `GoogleTokenVerifier` |

Do not commit production credentials to the repository. Values currently defined in Java configuration classes should be externalized before deployment. Use environment variables or an external secret-management system in deployed environments. Spring Boot environment variables can override properties by converting property names to uppercase and replacing periods and hyphens with underscores. For example:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_DATA_REDIS_HOST
SPRING_DATA_REDIS_PORT
JWT_SECRET
FPTAI_TEXTTOSPEECH_KEY
```

The frontend currently connects to the API and WebSocket server at `http://localhost:8080`. These addresses are defined in:

```text
donate-web/src/services/exiosClient.ts
donate-web/src/services/socket.ts
```

## 7. Local execution

### 7.1 Prepare infrastructure

Create a PostgreSQL database that matches the configured JDBC URL. Start Redis locally or with Docker:

```bash
docker run --name donate-redis -p 6379:6379 -d redis:7
```

### 7.2 Start the backend

On Windows:

```powershell
cd donate-backend
./gradlew.bat bootRun
```

On Linux or macOS:

```bash
cd donate-backend
./gradlew bootRun
```

The backend listens on `http://localhost:8080` by default.

### 7.3 Start the frontend

Open a second terminal:

```bash
cd donate-web
npm install
npm start
```

The development client listens on `http://localhost:3000` by default.

## 8. Build and verification

Run backend tests and create the executable JAR:

```powershell
cd donate-backend
./gradlew.bat clean test build
```

Run frontend tests:

```bash
cd donate-web
npm test
```

Create a production frontend build:

```bash
cd donate-web
npm run build
```

The backend artifact is generated under `donate-backend/build/libs/`. The frontend production files are generated under `donate-web/build/`.

## 9. API overview

The following table lists the principal API groups. It is an overview rather than a complete API specification.

| Base path | Purpose | Typical access |
|---|---|---|
| `/api/auth` | Registration, login, Google authentication, and current account | Public and authenticated |
| `/api/user` | User profile and avatar | Authenticated |
| `/api/streamers` | Streamer discovery, profile, media, settings, and statistics | Public and streamer |
| `/api/donate` | Donation creation, history, and rankings | Public and authenticated |
| `/api/payments` | Payment QR generation | Depends on operation |
| `/api/payment-account` | Streamer payment-account settings | Authenticated |
| `/api/wallets` | Wallet information | Authenticated |
| `/api/wallet-transactions` | Wallet transaction history | Depends on operation |
| `/api/withdrawals` | Withdrawal requests | Authenticated |
| `/api/follows` | Follower and following operations | Authenticated |
| `/api/notifications` | Notification retrieval and read status | Authenticated |
| `/api/reports` | Violation-report submission and history | Authenticated |
| `/api/admin` | Administrative operations | Administrator |
| `/api/webhooks` | External payment callbacks | Public callback |
| `/api/tts` | Text-to-speech integration | Public |

Selected endpoints:

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register an account |
| `POST` | `/api/auth/login` | Authenticate and obtain a JWT |
| `GET` | `/api/auth/me` | Retrieve the authenticated account |
| `GET` | `/api/streamers/search` | Search public streamer profiles |
| `GET` | `/api/streamers/{token}` | Retrieve a streamer profile |
| `POST` | `/api/donate/qr` | Create a payment-QR donation |
| `POST` | `/api/donate/bank-qr` | Create a bank-QR donation |
| `POST` | `/api/donate/wallet` | Donate with wallet balance |
| `GET` | `/api/donate/history` | Retrieve donation history |
| `GET` | `/api/wallets/me` | Retrieve the current wallet |
| `POST` | `/api/withdrawals` | Create a withdrawal request |
| `GET` | `/api/notifications` | Retrieve notifications |
| `POST` | `/api/webhooks/sepay` | Receive a SePay callback |
| `GET` | `/api/admin/overview` | Retrieve the administration summary |

JWT-protected requests use the following header:

```http
Authorization: Bearer <access-token>
```

## 10. Real-time communication

The backend exposes a SockJS endpoint at:

```text
/ws
```

STOMP clients receive events through `/topic` destinations. The frontend currently subscribes to:

```text
/topic/donate/{streamerId}
/topic/payment/{donationId}
```

The in-memory simple broker is suitable for a single backend instance. A distributed broker should be introduced before horizontally scaling real-time workloads.

## 11. Data model

The primary persistence groups are:

- Users, roles, and authentication data
- Streamer profiles, settings, social links, and blocks
- Donations and payment records
- Wallets, wallet transactions, and withdrawals
- Followers and notifications
- Product promotions
- Violation reports and penalties

PostgreSQL is the authoritative relational data store. Redis and Caffeine are used for caching or supporting high-frequency access paths and must not be treated as the source of record.

## 12. Performance testing

Performance and concurrency scripts are stored in the `test` directory:

```text
test/websocket-performance-test.cjs
test/donate-concurrency-test.cjs
test/donate-ramp-test.cjs
test/donate-capacity-test.cjs
test/k6-donate-load.js
```

These scripts cover donation throughput, concurrent requests, ramp-up behavior, capacity limits, and WebSocket delivery. Review each script's target URL, test account, and workload parameters before execution.

## 13. Current limitations

- The repository does not yet include an OpenAPI specification.
- Automated unit and integration test coverage is limited.
- The WebSocket message broker is local to one application instance.
- Backend and frontend service addresses are not yet centralized in environment-specific profiles.
- The Docker Compose database service and the active PostgreSQL application configuration require alignment before Compose can be used as a complete local environment.

## 14. Recommended development priorities

1. Move all credentials and service identifiers to environment variables or managed secrets.
2. Add OpenAPI documentation and request-response examples.
3. Add integration tests for authentication, donations, wallet accounting, and webhook processing.
4. Align Docker Compose with PostgreSQL and add health checks for service dependencies.
5. Externalize frontend API and WebSocket URLs.
6. Add continuous integration for backend tests, frontend tests, and production builds.

## 15. Author

Ngo Van Duc

Email: ducvan26324@gmail.com

## 16. License

This project is distributed under the MIT License.
