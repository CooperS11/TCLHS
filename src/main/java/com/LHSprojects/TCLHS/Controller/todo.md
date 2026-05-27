# Backend TODO

## Auth — REST

| Endpoint | Method | Request Body | Notes |
|---|---|---|---|
| `/api/auth/register` | POST | `{ email, password }` | Create new account, return session/token |
| `/api/auth/login` | POST | `{ email, password }` | Authenticate, return session/token |

---

## Account — REST + WebSocket

| Endpoint / Destination | Type | Request | Response / Broadcast |
|---|---|---|---|
| `/app/getAccountSettings` | STOMP send | — | Broadcast to `/topic/account/settings`: `{ firstName, lastName, email, gradeLevel, role, profilePhotoUrl }` |
| `/api/account/update` | POST | `{ firstName, lastName, email, gradeLevel, currentPassword, newPassword, accountType, profilePhoto }` | Updated account |

---

## Tutors — WebSocket

| Destination | Type | Request | Response / Broadcast |
|---|---|---|---|
| `/app/getTutors` | STOMP send | — | Broadcast to `/topic/tutors`: `[{ id, name, availability, courses, rating, numRatings }]` |
| `/app/getTutor` | STOMP send | `{ id: tutorId }` | Broadcast to `/topic/tutor`: `{ id, firstName, lastName, gender, gradeLevel, bio, courses, completedCourses, email, personalEmail, profilePhotoUrl }` |
| `/app/getTutorProfile` | STOMP send | — | Broadcast to `/topic/tutor/profile`: `{ name, bio, courses, availability, rating, numRatings, profilePhotoUrl }` |

---

## Tutor Profile Editing — REST

| Endpoint | Method | Request Body | Notes |
|---|---|---|---|
| `/api/tutors/{tutorId}` | PATCH | `{ bio }` or `{ courses }` or `{ availability }` | Inline edit fields on tutor home page |

---

## Session Requests (Links) — WebSocket

| Destination | Type | Request | Response / Broadcast |
|---|---|---|---|
| `/app/createLink` | STOMP send | `{ tutorId, subject, details, date, time, message }` | Push to `/topic/link/{tutorId}`: `{ requestId, subject, studentName, date, time, details, message, status: "pending" }` |
| `/app/acceptLink` | STOMP send | `{ requestId, tutorId }` | Push to `/topic/link/response/{tutorId}`: `{ status: "accepted" }` |
| `/app/rejectLink` | STOMP send | `{ requestId, tutorId }` | Push to `/topic/link/response/{tutorId}`: `{ status: "rejected" }` |

---

## Student Tutors — WebSocket + REST

| Endpoint / Destination | Type | Request | Response / Broadcast |
|---|---|---|---|
| `/app/getStudentTutors` | STOMP send | — | Broadcast to `/topic/student/tutors`: `[{ id, name, subject, status, review }]` |
| `/api/students/{studentId}/tutors/{tutorId}` | DELETE | — | Remove a tutor from student's list |

---

**Total: 4 REST endpoints, 7 STOMP message handlers, 5 STOMP broadcast topics.**
