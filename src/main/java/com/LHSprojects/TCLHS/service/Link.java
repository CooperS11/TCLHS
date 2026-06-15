package com.LHSprojects.TCLHS.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Link {
    private String id;
    private String studentId;
    private String tutorId;
    private String subject;
    private String details;
    private String message;
    private String status;
    private String lastSender;
    private List<Map<String, String>> sessions;

    public Link(String studentId, String tutorId, String subject, String details) {
        this.id = UUID.randomUUID().toString();
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.subject = subject;
        this.details = details;
        this.status = "pending";
    }

    public void proposeMeet(List<Map<String, String>> sessions, String message, String sender) {
        this.sessions = sessions;
        this.message = message;
        this.lastSender = sender;
    }

    public void acceptMeet() { this.status = "accepted"; }
    public void rejectMeet() { this.status = "rejected"; }

    public String getId()           { return id; }
    public String getStudentId()    { return studentId; }
    public String getTutorId()      { return tutorId; }
    public String getSubject()      { return subject; }
    public String getDetails()      { return details; }
    public String getMessage()      { return message; }
    public String getStatus()       { return status; }
    public String getLastSender()   { return lastSender; }
    public List<Map<String, String>> getSessions() { return sessions; }

    public void setId(String id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setSessions(List<Map<String, String>> sessions) { this.sessions = sessions; }
}
