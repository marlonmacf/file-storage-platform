# File Storage Service

A Spring Boot + Angular application to upload and store arbitrary files (any type) in an H2 in-memory database as BLOBs. Goal is to experiment and include full testing coverage: architecture rules, unit tests, integration tests, and end-to-end tests with Cypress.

---

## Features

- **Any File Upload** via REST (`POST /files/upload`)
- **List Files** with metadata (`GET /files`)
- **Download File** by ID (`GET /files/{id}`)
- Store files in H2 using `@Lob` BLOB storage
- Layered architecture with best practices

---

## Architecture Overview

```
backend/
├── src/main/java/...
│   ├── controller/
│   │   └── FileUploadController.java ← (handles upload, list, download endpoints)
│   ├── service/
│   │   └── FileStorageService.java   ← (validates, transforms metadata, and saves files)
│   ├── repository/
│   │   └── FileRepository.java       ← (JPA repository for file persistence)
│   ├── domain/
│   │   └── StoredFile.java           ← (holds metadata + binary content (byte[]))
│   ├── dto/...
│   └── util/...                      ← (optional validators)
└── src/test/java/...
    ├── architecture/                 ← (ensures layered independence using ArchUnit)
    ├── integration/                  ← (Spring Boot + H2 using @SpringBootTest and @DataJpaTest)
    └── unit/                         ← (isolated component behavior via JUnit + Mockito)

frontend/
└── src/
    ├── app/...
    └── cypress/                      ← (E2E: Cypress tests HTTP endpoints + UI end-to-end flow)
```

---

## Testing Strategy

### 1. Architecture Rules

Using ArchUnit to enforce:

- Naming/package conventions are enforced (e.g., classes ending with `Controller`, residing in proper packages)
- Services & Repositories **do not** depend on Controllers
- Controllers **do not** directly access Repositories

### 2. Unit Tests

Isolated tests per component:

- **Controller** (`@WebMvcTest`): HTTP mappings, validation, error handling
- **Service** (Mockito + JUnit): metadata logic, exception cases
- **Repository** (`@DataJpaTest`): custom queries (if any)
- **Utility Classes** (JUnit): pure logic

### 3. Integration Tests

Real application context with H2 DB:

- **`@SpringBootTest`** – end-to-end file upload + download flow
- **`@DataJpaTest`** – verify BLOB storage and retrieval
- **MockMvc** – full HTTP route integration without UI

### 4. End-to-End (E2E)

Using Cypress to simulate:

- File upload via UI
- Files listing and download
- Validation of HTTP headers and file integrity

---

## Getting Started

### Prerequisites

- Java 17+ or your preferred Java version
- Maven 3.8+
- Node.js + npm (for Angular)
- Cypress (installed via npm)

### Backend Setup
   ```
   mvn clean install
   mvn spring-boot:run
   ```

App will start on `http://localhost:8080`.

### Frontend Setup

```
cd frontend
npm install
npm run start
```

Angular app runs on `http://localhost:4200`.

### Cypress (E2E)

```
cd frontend
npm run cypress:open
```

Run Cypress tests against `http://localhost:4200`.

---