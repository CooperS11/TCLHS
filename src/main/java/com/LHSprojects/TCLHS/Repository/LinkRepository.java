package com.LHSprojects.TCLHS.Repository;

import com.LHSprojects.TCLHS.service.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LinkRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS "Links" (
                "id" UUID PRIMARY KEY,
                "TutorID" UUID NOT NULL,
                "StudentID" UUID NOT NULL,
                "Status" text NOT NULL DEFAULT 'pending',
                "TimeSuggestedBy" text,
                "SuggestedTime" json,
                "Message" text,
                "Details" text
            )
        """;
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("WARNING: Could not initialize Links table: " + e.getMessage());
        }
    }

    public void saveLink(Link link) {
        try {
            String suggestedTimeJson = link.getSessions() != null
                ? objectMapper.writeValueAsString(link.getSessions())
                : null;

            String sql = """
                INSERT INTO "Links" ("id", "TutorID", "StudentID", "Status", "TimeSuggestedBy", "SuggestedTime", "Message", "Details")
                VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, ?, ?::json, ?, ?)
                ON CONFLICT ("id") DO UPDATE SET
                    "Status" = EXCLUDED."Status",
                    "TimeSuggestedBy" = EXCLUDED."TimeSuggestedBy",
                    "SuggestedTime" = EXCLUDED."SuggestedTime",
                    "Message" = EXCLUDED."Message"
            """;

            jdbcTemplate.update(sql,
                link.getId(),
                link.getTutorId(),
                link.getStudentId(),
                link.getStatus(),
                link.getLastSender(),
                suggestedTimeJson,
                link.getMessage(),
                link.getDetails()
            );
        } catch (Exception e) {
            System.err.println("ERROR: Could not save link to database: " + e.getMessage());
        }
    }

    public void updateLinkStatus(String id, String status) {
        try {
            jdbcTemplate.update(
                "UPDATE \"Links\" SET \"Status\" = ? WHERE \"id\" = CAST(? AS UUID)",
                status, id
            );
        } catch (Exception e) {
            System.err.println("ERROR: Could not update link status: " + e.getMessage());
        }
    }
}
