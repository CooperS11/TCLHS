package com.LHSprojects.TCLHS.service;


import java.util.ArrayList;

import com.LHSprojects.TCLHS.model.Student;
import com.LHSprojects.TCLHS.model.Tutor;

//Fix imports, student and tutor imports were causing error

public class TutorMatcher {
    //calculates how many courses match between a student and tutor
    public int match(Student student, Tutor tutor) {
        ArrayList<String> s = student.getCourses();
        ArrayList<String> t = tutor.getCourses();
        int match = 0;

        for(int i = 0; i < t.size(); i++) {
            for(int j = 0; j < s.size(); j++) {
                if (s.get(i).equals(t.get(i))) {
                    match++;
                }
            }
        }

        return match;
    }

    //returns a list of matching courses
    public ArrayList<String> matchIDs(Student student, Tutor tutor) {
    ArrayList<String> match = new ArrayList<>();
    ArrayList<String> s = student.getCourses();

    for (String course : tutor.getCourses()) {
        if (s.contains(course)) {
            match.add(course);
        }
    }

    return match;
}

    //returns a list of tutors sorted by rating
    public ArrayList<Tutor> sortRating (Student student) {
        return null;
    }
}