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
                

                return new Tutor(
                    rs.getString("id"),
                    rs.getInt("Rating"),
                    rs.getInt("NumRatings"),
                    courses
                );
            } catch (Exception e) {
                // Handle JSON parsing errors (e.g., log and return null or default)
                e.printStackTrace();
                return null;  // Or throw a custom exception
            }
        });
    }
}