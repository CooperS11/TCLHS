package com.LHSprojects.TCLHS.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.UUID;
import com.LHSprojects.TCLHS.model.Tutor;

@Repository
public class TutorRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        for (String col : new String[]{"\"Bio\" text", "\"ProfilePicture\" text", "\"Grade\" int2", "\"Pronouns\" text"}) {
            try {
                jdbcTemplate.execute("ALTER TABLE \"Tutors\" ADD COLUMN IF NOT EXISTS " + col);
            } catch (Exception ignored) {}
        }
    }

    public String createTutor(String name, String availabilityJson, List<String> courses,
                              String bio, String profilePhotoUrl, Integer gradeLevel, String pronouns) {
        try {
            String id = UUID.randomUUID().toString();
            String coursesJson = objectMapper.writeValueAsString(courses != null ? courses : List.of());
            System.out.println("TutorRepository.createTutor: inserting tutor id=" + id + " name=" + name + " availability=" + availabilityJson + " grade=" + gradeLevel + " pronouns=" + pronouns + " courses=" + coursesJson);
            jdbcTemplate.update(
                "INSERT INTO \"Tutors\" (id, \"Name\", \"Availability\", \"Rating\", \"NumRatings\", \"Courses\", \"Bio\", \"ProfilePicture\", \"Grade\", \"Pronouns\") VALUES (CAST(? AS UUID), ?, ?::json, 0, 0, ?::json, ?, ?, ?, ?)",
                id, name, availabilityJson, coursesJson, bio, profilePhotoUrl, gradeLevel, pronouns
            );
            return id;
        } catch (Exception e) {
            System.err.println("TutorRepository.createTutor failed: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not create tutor: " + e.getMessage(), e);
        }
    }

    public void addRating(String tutorId, int newRating) {
        jdbcTemplate.update(
            "UPDATE \"Tutors\" SET \"Rating\" = (\"Rating\" * \"NumRatings\" + ?) / (\"NumRatings\" + 1), \"NumRatings\" = \"NumRatings\" + 1 WHERE id = CAST(? AS UUID)",
            newRating, tutorId
        );
    }

    public List<Tutor> getAllTutors() {
        String sql = "SELECT * FROM \"Tutors\"";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            try {
                String coursesJson = rs.getString("Courses");
                List<String> courses = objectMapper.readValue(
                    coursesJson,
                    objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class)
                );
                

                // attempt to read optional columns (Bio, ProfilePicture/ProfilePhotoUrl, Grade/GradeLevel, Pronouns)
                String bio = null;
                String profilePhotoUrl = null;
                String gradeLevel = null;
                String pronouns = null;
                try { bio = rs.getString("Bio"); } catch (Exception ignored) {}
                // accept several possible column names for profile picture
                try { profilePhotoUrl = rs.getString("ProfilePicture"); } catch (Exception ignored) {}
                if (profilePhotoUrl == null) try { profilePhotoUrl = rs.getString("ProfilePhotoUrl"); } catch (Exception ignored) {}
                if (profilePhotoUrl == null) try { profilePhotoUrl = rs.getString("ProfilePhoto"); } catch (Exception ignored) {}
                if (profilePhotoUrl == null) try { profilePhotoUrl = rs.getString("Profile_Photo"); } catch (Exception ignored) {}
                if (profilePhotoUrl == null) try { profilePhotoUrl = rs.getString("ProfilePhotoURL"); } catch (Exception ignored) {}

                try { gradeLevel = rs.getString("Grade"); } catch (Exception ignored) {}
                if (gradeLevel == null) try { gradeLevel = rs.getString("GradeLevel"); } catch (Exception ignored) {}

                try { pronouns = rs.getString("Pronouns"); } catch (Exception ignored) {}

                // Convert gradeLevel from String to Integer
                Integer gradeLevelInt = null;
                if (gradeLevel != null && !gradeLevel.isEmpty()) {
                    try {
                        gradeLevelInt = Integer.parseInt(gradeLevel);
                    } catch (NumberFormatException ignored) {}
                }

                return new Tutor(
                    rs.getString("id"),
                    rs.getString("Name"),
                    rs.getString("Availability"),
                    rs.getInt("Rating"),
                    rs.getInt("NumRatings"),
                    courses,
                    bio,
                    profilePhotoUrl,
                    gradeLevelInt,
                    pronouns
                );
            } catch (Exception e) {
                // Handle JSON parsing errors (e.g., log and return null or default)
                e.printStackTrace();
                return null;  // Or throw a custom exception
            }
        });
    }
}