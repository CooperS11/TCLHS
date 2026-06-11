package com.LHSprojects.TCLHS.Repository;

import com.LHSprojects.TCLHS.service.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Map<String, Object>> getLinksByStudentId(String studentId) {
        String sql = """
            SELECT l."id"::text, l."TutorID"::text AS "TutorID", l."StudentID"::text AS "StudentID",
                   l."Status", l."TimeSuggestedBy", l."SuggestedTime"::text AS "SuggestedTime",
                   l."Message", l."Details", t."Name" AS "TutorName"
            FROM "Links" l
            LEFT JOIN "Tutors" t ON t.id = l."TutorID"
            WHERE l."StudentID" = CAST(? AS UUID)
            ORDER BY l."id"
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> buildRow(rs,
            "TutorName", rs.getString("TutorName")), studentId);
    }

    public List<Map<String, Object>> getLinksByTutorId(String tutorId) {
        String sql = """
            SELECT l."id"::text, l."TutorID"::text AS "TutorID", l."StudentID"::text AS "StudentID",
                   l."Status", l."TimeSuggestedBy", l."SuggestedTime"::text AS "SuggestedTime",
                   l."Message", l."Details", a."Name" AS "StudentName"
            FROM "Links" l
            LEFT JOIN "Private Accounts" a ON a."UserID" = l."StudentID"
            WHERE l."TutorID" = CAST(? AS UUID)
            ORDER BY l."id"
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> buildRow(rs,
            "StudentName", rs.getString("StudentName")), tutorId);
    }

    public Map<String, Object> getLinkById(String id) {
        String sql = """
            SELECT l."id"::text, l."TutorID"::text AS "TutorID", l."StudentID"::text AS "StudentID",
                   l."Status", l."TimeSuggestedBy", l."SuggestedTime"::text AS "SuggestedTime",
                   l."Message", l."Details",
                   t."Name" AS "TutorName", a."Name" AS "StudentName"
            FROM "Links" l
            LEFT JOIN "Tutors" t ON t.id = l."TutorID"
            LEFT JOIN "Private Accounts" a ON a."UserID" = l."StudentID"
            WHERE l."id" = CAST(? AS UUID)
        """;
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = buildRow(rs, "TutorName", rs.getString("TutorName"));
            row.put("studentName", rs.getString("StudentName"));
            return row;
        }, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Map<String, Object> buildRow(java.sql.ResultSet rs, String extraKey, String extraVal) throws java.sql.SQLException {
        Map<String, Object> row = new HashMap<>();
        row.put("id", rs.getString("id"));
        row.put("tutorId", rs.getString("TutorID"));
        row.put("studentId", rs.getString("StudentID"));
        row.put("status", rs.getString("Status"));
        row.put("timeSuggestedBy", rs.getString("TimeSuggestedBy"));
        row.put("suggestedTime", rs.getString("SuggestedTime"));
        row.put("message", rs.getString("Message"));
        row.put("details", rs.getString("Details"));
        row.put(extraKey.equals("TutorName") ? "tutorName" : "studentName", extraVal);
        return row;
    }
}
