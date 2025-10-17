# 🎬 GIC Cinemas — Runbook (Gradle)

A complete Java-based cinema booking system consisting of a **Spring Boot backend** and a **CLI frontend**.

---

## 🧩 Project Overview

```
backend/   → Spring Boot app (CinemaApplication)
cli/       → Terminal client using java.net.http
common/    → Shared DTOs
```

The backend serves a REST API under `/api`, and the CLI consumes it for seat booking operations.

---

## ⚙️ Prerequisites

- **JDK 17+**
- **Gradle 8+** (or use the included `./gradlew` wrapper)
- database — default is in-memory H2.

Default backend base URL:  
**http://localhost:8080/api**

---

## 🚀 Quick Start

### 1️⃣ Start the backend (development mode)
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=dev'
```

### 2️⃣ Start the CLI (in a new terminal)
run directly from source:
```bash
./gradlew :cli:run --args='--api.base=http://localhost:8080/api'
```

---

## 🖥 Backend

### ▶️ Run with hot reload
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=dev'
```

### 📦 Build a runnable JAR
```bash
./gradlew :backend:build -x test
java -jar backend/build/libs/backend-*.jar --spring.profiles.active=dev
```

### ⚙️ Common options
| Option | Example | Description |
|--------|----------|-------------|
| `--server.port` | `--server.port=9090` | Run on a different port |
| `--server.servlet.context-path` | `--server.servlet.context-path=/api` | Set base API path |
| `--spring.profiles.active` | `--spring.profiles.active=dev` | Use a specific Spring profile |

---

### 📡 API Endpoints

| Method | Endpoint | Description |
|--------|-----------|-------------|
| `POST` | `/api/seating-config` | Create or find a seating layout |
| `POST` | `/api/booking/reserve` | Reserve seats (temporary hold) |
| `POST` | `/api/booking/change-booking` | Change seat allocation |
| `POST` | `/api/booking/confirm/{bookingId}` | Confirm a booking |
| `GET`  | `/api/booking/check/{bookingId}` | View confirmed booking |

Error responses are standardized via `GlobalExceptionHandler`:
```json
{
  "status": 400,
  "error": "No Available Seats",
  "message": "Only 2 seats left for Inception"
}
```

---

## 💻 CLI

The CLI connects to the backend using Java’s `HttpClient`.  
It guides users through:
- Creating or finding seat maps
- Reserving seats
- Adjusting seat positions
- Confirming bookings
- Viewing booking details

### ▶️ Run
From project root:
```bash
./gradlew :cli:run --args='--api.base=http://localhost:8080/api'
```

Or from the installed binary:
```bash
./gradlew :cli:installDist
./cli/build/install/cli/bin/cli --api.base=http://localhost:8080/api
```

### CLI Options
| Flag | Example | Description |
|------|----------|-------------|
| `--api.base` | `--api.base=http://localhost:8080/api` | Backend base URL (required) |

---

## 🧪 Running Tests

Run all tests:
```bash
./gradlew test
```

Run backend-only tests:
```bash
./gradlew :backend:test
```

> Integration tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)` and `TestRestTemplate`.

---

## 🩺 Troubleshooting

**❗ Empty error body during tests**  
Use `ResponseEntity<String>` and deserialize JSON manually.

**❗ GlobalExceptionHandler not triggered**  
Ensure it’s in the same package tree as `CinemaApplication` and no controller catches exceptions.

**❗ CLI shows "Booking Not Found"**  
Means backend returned `404` with:
```json
{ "error": "Booking Not Found" }
```

**❗ Port already in use**  
Change backend port:
```bash
./gradlew :backend:bootRun --args='--server.port=9090'
```
Then run CLI with:
```bash
./gradlew :cli:run --args='--api.base=http://localhost:9090/api'
```

---

## 🎞 Example Workflow

1. Start backend:  
   `./gradlew :backend:bootRun`

2. Start CLI:  
   `./gradlew :cli:run --args='--api.base=http://localhost:8080/api'`

3. In CLI:
    - Enter movie title, rows, and seats per row.
    - Choose number of tickets to book.
    - Adjust seat if needed.
    - Confirm booking.
    - Check your booking via booking ID.

---

## 🧱 Build Artifacts

| Module | Output | Description |
|--------|---------|-------------|
| `backend` | `backend/build/libs/backend-*.jar` | Spring Boot app |
| `cli` | `cli/build/install/cli/bin/cli` | Runnable CLI binary |
| `common` | Shared classes | Used by both modules |

---

### ✅ Summary Commands

```bash
# Start backend
./gradlew :backend:bootRun

# Start CLI
./gradlew :cli:run --args='--api.base=http://localhost:8080/api'

# Build JARs
./gradlew build -x test

# Run backend JAR
java -jar backend/build/libs/backend-*.jar
```
