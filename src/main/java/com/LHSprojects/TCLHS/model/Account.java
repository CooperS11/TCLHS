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

    public String getName() {
        return firstName + " " + lastName;
    }

    public int getGradeLevel(){
        return gradeLevel;
    }

    public String getEmail() {
        return this.email;
    }

    public boolean isValidLogin(String givenPassword){
        return (givenPassword.equals(password));
    }

    //calculates how many courses match between a student and tutor
    public ArrayList<Integer> match(Student student, Tutor tutor) {
        ArrayList<Integer> s = student.getCourses();
        ArrayList<Integer> t = tutor.getCourses();
        int match = 0;
        if (s == t) {
            match++;
        }
    }
}