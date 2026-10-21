# Nexgradia Backend Foundation

Minimal Spring Boot backend foundation for **Nexgradia — Intelligent Campus Placement & Career Intelligence Platform**.

## Technology Stack
- **Language & Runtime:** Java 21 (compatible with Java 21+)
- **Framework:** Spring Boot 3.4.3
- **Build Tool:** Apache Maven
- **Security:** Spring Security 6 with JJWT (`io.jsonwebtoken` 0.12.6) stateless Bearer token authentication
- **Database:** PostgreSQL (with Flyway migrations)
- **Monitoring/Health:** Spring Boot Actuator + Custom Health Endpoint (`/api/v1/health`)
- **Testing:** JUnit 5, MockMvc, H2 (for isolated unit/integration tests without running PostgreSQL)

---

## Authentication & Role-Based Access Control

The platform implements stateless JSON Web Token (JWT) authentication:
- **Roles:**
  - `STUDENT`: Normal student user (can access `/api/v1/student/**` and own account profile).
  - `RECRUITER`: Company recruitment partner (can access `/api/v1/recruiter/**` and own account profile).
  - `PLACEMENT_ADMIN`: Placement cell administrative staff (can access `/api/v1/admin/**` and inspect user profiles).
- **Security Controls:**
  - Passwords hashed using **BCrypt** (10 rounds).
  - Public registration only allows non-privileged roles (`STUDENT`, `RECRUITER`). Attempting to register `PLACEMENT_ADMIN` publicly returns HTTP 403 Forbidden.
  - Insecure Direct Object Reference (IDOR) prevention: endpoint `/api/v1/users/{id}` strictly enforces `@PreAuthorize("hasRole('PLACEMENT_ADMIN') or #id == principal.id")`.
  - Passwords and sensitive claims are never serialized or returned in API responses.
  - CORS strictly configured to explicit origins (no wildcard `*` with credentials).
  - Production profile (`application-prod.yml`) enforces that `JWT_SECRET` must be set externally and rejects default dev keys.

### Authentication Endpoints
- `POST /api/v1/auth/register` — Register a student or recruiter account.
- `POST /api/v1/auth/login` — Authenticate and receive a signed JWT Bearer token.
- `GET /api/v1/users/me` — Retrieve the currently authenticated user's account data.
- `GET /api/v1/users/{id}` — Retrieve a user account by ID (owner or admin only).

### Student Profile & Academic Record Endpoints (`ROLE_STUDENT` Only)
- `GET /api/v1/students/me` — Retrieve authenticated student's profile.
- `PUT /api/v1/students/me` — Create or update authenticated student's profile.
- `GET /api/v1/students/me/academics` — Retrieve authenticated student's academic records and semester history.
- `PUT /api/v1/students/me/academics` — Create or update academic records (scores, backlogs, semester breakdown). Student submissions always force `UNVERIFIED` status.

---

## Rate-Limiting & Brute-Force Mitigation Plan

> **Current Implementation Status:**
> Rate limiting is **NOT** yet implemented in the application source code in Task 2.
> The architectural plan below documents how brute-force protection and rate limiting will be enforced before public deployment.

1. **Authentication Endpoint Protection:**
   - Public auth endpoints (`/api/v1/auth/login`, `/api/v1/auth/register`) are high-value targets for brute-force and credential-stuffing attacks.
2. **Planned Mitigation Strategy:**
   - **Layer 1 (Network/Edge WAF):** In cloud deployment (e.g., AWS CloudFront/WAF or Cloudflare), apply IP-level rate limiting (e.g., maximum 10 requests per minute per IP to `/api/v1/auth/login`).
   - **Layer 2 (API Gateway / Filter):** A token-bucket rate limiter using **Bucket4j** backed by Redis or an in-memory sliding window cache.
   - **Layer 3 (Account Lockout Policy):** Future feature to temporarily lock user accounts or introduce exponential backoff after multiple failed authentication attempts within a short window.
3. **Verification Requirement:**
   - Do not claim rate limiting is active until the implementation and its corresponding integration test suite are delivered.

---

## Getting Started

### 1. Prerequisites
- JDK 21 or later installed (`java -version`)
- Maven 3.8+ installed (`mvn -version`) or use the included `./mvnw.cmd` wrapper
- (Optional) Docker / Docker Compose for local PostgreSQL

---

### 2. Running Unit & Integration Tests
Tests run automatically against an isolated in-memory H2 database (PostgreSQL mode). No external database or Docker container is required.

From the `backend/` directory:
```bash
./mvnw test
```

Or from the workspace root:
```bash
./backend/mvnw.cmd -f backend/pom.xml test
```

---

### 3. Local Development with PostgreSQL

#### Option A: Using Docker Compose
Start a local PostgreSQL container:
```bash
cd backend
docker compose up -d
```
To stop the database:
```bash
docker compose down
```

#### Option B: Using Existing Local PostgreSQL
1. Create a database named `nexgradia_db`.
2. Configure credentials via environment variables or a `.env` file (see `.env.example`).

---

### 4. Running the Application
Set environment variables (or use defaults) and run:
```bash
cd backend
./mvnw spring-boot:run
```

Or run the compiled JAR:
```bash
./mvnw clean package -DskipTests
java -jar target/nexgradia-backend-0.0.1-SNAPSHOT.jar
```

---

### 5. Verification Endpoints
Once the application starts:
- **Custom Health Endpoint:** `http://localhost:8080/api/v1/health`
  - Response:
    ```json
    {
      "service": "nexgradia-backend",
      "status": "UP",
      "timestamp": "2026-10-21T05:10:00Z"
    }
    ```
- **Spring Actuator Health:** `http://localhost:8080/actuator/health`
  - Response:
    ```json
    {
      "status": "UP"
    }
    ```
