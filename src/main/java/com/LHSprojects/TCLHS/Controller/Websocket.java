package com.LHSprojects.TCLHS.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.LHSprojects.TCLHS.Repository.AccountRepository;
import com.LHSprojects.TCLHS.Repository.LinkRepository;
import com.LHSprojects.TCLHS.Repository.Repository;
import com.LHSprojects.TCLHS.model.Tutor;
import com.LHSprojects.TCLHS.model.UserAccount;
import com.LHSprojects.TCLHS.service.Link;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class Websocket {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private Repository repository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LinkRepository linkRepository;

    @MessageMapping("/getTutors")
    @SendTo("/topic/tutors")
    public List<Tutor> getTutors() {
        return repository.getAllTutors();
    }

    @MessageMapping("/getTutor")
    public void getTutor(@Payload Map<String, String> payload) {
        String id = payload.get("id");
        if (id == null) return;
        Tutor tutor = repository.getTutor(id);
        if (tutor != null) {
            messagingTemplate.convertAndSend("/topic/tutor/" + id, tutor);
        }
    }

    @MessageMapping("/getTutorProfile")
    public void getTutorProfile(@Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        if (userId == null) return;
        Tutor tutor = repository.getTutor(userId);
        if (tutor != null) {
            messagingTemplate.convertAndSend("/topic/tutor/profile/" + userId, tutor);
        }
    }

    @MessageMapping("/createLink")
    public void createLink(@Payload Map<String, Object> payload) {
        String tutorId   = getString(payload, "tutorId");
        String studentId = getString(payload, "studentId");
        String details   = getString(payload, "details");
        String message   = getString(payload, "message");

        List<String> subjects = new ArrayList<>();
        Object subjectsObj = payload.get("subjects");
        if (subjectsObj instanceof List) {
            for (Object s : (List<?>) subjectsObj) {
                if (s != null) subjects.add(s.toString());
            }
        }
        String subject = String.join(", ", subjects);

        List<Map<String, String>> sessions = new ArrayList<>();
        Object sessionsObj = payload.get("sessions");
        if (sessionsObj instanceof List) {
            for (Object s : (List<?>) sessionsObj) {
                if (s instanceof Map) {
                    Map<String, String> session = new HashMap<>();
                    ((Map<?, ?>) s).forEach((k, v) -> session.put(k.toString(), v != null ? v.toString() : ""));
                    sessions.add(session);
                }
            }
        }

        String studentName = "Student";
        if (studentId != null) {
            try {
                UserAccount student = accountRepository.findById(studentId);
                if (student != null && student.getName() != null) {
                    studentName = student.getName();
                }
            } catch (Exception ignored) {}
        }

        Link link = new Link(studentId, tutorId, subject, details);
        link.proposeMeet(sessions, message, "student");
        repository.saveLink(link);
        linkRepository.saveLink(link);

        Map<String, Object> broadcast = new HashMap<>();
        broadcast.put("requestId", link.getId());
        broadcast.put("studentId", studentId);
        broadcast.put("studentName", studentName);
        broadcast.put("subject", subject);
        broadcast.put("details", details);
        broadcast.put("sessions", sessions);
        broadcast.put("message", message);
        broadcast.put("status", "pending");

        messagingTemplate.convertAndSend("/topic/link/" + tutorId, broadcast);
    }

    @MessageMapping("/acceptLink")
    public void acceptLink(@Payload Map<String, String> payload) {
        String requestId = payload.get("requestId");
        Link link = requestId != null ? repository.getLink(requestId) : null;
        if (link != null) {
            link.acceptMeet();
            repository.saveLink(link);
            linkRepository.updateLinkStatus(requestId, "accepted");
        }
        String studentId = link != null ? link.getStudentId() : null;
        if (studentId != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "accepted");
            messagingTemplate.convertAndSend("/topic/link/response/" + studentId, (Object) response);
        }
    }

    @MessageMapping("/rejectLink")
    public void rejectLink(@Payload Map<String, String> payload) {
        String requestId = payload.get("requestId");
        Link link = requestId != null ? repository.getLink(requestId) : null;
        if (link != null) {
            link.rejectMeet();
            repository.saveLink(link);
            linkRepository.updateLinkStatus(requestId, "rejected");
        }
        String studentId = link != null ? link.getStudentId() : null;
        if (studentId != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "rejected");
            messagingTemplate.convertAndSend("/topic/link/response/" + studentId, (Object) response);
        }
    }

    @MessageMapping("/suggestTime")
    public void suggestTime(@Payload Map<String, Object> payload) {
        String linkId      = getString(payload, "linkId");
        String suggestedBy = getString(payload, "suggestedBy");
        String message     = getString(payload, "message");

        List<Map<String, String>> sessions = new ArrayList<>();
        Object sessionsObj = payload.get("sessions");
        if (sessionsObj instanceof List) {
            for (Object s : (List<?>) sessionsObj) {
                if (s instanceof Map) {
                    Map<String, String> session = new HashMap<>();
                    ((Map<?, ?>) s).forEach((k, v) -> session.put(k.toString(), v != null ? v.toString() : ""));
                    sessions.add(session);
                }
            }
        }

        Link link = linkId != null ? repository.getLink(linkId) : null;
        if (link == null) return;

        link.proposeMeet(sessions, message, suggestedBy);
        repository.saveLink(link);
        linkRepository.saveLink(link);

        Map<String, Object> broadcast = new HashMap<>();
        broadcast.put("requestId", linkId);
        broadcast.put("sessions", sessions);
        broadcast.put("message", message);
        broadcast.put("suggestedBy", suggestedBy);
        broadcast.put("type", "newTimeSuggested");

        if ("student".equals(suggestedBy)) {
            messagingTemplate.convertAndSend("/topic/link/" + link.getTutorId(), broadcast);
        } else {
            messagingTemplate.convertAndSend("/topic/link/response/" + link.getStudentId(), (Object) broadcast);
        }
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
