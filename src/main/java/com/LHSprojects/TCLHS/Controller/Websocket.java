package com.LHSprojects.TCLHS.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.resilience.annotation.ConcurrencyLimit;

@controller
public class Websocket {
    @MessageMapping("/getTutors");
    @SendTo("/topic/tutors")
    public void getTutors() {
        // This method will be called when a message is sent to the "/getTutors" endpoint.
        // You can implement logic here to retrieve and send tutor data to the client.
        
    }
}
