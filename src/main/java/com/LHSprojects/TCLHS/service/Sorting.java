package com.LHSprojects.TCLHS.service;
import java.util.ArrayList;

import com.LHSprojects.TCLHS.model.Student;
import com.LHSprojects.TCLHS.model.Tutor;

public class Sorting {
    //calculates how many courses match between a student and tutor
    public int courseMatch(Student student, Tutor tutor) {
        ArrayList<String> s = student.getCourses();
        ArrayList<String> t = tutor.getCourses();
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
    public ArrayList<String> idMatch(Student student, Tutor tutor) {
        ArrayList<String> s = student.getCourses();
        ArrayList<String> t = tutor.getCourses();
        ArrayList<String> match = new ArrayList<String>();

        for(int i = 0; i < t.size(); i++) {
            for(int j = 0; j < s.size(); j++) {
                if (s.get(i) == t.get(i)) {
                    match.add(s.get(i));
                }
            }
        }

        return match;
    }

    

    //sorts tutors alphabetically; needs list of tutors to work
    public ArrayList<Tutor> alphaSort(ArrayList<Tutor> tutors) {
        int n = tutors.size() - 1;

        ArrayList<Tutor> sorted = tutors;

        Tutor temp;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (sorted.get(i).getName().compareTo(sorted.get(j).getName()) > 0) {
                    temp = sorted.get(i);
                    sorted.set(i, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }
        return sorted;
    }
}

