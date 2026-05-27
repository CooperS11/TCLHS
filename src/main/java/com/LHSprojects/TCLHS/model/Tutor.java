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
    private String bio;
    private String profilePhotoUrl;
    private String gradeLevel;
    private String pronouns;
    //integers could represent courses, or we could use strings

    public Tutor(String id, int rating, int numRatings, List<String> courses) {
        this.id = id;
        this.rating = rating;
        this.numRatings = numRatings;
        this.courses = new ArrayList<>(courses);
        this.name = "";
        this.availability = "";
        this.bio = "";
        this.profilePhotoUrl = "";
        this.gradeLevel = "";
    }

    public Tutor(String id, String name, String availability, int rating, int numRatings, List<String> courses) {
        this.id = id;
        this.name = name;
        this.availability = availability;
        this.rating = rating;
        this.numRatings = numRatings;
        this.courses = new ArrayList<>(courses);
        this.bio = "";
        this.profilePhotoUrl = "";
        this.gradeLevel = "";
    }

    public Tutor(String id, String name, String availability, int rating, int numRatings, List<String> courses, String bio, String profilePhotoUrl, String gradeLevel, String pronouns) {
        this.id = id;
        this.name = name;
        this.availability = availability;
        this.rating = rating;
        this.numRatings = numRatings;
        this.courses = new ArrayList<>(courses);
        this.bio = bio != null ? bio : "";
        this.profilePhotoUrl = profilePhotoUrl != null ? profilePhotoUrl : "";
        this.gradeLevel = gradeLevel != null ? gradeLevel : "";
        this.pronouns = pronouns != null ? pronouns : "";
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

    public String getBio() {
        return this.bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePhotoUrl() {
        return this.profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getGradeLevel() {
        return this.gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getPronouns() {
        return this.pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }
}