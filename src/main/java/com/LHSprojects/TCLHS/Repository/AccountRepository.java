package com.LHSprojects.TCLHS.Repository;

import com.LHSprojects.TCLHS.model.UserAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class AccountRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS \"Private Accounts\" (
                \"UserID\" UUID PRIMARY KEY,
                \"created_at\" timestamptz NOT NULL DEFAULT now(),
                \"Email\" text NOT NULL UNIQUE,
                \"Password\" text NOT NULL,
                \"Name\" text,
                \"Pronouns\" text,
                \"Bio\" text,
                \"Subjects\" json,
                \"ProfilePic\" text,
                \"GradeLevel\" int
            )
        """;
        jdbcTemplate.execute(sql);
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM \"Private Accounts\" WHERE \"Email\" = ?",
            Integer.class,
            email
        );
        return count != null && count > 0;
    }

    public String createUser(String email, String passwordHash) {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO \"Private Accounts\" (\"UserID\", \"Email\", \"Password\") VALUES (CAST(? AS UUID), ?, ?)";
        jdbcTemplate.update(sql, id, email, passwordHash);
        return id;
    }

    public boolean updatePreferences(String userId, String name, String pronouns, String bio, String profilePic, Integer gradeLevel, List<String> subjects) {
        try {
            String subjectsJson = subjects != null ? objectMapper.writeValueAsString(subjects) : null;
            String sql = "UPDATE \"Private Accounts\" SET \"Name\" = ?, \"Pronouns\" = ?, \"Bio\" = ?, \"ProfilePic\" = ?, \"GradeLevel\" = ?, \"Subjects\" = ?::json WHERE \"UserID\" = CAST(? AS UUID)";
            return jdbcTemplate.update(sql, name, pronouns, bio, profilePic, gradeLevel, subjectsJson, userId) == 1;
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update user preferences", ex);
        }
    }

    public UserAccount findById(String userId) {
        String sql = "SELECT * FROM \"Private Accounts\" WHERE \"UserID\" = CAST(? AS UUID)";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, (rs, rowNum) -> mapRow(rs));
    }

    public UserAccount findByEmail(String email) {
        String sql = "SELECT * FROM \"Private Accounts\" WHERE \"Email\" = ?";
        List<UserAccount> results = jdbcTemplate.query(sql, new Object[]{email}, (rs, rowNum) -> mapRow(rs));
        return results.isEmpty() ? null : results.get(0);
    }

    private UserAccount mapRow(ResultSet rs) {
        try {
            List<String> subjects = null;
            String subjectsJson = rs.getString("Subjects");
            if (subjectsJson != null) {
                subjects = objectMapper.readValue(subjectsJson, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
            return new UserAccount(
                rs.getString("UserID"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getString("Email"),
                rs.getString("Password"),
                rs.getString("Name"),
                rs.getString("Pronouns"),
                rs.getString("Bio"),
                subjects,
                rs.getString("ProfilePic"),
                rs.getObject("GradeLevel", Integer.class)
            );
        } catch (Exception ex) {
            throw new RuntimeException("Failed to map user row", ex);
        }
    }
}
