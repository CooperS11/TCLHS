# TCLHS — Tutor Connection for Lincoln High School Students

A web application that connects students with tutors based on shared courses and ratings.

## Tech Stack

- **Backend:** Java 21 + Spring Boot 4.0.2
- **Messaging:** WebSocket (STOMP over SockJS)
- **Frontend:** HTML5, CSS3, vanilla JavaScript
- **Build:** Maven 3.9
- **Container:** Docker (multi-stage build)

## Getting Started

### Prerequisites

- Java 21 JDK
- Maven 3.9+ (or use the included wrapper)

### Run Locally

```bash
./mvnw spring-boot:run
```

Then open [http://localhost:8080](http://localhost:8080).

### Build & Run JAR

```bash
./mvnw clean package -DskipTests
java -jar target/TCLHS-0.0.1-SNAPSHOT.jar
```

### Run with Docker

```bash
docker build -t tclhs .
docker run -p 8080:8080 tclhs
```

## Project Structure

```
src/main/java/com/LHSprojects/TCLHS/
├── TclhsApplication.java       # Entry point
├── config/
│   └── WebSocketConfig.java    # STOMP/SockJS configuration
├── model/
│   ├── Account.java            # Base user model
│   ├── Student.java            # Student profile + course list
│   └── Tutor.java              # Tutor profile + rating system
├── service/
│   └── TutorMatcher.java       # Course-overlap matching algorithm
└── Repository/
    └── Repository.java         # Thread-safe in-memory tutor cache

src/main/resources/static/
├── index.html                  # Landing page
├── login.html                  # Sign in
├── register.html               # Account creation
├── student-home.html           # Student dashboard
├── tutor-home.html             # Tutor dashboard
├── browse-tutors.html          # Browse/search tutors
└── view-tutor.html             # Individual tutor profile
```

## Features

- **Tutor matching** — finds tutors whose courses overlap with a student's schedule
- **Rating system** — tutors accumulate ratings with a running average
- **Real-time messaging** — WebSocket infrastructure (STOMP + SockJS) ready for chat
- **In-memory data store** — `ConcurrentHashMap`-backed repository (database integration planned)

## WebSocket Endpoints

| Endpoint | Description |
|----------|-------------|
| `/ws` | WebSocket connection (SockJS fallback supported) |
| `/app/**` | Client-to-server messages |
| `/topic/**` | Server broadcast topics |

## Contributing

This project is in early development. Contributions welcome — open an issue or submit a pull request.
