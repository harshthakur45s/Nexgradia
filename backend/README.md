# Nexgradia Backend Foundation

Minimal Spring Boot backend foundation for **Nexgradia — Intelligent Campus Placement & Career Intelligence Platform**.

## Technology Stack
- **Language & Runtime:** Java 21 (compatible with Java 21+)
- **Framework:** Spring Boot 3.4.3
- **Build Tool:** Apache Maven
- **Database:** PostgreSQL (with Flyway migrations)
- **Monitoring/Health:** Spring Boot Actuator + Custom Health Endpoint (`/api/v1/health`)
- **Testing:** JUnit 5, MockMvc, H2 (for isolated unit/integration tests without running PostgreSQL)

---

## Getting Started

### 1. Prerequisites
- JDK 21 or later installed (`java -version`)
- Maven 3.8+ installed (`mvn -version`)
- (Optional) Docker / Docker Compose for local PostgreSQL

---

### 2. Running Unit & Integration Tests
Tests run automatically against an isolated in-memory H2 database (PostgreSQL mode). No external database or Docker container is required.

From the `backend/` directory:
```bash
mvn test
```

Or from the workspace root:
```bash
mvn -f backend/pom.xml test
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
mvn spring-boot:run
```

Or run the compiled JAR:
```bash
mvn clean package -DskipTests
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
