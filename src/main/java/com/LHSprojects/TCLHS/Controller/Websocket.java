package com.LHSprojects.TCLHS.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.LHSprojects.TCLHS.Repository.Repository;
import com.LHSprojects.TCLHS.model.Tutor;
import com.LHSprojects.TCLHS.service.Link;
import java.util.List;
import java.util.Map;

@Controller
public class Websocket {

@Autowired
private SimpMessagingTemplate messagingTemplate;

@Autowired
private Repository repository;

    @MessageMapping("/getTutors")
    @SendTo("/topic/tutors")
    public List<Tutor> getTutors() {
        return repository.getAllTutors();
    }

    @MessageMapping("/createLink")
    public void createLink(@Payload Map<String, String> payload) {
        String tutorId = payload.get("tutorId");
        String subject = payload.get("subject");
        String details = payload.get("details");
        String date    = payload.get("date");
        String time    = payload.get("time");
        String message = payload.get("message");

        Tutor tutor = repository.getTutor(tutorId);
        Link link = new Link(null, tutor, details, subject);
        link.proposeMeet(time, date, message, "student");

        messagingTemplate.convertAndSend("/topic/link/" + tutorId, payload);
    }

    @MessageMapping("/acceptLink")
    public void acceptLink(@Payload Map<String, String> payload) {
        String tutorId   = payload.get("tutorId");
        String requestId = payload.get("requestId");
        Map<String, String> response = Map.of("requestId", requestId, "status", "accepted");
        messagingTemplate.convertAndSend("/topic/link/response/" + tutorId, response);
    }

    @MessageMapping("/rejectLink")
    public void rejectLink(@Payload Map<String, String> payload) {
        String tutorId   = payload.get("tutorId");
        String requestId = payload.get("requestId");
        Map<String, String> response = Map.of("requestId", requestId, "status", "rejected");
        messagingTemplate.convertAndSend("/topic/link/response/" + tutorId, response);
    }

    /*
    functions needed
    - getTutor(id): Retrieve a specific tutor by ID, send to topic/tutor/{id}
    
    - get matched tutors, use 
    - edit tutor information
    - edit student information
    - give tutor a rating
    - show a tutor messages
    */
}
