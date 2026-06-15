# TCLHS — Claude Project Context

## What this is
Peer tutoring platform for a high school. Students find and request sessions with tutors; tutors accept/reject; completed sessions can be rated.

## Tech stack
- **Backend:** Java Spring Boot 4.0.2, Spring JDBC (`JdbcTemplate` — no JPA), Argon2id passwords
- **Database:** PostgreSQL on Supabase (`application.properties` has the URL)
- **Frontend:** Vanilla HTML/CSS/JS — no framework. Static files served by Spring Boot from `src/main/resources/static/`
- **Real-time:** WebSocket/STOMP via SockJS (`/ws` endpoint)
- **Auth:** `authentication.js` exports an `Auth` module; state in localStorage

## Key rules
- **Server restart required** after any Java change. HTML/JS update on browser reload.
- **Port 8081** is the default. If the user can't start the server, check for a stale process: `netstat -ano | findstr :8081` then `Stop-Process -Id <pid> -Force`.
- **Never start a background Spring Boot server** during diagnosis — it occupies the port and blocks the user's server.
- **`isTutor`** must be determined by `Auth.getTutorId() === link.tutorId`, not by role string. Role 'both' breaks role-string checks.
- **Tutor cache** (`ConcurrentHashMap` in `Repository.java`) must be refreshed after DB updates: re-fetch from DB and call `repository.saveTutor(fresh)`.

## Domain model
| Table | Key columns |
|-------|------------|
| `Private Accounts` | userId (UUID), name, email, password, gradeLevel, profilePic, role, tutorId |
| `Tutors` | id (UUID), name, availability (JSON), rating, numRatings, courses (JSON), bio, profilePicture, grade, pronouns |
| Links | id, tutorId, studentId, status (pending→accepted→completed/rejected), subject, details, sessions (JSON) |

**Availability JSON format:** `{"Dates": {"Monday": [[9.0, 17.0]]}}` — decimal hours

## Features already built (as of 2026-06-14)
- Account settings page (save name/email/grade/pic/password) → `POST /api/account/update`
- Contact email card on accepted links (student sees tutor email; tutor sees student email)
- End Session & Rate Tutor flow (1–5 stars, atomic SQL average) → `POST /api/tutor/rate`
- Edit Tutor Profile (`tutor-setup.html?edit=true`, autofill from DB) → `POST /api/tutor/update`
- `GET /api/tutor/{tutorId}` endpoint returning full tutor profile

## Known technical debt
- DB password hardcoded in `application.properties` — should use env vars / `.env`
- Schema changes done via `@PostConstruct` ALTER TABLE — should migrate to Flyway
- No unit or integration tests
- No CI/CD pipeline
- Stale git worktrees in `.claude/worktrees/` (safe to delete)

## Memory
Full session history: `.claude/memory/MEMORY.md`
