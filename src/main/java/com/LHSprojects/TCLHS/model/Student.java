package com.LHSprojects.TCLHS.model;

import java.util.ArrayList;

public class Student {
    private String id; //points to account
    private ArrayList<String> courses;

    public Student(String id, ArrayList<String> courses){
        this.id = id;
        this.courses = courses;
    }

    public ArrayList<String> getCourses() {
        return courses;
    }

    public void addCourse(String id) {
        this.courses.add(id);
    }

    public void removeCourse(String courseID) {
        for (int i = 0; i < courses.size(); i++) {
            if (courseID.equals(courses.get(i))) {
                courses.remove(i);
            }
        }
    }
}