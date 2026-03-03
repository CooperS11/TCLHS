package com.LHSprojects.TCLHS.model;

public class Account {
    private String id;
    private String firstName;
    private String lastName;
    private int gradeLevel;
    private String email;
    private String password;
    private String key; //placeholder 
    public Account(String id, String firstName, String lastName, int gradeLevel, String email, String password, String key){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gradeLevel = gradeLevel;
        this.email = email;
        this.password = password;
        this.key = key;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public int getGradeLevel(){
        return gradeLevel;
    }
    public boolean isValidLogin(String givenPassword){
        return (givenPassword.equals(password));
    }
}