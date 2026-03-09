package com.LHSprojects.TCLHS.model;

import java.util.ArrayList;

public class Tutor {
    private String id; //points to account
    private int rating;
    private int numRatings;
    private ArrayList<Integer> courses; //list of tutor's courses that they are good at
    //integers could represent courses, or we could use strings

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
    
    public void addRating(int addRating) {
        int tempRatings = this.ratings * this.rating; //total of previous ratings
        tempRatings += addRating; //adds new rating and increments rating count
        this.numRatings++;
        int newRating = tempRatings / this.numRatings; //sets new rating
        this.rating = newRating;
    }
    
    public ArrayList<Integer> getCourses() {
        return courses;
    }
}