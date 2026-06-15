package com.LHSprojects.TCLHS.Controller;

import com.LHSprojects.TCLHS.Repository.AccountRepository;
import com.LHSprojects.TCLHS.Repository.LinkRepository;
import com.LHSprojects.TCLHS.Repository.Repository;
import com.LHSprojects.TCLHS.Repository.TutorRepository;
import com.LHSprojects.TCLHS.config.SessionAuthInterceptor;
import com.LHSprojects.TCLHS.service.Link;
import com.LHSprojects.TCLHS.model.Tutor;
import com.LHSprojects.TCLHS.model.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private Repository repository;

    @PostMapping(path = "/auth/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        if (request.email == null || request.email.isBlank() || request.password == null || request.password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }

        if (request.password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long.");
        }

        if (accountRepository.existsByEmail(request.email.trim().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
        }

        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            String hash = argon2.hash(2, 65536, 1, request.password.toCharArray());
            String userId = accountRepository.createUser(request.email.trim().toLowerCase(), hash);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(SessionAuthInterceptor.SESSION_USER_ID, userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(userId));
        } finally {
            argon2.wipeArray(request.password.toCharArray());
        }
    }

    @PostMapping(path = "/account/preferences", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePreferences(@RequestBody PreferencesRequest request, HttpSession session) {
        if (request.userId == null || request.userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId.");
        }
        requireValidUuid(request.userId, "userId");
        requireSelf(session, request.userId);
        if (request.firstName == null || request.firstName.isBlank() || request.lastName == null || request.lastName.isBlank() || request.gradeLevel == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name, last name, and grade level are required.");
        }

        String fullName = request.firstName.trim() + " " + request.lastName.trim();
        boolean updated = accountRepository.updatePreferences(
            request.userId,
            fullName,
            blankToNull(request.pronouns),
            blankToNull(request.bio),
            blankToNull(request.profilePicUrl),
            request.gradeLevel,
            request.subjects
        );

        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/tutor/setup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TutorSetupResponse> tutorSetup(@RequestBody TutorSetupRequest request, HttpSession session) {
        if (request.userId == null || request.userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId.");
        }
        requireValidUuid(request.userId, "userId");
        requireSelf(session, request.userId);
        if (request.firstName == null || request.firstName.isBlank() || request.lastName == null || request.lastName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First and last name are required.");
        }

        String fullName = request.firstName.trim() + " " + request.lastName.trim();
        String availabilityJson = serializeAvailability(request.availability);

        String tutorId = tutorRepository.createTutor(
            fullName,
            availabilityJson,
            request.courses,
            blankToNull(request.bio),
            blankToNull(request.profilePicUrl),
            request.gradeLevel,
            blankToNull(request.pronouns)
        );

        accountRepository.updateTutorId(request.userId, tutorId);

        // Also mirror basic account info
        if (request.gradeLevel != null || request.bio != null || request.pronouns != null) {
            try {
                accountRepository.updatePreferences(
                    request.userId, fullName,
                    blankToNull(request.pronouns), blankToNull(request.bio),
                    blankToNull(request.profilePicUrl), request.gradeLevel,
                    request.courses
                );
            } catch (Exception ignored) {}
        }

        // Add the new tutor to the in-memory cache
        Tutor tutor = new Tutor(tutorId, fullName, availabilityJson, 0, 0,
            request.courses != null ? request.courses : List.of(),
            blankToNull(request.bio), blankToNull(request.profilePicUrl),
            request.gradeLevel, blankToNull(request.pronouns));
        repository.saveTutor(tutor);

        return ResponseEntity.status(HttpStatus.CREATED).body(new TutorSetupResponse(tutorId));
    }

    @GetMapping(path = "/links/student/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> getStudentLinks(@PathVariable String studentId, HttpSession session) {
        requireValidUuid(studentId, "studentId");
        requireSelf(session, studentId);
        try {
            return ResponseEntity.ok(linkRepository.getLinksByStudentId(studentId));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load links.");
        }
    }

    @GetMapping(path = "/links/tutor/{tutorId}")
    public ResponseEntity<List<Map<String, Object>>> getTutorLinks(@PathVariable String tutorId, HttpSession session) {
        requireValidUuid(tutorId, "tutorId");
        UserAccount sessionUser = requireSessionUser(session);
        if (sessionUser.getTutorId() == null || !sessionUser.getTutorId().equals(tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized.");
        }
        try {
            return ResponseEntity.ok(linkRepository.getLinksByTutorId(tutorId));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load links.");
        }
    }

    @GetMapping(path = "/link/{linkId}")
    public ResponseEntity<Map<String, Object>> getLink(@PathVariable String linkId, HttpSession session) {
        requireValidUuid(linkId, "linkId");
        Map<String, Object> link = linkRepository.getLinkById(linkId);
        if (link == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found.");
        }
        requireLinkParticipant(session, (String) link.get("studentId"), (String) link.get("tutorId"));
        return ResponseEntity.ok(link);
    }

    @PostMapping(path = "/links", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> createLink(@RequestBody CreateLinkRequest request, HttpSession session) {
        String tutorId = request.tutorId;
        String studentId = request.studentId;
        if (tutorId == null || tutorId.isBlank() || studentId == null || studentId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing tutorId or studentId.");
        }
        requireValidUuid(tutorId, "tutorId");
        requireValidUuid(studentId, "studentId");
        requireSelf(session, studentId);
        if (tutorId.equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create a link between the same user.");
        }

        List<String> subjects = request.subjects != null ? request.subjects : List.of();
        String subject = String.join(", ", subjects);

        List<Map<String, String>> sessions = request.sessions != null ? request.sessions : List.of();

        String studentName = "Student";
        try {
            UserAccount stu = accountRepository.findById(studentId);
            if (stu != null && stu.getName() != null) studentName = stu.getName();
        } catch (Exception ignored) {}

        Link link = new Link(studentId, tutorId, subject, request.details);
        link.proposeMeet(sessions, request.message, "student");
        repository.saveLink(link);
        linkRepository.saveLink(link);

        Map<String,Object> resp = Map.of(
            "requestId", link.getId(),
            "studentId", studentId,
            "studentName", studentName,
            "subject", subject,
            "details", request.details,
            "sessions", sessions,
            "message", request.message,
            "status", "pending"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping(path = "/links/{requestId}/accept")
    public ResponseEntity<?> acceptLinkEndpoint(@PathVariable String requestId, HttpSession session) {
        requireValidUuid(requestId, "requestId");
        // Try memory cache first, then load from DB if needed
        Link link = requestId != null ? repository.getLink(requestId) : null;
        if (link == null && requestId != null) {
            Map<String, Object> dbLink = linkRepository.getLinkById(requestId);
            if (dbLink != null) {
                link = new Link(
                    (String) dbLink.get("studentId"), 
                    (String) dbLink.get("tutorId"), 
                    "", 
                    (String) dbLink.get("details")
                );
                link.setId((String) dbLink.get("id"));
                link.setStatus((String) dbLink.get("status"));
                if (dbLink.get("suggestedTime") != null) {
                    try {
                        link.setSessions(objectMapper.readValue((String) dbLink.get("suggestedTime"), List.class));
                    } catch (Exception ignored) {}
                }
            }
        }
        if (link != null) {
            requireLinkParticipant(session, link.getStudentId(), link.getTutorId());
            link.acceptMeet();
            repository.saveLink(link);
            linkRepository.saveLink(link);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/links/{requestId}/reject")
    public ResponseEntity<?> rejectLinkEndpoint(@PathVariable String requestId, HttpSession session) {
        requireValidUuid(requestId, "requestId");
        // Try memory cache first, then load from DB if needed
        Link link = requestId != null ? repository.getLink(requestId) : null;
        if (link == null && requestId != null) {
            Map<String, Object> dbLink = linkRepository.getLinkById(requestId);
            if (dbLink != null) {
                link = new Link(
                    (String) dbLink.get("studentId"), 
                    (String) dbLink.get("tutorId"), 
                    "", 
                    (String) dbLink.get("details")
                );
                link.setId((String) dbLink.get("id"));
                link.setStatus((String) dbLink.get("status"));
                if (dbLink.get("suggestedTime") != null) {
                    try {
                        link.setSessions(objectMapper.readValue((String) dbLink.get("suggestedTime"), List.class));
                    } catch (Exception ignored) {}
                }
            }
        }
        if (link != null) {
            requireLinkParticipant(session, link.getStudentId(), link.getTutorId());
            link.cancelMeet();
            repository.saveLink(link);
            linkRepository.saveLink(link);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/links/{linkId}/suggest", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> suggestTimeEndpoint(@PathVariable String linkId, @RequestBody SuggestTimeRequest request, HttpSession session) {
        requireValidUuid(linkId, "linkId");
        // Try memory cache first, then load from DB if needed
        Link link = linkId != null ? repository.getLink(linkId) : null;
        if (link == null && linkId != null) {
            Map<String, Object> dbLink = linkRepository.getLinkById(linkId);
            if (dbLink != null) {
                link = new Link(
                    (String) dbLink.get("studentId"), 
                    (String) dbLink.get("tutorId"), 
                    "", 
                    (String) dbLink.get("details")
                );
                link.setId((String) dbLink.get("id"));
                link.setStatus((String) dbLink.get("status"));
                if (dbLink.get("suggestedTime") != null) {
                    try {
                        link.setSessions(objectMapper.readValue((String) dbLink.get("suggestedTime"), List.class));
                    } catch (Exception ignored) {}
                }
            }
        }
        if (link == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found.");
        requireLinkParticipant(session, link.getStudentId(), link.getTutorId());

        List<Map<String, String>> sessions = request.sessions != null ? request.sessions : List.of();
        link.proposeMeet(sessions, request.message, request.suggestedBy != null ? request.suggestedBy : "student");
        repository.saveLink(link);
        linkRepository.saveLink(link);
        return ResponseEntity.ok().build();
    }

    private static final java.util.regex.Pattern UUID_PATTERN =
        java.util.regex.Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private void requireValidUuid(String value, String fieldName) {
        if (value == null || !UUID_PATTERN.matcher(value).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName + ".");
        }
    }

    // ── Session helpers ───────────────────────────────────────
    // SessionAuthInterceptor guarantees a session with a userId attribute exists
    // for any request that reaches these controller methods.

    private String requireSessionUserId(HttpSession session) {
        String userId = (String) session.getAttribute(SessionAuthInterceptor.SESSION_USER_ID);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }
        return userId;
    }

    private UserAccount requireSessionUser(HttpSession session) {
        String userId = requireSessionUserId(session);
        try {
            return accountRepository.findById(userId);
        } catch (Exception ex) {
            session.invalidate();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is no longer valid.");
        }
    }

    private void requireSelf(HttpSession session, String userId) {
        if (!requireSessionUserId(session).equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized.");
        }
    }

    private void requireLinkParticipant(HttpSession session, String studentId, String tutorId) {
        String sessionUserId = requireSessionUserId(session);
        if (sessionUserId.equals(studentId)) {
            return;
        }
        UserAccount user = requireSessionUser(session);
        if (user.getTutorId() != null && user.getTutorId().equals(tutorId)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized for this link.");
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
    private String serializeAvailability(Object availability) {
        if (availability == null) {
            return null;
        }
        if (availability instanceof String) {
            return (String) availability;
        }
        try {
            return objectMapper.writeValueAsString(availability);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to serialize availability", ex);
        }
    }
    // ── Request / Response DTOs ──────────────────────────────

    public static class RegisterRequest {
        public String email;
        public String password;
    }

    public static class RegisterResponse {
        public String userId;
        public RegisterResponse(String userId) { this.userId = userId; }
    }

    public static class PreferencesRequest {
        public String userId;
        public String firstName;
        public String lastName;
        public Integer gradeLevel;
        public String profilePicUrl;
        public String pronouns;
        public String bio;
        public List<String> subjects;
    }

    public static class TutorSetupRequest {
        public String userId;
        public String firstName;
        public String lastName;
        public Integer gradeLevel;
        public String profilePicUrl;
        public String pronouns;
        public String bio;
        public List<String> courses;
        public Object availability;
    }

    public static class TutorSetupResponse {
        public String tutorId;
        public TutorSetupResponse(String tutorId) { this.tutorId = tutorId; }
    }

    public static class CreateLinkRequest {
        public String tutorId;
        public String studentId;
        public List<String> subjects;
        public String details;
        public List<Map<String, String>> sessions;
        public String message;
    }

    public static class SuggestTimeRequest {
        public List<Map<String, String>> sessions;
        public String message;
        public String suggestedBy;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class LoginResponse {
        public String userId;
        public String tutorId;
        public boolean hasPreferences;
        public boolean isTutor;

        public LoginResponse(String userId, String tutorId, boolean hasPreferences, boolean isTutor) {
            this.userId = userId;
            this.tutorId = tutorId;
            this.hasPreferences = hasPreferences;
            this.isTutor = isTutor;
        }
    }

    public static class AccountResponse {
        public String userId;
        public String name;
        public String email;
        public Integer gradeLevel;
        public String pronouns;
        public String bio;
        public String profilePic;
        public List<String> subjects;
        public String tutorId;
        public boolean isTutor;

        public AccountResponse() {}
    }

    public static class TutorProfileResponse {
        public String id;
        public String name;
        public String bio;
        public List<String> courses;
        public String availability;
        public Integer gradeLevel;
        public String pronouns;
        public String profilePhotoUrl;
        public int rating;
        public int numRatings;
        public TutorProfileResponse() {}
    }

    public static class TutorUpdateRequest {
        public String tutorId;
        public String userId;
        public String firstName;
        public String lastName;
        public Integer gradeLevel;
        public String profilePicUrl;
        public String pronouns;
        public String bio;
        public List<String> courses;
        public Object availability;
    }

    public static class RateRequest {
        public String tutorId;
        public String linkId;
        public int rating;
    }

    public static class AccountUpdateRequest {
        public String userId;
        public String firstName;
        public String lastName;
        public String email;
        public Integer gradeLevel;
        public String profilePicUrl;
        public String currentPassword;
        public String newPassword;
    }

    @PostMapping(path = "/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        if (request.email == null || request.email.isBlank() || request.password == null || request.password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }

        UserAccount user = accountRepository.findByEmail(request.email.trim().toLowerCase());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            boolean ok = argon2.verify(user.getPassword(), request.password.toCharArray());
            if (!ok) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
            }

            // Avoid session fixation: discard any pre-existing (anonymous) session and start fresh.
            HttpSession oldSession = httpRequest.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(SessionAuthInterceptor.SESSION_USER_ID, user.getId());

            boolean hasPrefs = user.getName() != null && user.getGradeLevel() != null;
            boolean isTutor = user.getTutorId() != null && tutorRepository.findById(user.getTutorId()) != null;
            return ResponseEntity.ok(new LoginResponse(user.getId(), user.getTutorId(), hasPrefs, isTutor));
        } finally {
            argon2.wipeArray(request.password.toCharArray());
        }
    }

    @PostMapping(path = "/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/tutor/{tutorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TutorProfileResponse> getTutorProfile(@PathVariable String tutorId) {
        requireValidUuid(tutorId, "tutorId");
        Tutor tutor = tutorRepository.findById(tutorId);
        if (tutor == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutor not found.");
        TutorProfileResponse resp = new TutorProfileResponse();
        resp.id             = tutor.getId();
        resp.name           = tutor.getName();
        resp.bio            = tutor.getBio();
        resp.courses        = tutor.getCourses();
        resp.availability   = tutor.getAvailability();
        resp.gradeLevel     = tutor.getGradeLevel();
        resp.pronouns       = tutor.getPronouns();
        resp.profilePhotoUrl = tutor.getProfilePhotoUrl();
        resp.rating         = tutor.getRating();
        resp.numRatings     = tutor.getNumRatings();
        return ResponseEntity.ok(resp);
    }

    @GetMapping(path = "/tutors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TutorProfileResponse>> getAllTutors() {
        List<Tutor> tutors = repository.getAllTutors();
        List<TutorProfileResponse> resp = tutors.stream().map(tutor -> {
            TutorProfileResponse r = new TutorProfileResponse();
            r.id = tutor.getId(); r.name = tutor.getName(); r.bio = tutor.getBio(); r.courses = tutor.getCourses();
            r.availability = tutor.getAvailability(); r.gradeLevel = tutor.getGradeLevel(); r.pronouns = tutor.getPronouns();
            r.profilePhotoUrl = tutor.getProfilePhotoUrl(); r.rating = tutor.getRating(); r.numRatings = tutor.getNumRatings();
            return r;
        }).toList();
        return ResponseEntity.ok(resp);
    }

    @PostMapping(path = "/tutor/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateTutor(@RequestBody TutorUpdateRequest request, HttpSession session) {
        if (request.tutorId == null || request.tutorId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing tutorId.");
        }
        requireValidUuid(request.tutorId, "tutorId");
        if (request.userId != null && !request.userId.isBlank()) {
            requireValidUuid(request.userId, "userId");
            requireSelf(session, request.userId);
        }
        UserAccount sessionUser = requireSessionUser(session);
        if (sessionUser.getTutorId() == null || !sessionUser.getTutorId().equals(request.tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized.");
        }
        if (request.firstName == null || request.firstName.isBlank() || request.lastName == null || request.lastName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First and last name are required.");
        }

        String fullName        = request.firstName.trim() + " " + request.lastName.trim();
        String availabilityJson = serializeAvailability(request.availability);

        tutorRepository.upsertTutor(
            request.tutorId, fullName, availabilityJson,
            request.courses, blankToNull(request.bio),
            blankToNull(request.profilePicUrl), request.gradeLevel, blankToNull(request.pronouns)
        );

        // Mirror name/grade/pronouns/pic back to account
        if (request.userId != null && !request.userId.isBlank()) {
            try {
                accountRepository.updatePreferences(
                    request.userId, fullName, blankToNull(request.pronouns), blankToNull(request.bio),
                    blankToNull(request.profilePicUrl), request.gradeLevel, request.courses
                );
            } catch (Exception ignored) {}
        }

        // Refresh in-memory cache from DB
        Tutor fresh = tutorRepository.findById(request.tutorId);
        if (fresh != null) repository.saveTutor(fresh);

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/tutor/rate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> rateTutor(@RequestBody RateRequest request, HttpSession session) {
        if (request.tutorId == null || request.tutorId.isBlank() || request.linkId == null || request.linkId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing tutorId or linkId.");
        }
        requireValidUuid(request.tutorId, "tutorId");
        requireValidUuid(request.linkId, "linkId");
        if (request.rating < 1 || request.rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5.");
        }

        Map<String, Object> link = linkRepository.getLinkById(request.linkId);
        if (link == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found.");
        }
        String sessionUserId = requireSessionUserId(session);
        if (!sessionUserId.equals(link.get("studentId")) || !request.tutorId.equals(link.get("tutorId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized.");
        }

        tutorRepository.addRating(request.tutorId, request.rating);
        linkRepository.updateLinkStatus(request.linkId, "completed");

        Tutor cached = repository.getTutor(request.tutorId);
        if (cached != null) {
            cached.addRating(request.rating);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/account/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAccount(@RequestBody AccountUpdateRequest request, HttpSession session) {
        if (request.userId == null || request.userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId.");
        }
        requireValidUuid(request.userId, "userId");
        requireSelf(session, request.userId);
        if (request.firstName == null || request.firstName.isBlank() || request.lastName == null || request.lastName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First and last name are required.");
        }
        if (request.email == null || request.email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }
        if (request.newPassword != null && !request.newPassword.isBlank() && request.newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long.");
        }

        String fullName = request.firstName.trim() + " " + request.lastName.trim();
        String newEmail = request.email.trim().toLowerCase();

        UserAccount currentUser;
        try {
            currentUser = accountRepository.findById(request.userId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
        if (!newEmail.equals(currentUser.getEmail()) && accountRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use.");
        }

        boolean updated = accountRepository.updateAccount(
            request.userId,
            fullName,
            newEmail,
            request.gradeLevel,
            blankToNull(request.profilePicUrl)
        );

        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }

        // If this account is linked to an existing tutor record, mirror the changed name/profile to it.
        // Do NOT create a new Tutors row for accounts that aren't actually tutors.
        try {
            if (currentUser.getTutorId() != null) {
                String tutorId = currentUser.getTutorId();
                Tutor existing = tutorRepository.findById(tutorId);
                if (existing != null) {
                    tutorRepository.upsertTutor(
                        tutorId, fullName, existing.getAvailability(), existing.getCourses(),
                        blankToNull(existing.getBio()), blankToNull(request.profilePicUrl),
                        request.gradeLevel, existing.getPronouns()
                    );

                    // Refresh in-memory cache
                    Tutor fresh = tutorRepository.findById(tutorId);
                    if (fresh != null) repository.saveTutor(fresh);
                }
            }
        } catch (Exception ignored) {}

        if (request.newPassword != null && !request.newPassword.isBlank()) {
            if (request.currentPassword == null || request.currentPassword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is required to set a new password.");
            }
            Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
            try {
                if (!argon2.verify(currentUser.getPassword(), request.currentPassword.toCharArray())) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect.");
                }
                String newHash = argon2.hash(2, 65536, 1, request.newPassword.toCharArray());
                accountRepository.updatePassword(request.userId, newHash);
            } finally {
                argon2.wipeArray(request.currentPassword.toCharArray());
                argon2.wipeArray(request.newPassword.toCharArray());
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/account/{userId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String userId, HttpSession session) {
        requireValidUuid(userId, "userId");
        requireSelf(session, userId);
        try {
            UserAccount user = accountRepository.findById(userId);
            AccountResponse resp = new AccountResponse();
            resp.userId = user.getId();
            resp.name = user.getName();
            resp.email = user.getEmail();
            resp.gradeLevel = user.getGradeLevel();
            resp.pronouns = user.getPronouns();
            resp.bio = user.getBio();
            resp.profilePic = user.getProfilePic();
            resp.subjects = user.getSubjects();
            resp.tutorId = user.getTutorId();
            resp.isTutor = user.getTutorId() != null && tutorRepository.findById(user.getTutorId()) != null;
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
    }

    @DeleteMapping(path = "/account/{userId}")
    public ResponseEntity<?> deleteAccount(@PathVariable String userId, HttpSession session) {
        if (userId == null || userId.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId.");
        requireValidUuid(userId, "userId");
        requireSelf(session, userId);
        try {
            UserAccount user = accountRepository.findById(userId);
            if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            // If user has a tutor profile, remove tutor record and cache
            String tutorId = user.getTutorId();
            if (tutorId != null) {
                tutorRepository.upsertTutor(tutorId, "", null, List.of(), null, null, null, null);
                repository.deleteTutor(tutorId);
            }
            boolean ok = accountRepository.deleteAccount(userId);
            if (!ok) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete account.");
            session.invalidate();
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) { throw ex; }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete account."); }
    }
}

