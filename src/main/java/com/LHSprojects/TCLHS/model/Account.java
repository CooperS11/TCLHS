package com.LHSprojects.TCLHS.model;
//import java.util.ArrayList;

public class Account {
    private String id;
    private String firstName;
    private String lastName;
    private int gradeLevel;
    private String email;
    private String bio;
    private String pronouns;
    boolean isTutor;
    boolean student;

    public Account(String id, String firstName, String lastName, int gradeLevel, String email, String bio, String pronouns, boolean isTutor) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gradeLevel = gradeLevel;
        this.email = email;
        this.bio = bio;
        this.pronouns = pronouns;
        this.isTutor = isTutor;
        
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public int getGradeLevel(){
        return gradeLevel;
    }

    public String getEmail() {
        return this.email;
    }

    public String getId() {
        return id;
    }

    public String getBio() {
        return bio;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void changeFirst(String name) {
        firstName = name;
    }

    public void changeLast(String name) {
        lastName = name;
    }

    public void changeGrade(int grade) {
        gradeLevel = grade;
    }

    public void changeEmail(String address) {
        email = address;
    }

    public void changeBio(String b) {
        bio = b;
    }

    public void changePronouns(String pronoun) {
        pronouns = pronoun;
    }

}