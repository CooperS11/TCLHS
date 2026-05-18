package com.LHSprojects.TCLHS.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.resilience.annotation.ConcurrencyLimit;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class Websocket {

@Autowired
private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/getTutors");
    @SendTo("/topic/tutors")
    public void getTutors() {
        // This method will be called when a message is sent to the "/getTutors" endpoint.
        // You can implement logic here to retrieve and send tutor data to the client.
        
    }
}
