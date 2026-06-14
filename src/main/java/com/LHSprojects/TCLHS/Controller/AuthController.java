package com.LHSprojects.TCLHS.Controller;

import com.LHSprojects.TCLHS.Repository.AccountRepository;
import com.LHSprojects.TCLHS.Repository.LinkRepository;
import com.LHSprojects.TCLHS.Repository.Repository;
import com.LHSprojects.TCLHS.Repository.TutorRepository;
import com.LHSprojects.TCLHS.model.Tutor;
import com.LHSprojects.TCLHS.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        if (request.email == null || request.email.isBlank() || request.password == null || request.password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }

        if (accountRepository.existsByEmail(request.email.trim().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
        }

        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            String hash = argon2.hash(2, 65536, 1, request.password.toCharArray());
            String userId = accountRepository.createUser(request.email.trim().toLowerCase(), hash);
            return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(userId));
        } finally {
            argon2.wipeArray(request.password.toCharArray());
        }
    }

    @PostMapping(path = "/account/preferences", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePreferences(@RequestBody PreferencesRequest request) {
        if (request.userId == null || request.userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId.");
        }
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
    public ResponseEntity<TutorSetupResponse> tutorSetup(@RequestBody TutorSetupRequest request) {
        if (request.userId == null || request.userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId.");
        }
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
    public ResponseEntity<List<Map<String, Object>>> getStudentLinks(@PathVariable String studentId) {
        try {
            return ResponseEntity.ok(linkRepository.getLinksByStudentId(studentId));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load links.");
        }
    }

    @GetMapping(path = "/links/tutor/{tutorId}")
    public ResponseEntity<List<Map<String, Object>>> getTutorLinks(@PathVariable String tutorId) {
        try {
            return ResponseEntity.ok(linkRepository.getLinksByTutorId(tutorId));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load links.");
        }
    }

    @GetMapping(path = "/link/{linkId}")
    public ResponseEntity<Map<String, Object>> getLink(@PathVariable String linkId) {
        Map<String, Object> link = linkRepository.getLinkById(linkId);
        if (link == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found.");
        }
        return ResponseEntity.ok(link);
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

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class LoginResponse {
        public String userId;
        public String tutorId;
        public boolean hasPreferences;

        public LoginResponse(String userId, String tutorId, boolean hasPreferences) {
            this.userId = userId;
            this.tutorId = tutorId;
            this.hasPreferences = hasPreferences;
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

        public AccountResponse() {}
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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
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

            boolean hasPrefs = user.getName() != null && user.getGradeLevel() != null;
            return ResponseEntity.ok(new LoginResponse(user.getId(), user.getTutorId(), hasPrefs));
        } finally {
            argon2.wipeArray(request.password.toCharArray());
        }
    }

    @PostMapping(path = "/tutor/rate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> rateTutor(@RequestBody RateRequest request) {
        if (request.tutorId == null || request.tutorId.isBlank() || request.linkId == null || request.linkId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing tutorId or linkId.");
        }
        if (request.rating < 1 || request.rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5.");
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
    public ResponseEntity<?> updateAccount(@RequestBody AccountUpdateRequest request) {
        if (request.userId == null || request.userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId.");
        }
        if (request.firstName == null || request.firstName.isBlank() || request.lastName == null || request.lastName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First and last name are required.");
        }
        if (request.email == null || request.email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        String fullName = request.firstName.trim() + " " + request.lastName.trim();
        boolean updated = accountRepository.updateAccount(
            request.userId,
            fullName,
            request.email.trim().toLowerCase(),
            request.gradeLevel,
            blankToNull(request.profilePicUrl)
        );

        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }

        if (request.newPassword != null && !request.newPassword.isBlank()) {
            if (request.currentPassword == null || request.currentPassword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is required to set a new password.");
            }
            UserAccount user = accountRepository.findById(request.userId);
            Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
            try {
                if (!argon2.verify(user.getPassword(), request.currentPassword.toCharArray())) {
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
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String userId) {
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
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
    }
}
