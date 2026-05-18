package com.LHSprojects.TCLHS.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.resilience.annotation.ConcurrencyLimit;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.LHSprojects.TCLHS.Repository.Repository;
import com.LHSprojects.TCLHS.model.Tutor;
import java.util.List;

@Controller
public class Websocket {

@Autowired
private SimpMessagingTemplate messagingTemplate;

@Autowired
private Repository repository;

    @MessageMapping("/getTutors")
    @SendTo("/topic/tutors")
    public List<Tutor> getTutors() {
        // Retrieves all tutors from the repository and sends to connected clients
        return repository.getAllTutors();
    }
}
