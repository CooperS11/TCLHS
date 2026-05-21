package com.LHSprojects.TCLHS.service;

import com.LHSprojects.TCLHS.model.Student;
import com.LHSprojects.TCLHS.model.Tutor;

public class Link {
    //initially defined fields
    private Student student;
    private Tutor tutor;
    private String subject;
    
    //fields that will be updated as the meeting is proposed and accepted/rejected 
    private String time;
    private String date;
    private String status;
    private String lastSender;
    private String message;
    private String details;
    

    public Link(Student student, Tutor tutor, String details, String subject){
        this.student = student;
        this.tutor = tutor; 
        this.status = "pending"; 
        this.subject = subject;
        this.details = details;
        
    }

    public void proposeMeet(String time, String date, String message, String sender){
        this.time = time;
        this.date = date;
        this.message = message;
        this.lastSender = sender;
    }

    public void acceptMeet(){
        this.status = "accepted";
    }

    public void rejectMeet(){
        this.status = "rejected";
    }

    











    

}
