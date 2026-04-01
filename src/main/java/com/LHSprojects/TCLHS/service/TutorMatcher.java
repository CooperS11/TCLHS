package com.LHSprojects.TCLHS.service;

import java.util.ArrayList;

public class TutorMatcher {
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
}