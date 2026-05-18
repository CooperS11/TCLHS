package com.LHSprojects.TCLHS.model;

import java.util.ArrayList;
import java.util.List;

public class Tutor {
    private String id; //points to account
    private String name;
    private String availability;
    private int rating;
    private int numRatings;
    private ArrayList<String> courses;
    //integers could represent courses, or we could use strings

    public Tutor(String id, int rating, int numRatings, List<String> courses) {
        this.id = id;
        this.rating = rating;
        this.numRatings = numRatings;
        this.courses = new ArrayList<>(courses);
        this.name = "";
        this.availability = "";
    }

    public Tutor(String id, String name, String availability, int rating, int numRatings, List<String> courses) {
        this.id = id;
        this.name = name;
        this.availability = availability;
        this.rating = rating;
        this.numRatings = numRatings;
        this.courses = new ArrayList<>(courses);
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
    
    public void addRating(int addRating) {
        int tempRatings = this.numRatings * this.rating; //total of previous ratings
        tempRatings += addRating; //adds new rating and increments rating count
        this.numRatings++;
        int newRating = tempRatings / this.numRatings; //sets new rating
        this.rating = newRating;
    }
    
    public ArrayList<String> getCourses() {
        return courses;
    }

    public int getRating() {
        return this.rating;
    }

    public int getNumRatings() {
        return this.numRatings;
    }
    
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvailability() {
        return this.availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}