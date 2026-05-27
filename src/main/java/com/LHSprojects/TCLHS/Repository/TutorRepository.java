package com.LHSprojects.TCLHS.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.LHSprojects.TCLHS.model.Tutor;

@Repository
public class TutorRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

                return new Tutor(
                    rs.getString("id"),
                    rs.getString("Name"),
                    rs.getString("Availability"),
                    rs.getInt("Rating"),
                    rs.getInt("NumRatings"),
                    courses,
                    bio,
                    profilePhotoUrl,
                    gradeLevel,
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