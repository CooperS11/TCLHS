package com.LHSprojects.TCLHS.model;
//import java.util.ArrayList;

public class Account {
    private String id;
    private String firstName;
    private String lastName;
    private int gradeLevel;
    private String email;
    private String key;

    public Account(String id, String firstName, String lastName, int gradeLevel, String email, String key){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gradeLevel = gradeLevel;
        this.email = email;
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

    public String getId() {
        return id;
    }

    /* Put this in service instead of model

    //calculates how many courses match between a student and tutor
    public int match(Student student, Tutor tutor) {
        ArrayList<Integer> s = student.getCourses();
        ArrayList<Integer> t = tutor.getCourses();
        int match = 0;

        for(int i = 0; i < t.size(); i++) {
            for(int j = 0; j < s.size(); j++) {
                if (s.get(i) == t.get(i)) {
                    match++;
                }
            }
        }

        return match;
    }

    //returns a list of matching courses
    public ArrayList<Integer> matchIDs(Student student, Tutor tutor) {
        ArrayList<Integer> s = student.getCourses();
        ArrayList<Integer> t = tutor.getCourses();
        ArrayList<Integer> match = new ArrayList<Integer>();

        for(int i = 0; i < t.size(); i++) {
            for(int j = 0; j < s.size(); j++) {
                if (s.get(i) == t.get(i)) {
                    match.add(s.get(i));
                }
            }
        }

        return match;
    }
    */
}