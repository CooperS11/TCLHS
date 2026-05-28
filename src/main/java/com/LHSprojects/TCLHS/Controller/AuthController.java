package com.LHSprojects.TCLHS.Controller;

import com.LHSprojects.TCLHS.Repository.AccountRepository;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AccountRepository accountRepository;

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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public static class RegisterRequest {
        public String email;
        public String password;
    }

    public static class RegisterResponse {
        public String userId;

        public RegisterResponse(String userId) {
            this.userId = userId;
        }
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

    // --- Login / account endpoints ---
    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class LoginResponse {
        public String userId;
        public boolean hasPreferences;

        public LoginResponse(String userId, boolean hasPreferences) {
            this.userId = userId;
            this.hasPreferences = hasPreferences;
        }
    }

    public static class AccountResponse {
        public String userId;
        public String name;
        public Integer gradeLevel;
        public String pronouns;
        public String bio;
        public String profilePic;
        public List<String> subjects;

        public AccountResponse() {}
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
            return ResponseEntity.ok(new LoginResponse(user.getId(), hasPrefs));
        } finally {
            argon2.wipeArray(request.password.toCharArray());
        }
    }

    @GetMapping(path = "/account/{userId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String userId) {
        try {
            UserAccount user = accountRepository.findById(userId);
            AccountResponse resp = new AccountResponse();
            resp.userId = user.getId();
            resp.name = user.getName();
            resp.gradeLevel = user.getGradeLevel();
            resp.pronouns = user.getPronouns();
            resp.bio = user.getBio();
            resp.profilePic = user.getProfilePic();
            resp.subjects = user.getSubjects();
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
    }
}
