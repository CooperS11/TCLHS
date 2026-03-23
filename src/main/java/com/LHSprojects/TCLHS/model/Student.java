package com.LHSprojects.TCLHS.model;

import java.util.ArrayList;

public class Student {
    private String id; //points to account
    private ArrayList<Integer> courses;

    public ArrayList<Integer> getCourses() {
        return courses;
    }

    public void addCourse(int id) {
        this.courses.add(id);
    }

    public void removeCourse(int courseID) {
        for (int i = 0; i < courses.size(); i++) {
            if (courseID == courses.get(i)) {
                courses.remove(i);
            }
        }
    }
}