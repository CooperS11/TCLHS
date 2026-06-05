package com.LHSprojects.TCLHS.service;


import java.util.ArrayList;

import com.LHSprojects.TCLHS.model.Student;
import com.LHSprojects.TCLHS.model.Tutor;


public class TutorMatcher {
    public ArrayList<Tutor> match(Student student, ArrayList<Tutor> tutors) {
       //gives a score to each tutor based on how many courses they have in common with the student, then sorts the tutors based on that score and resolves ties with rating, and then alphabetically
         ArrayList<Tutor> sorted = new ArrayList<>(tutors);
         
         sorted.sort((t1, t2) -> {
             // Calculate common courses for each tutor
             int commonCourses1 = 0;
             int commonCourses2 = 0;
             
             for (String course : student.getCourses()) {
                 if (t1.getCourses().contains(course)) {
                     commonCourses1++;
                 }
                 if (t2.getCourses().contains(course)) {
                     commonCourses2++;
                 }
             }
             
             // First sort by common courses (descending)
             if (commonCourses1 != commonCourses2) {
                 return Integer.compare(commonCourses2, commonCourses1);
             }
             
             // Then by rating (descending)
             if (t1.getRating() != t2.getRating()) {
                 return Integer.compare(t2.getRating(), t1.getRating());
             }
             
             // Finally alphabetically by name (ascending)
             return t1.getName().compareTo(t2.getName());
         });
    

         return sorted;
    }

}