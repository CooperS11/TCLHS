package com.LHSprojects.TCLHS.service;


import java.util.ArrayList;

import com.LHSprojects.TCLHS.model.Student;
import com.LHSprojects.TCLHS.model.Tutor;


public class TutorMatcher {
    public ArrayList<Tutor> match(Student student, ArrayList<Tutor> tutors) {
       //gives a score to each tutor based on how many courses they have in common with the student, then sorts the tutors based on that score and resolves ties with rating, and then alphabetically
          ArrayList<Tutor> filtered = new ArrayList<>();
          Integer studentGradeLevel = student.getGradeLevel();
          
          // Filter tutors by grade level - only include tutors at or above the student's grade level
          for (Tutor tutor : tutors) {
              Integer tutorGradeLevel = tutor.getGradeLevel();
              
              // Include tutor if student has no grade level or tutor has no grade level or tutor grade level >= student grade level
              if (studentGradeLevel == null || tutorGradeLevel == null || tutorGradeLevel >= studentGradeLevel) {
                  filtered.add(tutor);
              }
          }
          
          ArrayList<Tutor> sorted = new ArrayList<>(filtered);
          
          sorted.sort((t1, t2) -> {
              // calculate common courses 
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
              
              // sort by common courses (descending)
              if (commonCourses1 != commonCourses2) {
                  return Integer.compare(commonCourses2, commonCourses1);
              }
              
              // Then by rating (descending)
              if (t1.getRating() != t2.getRating()) {
                  return Integer.compare(t2.getRating(), t1.getRating());
              }
              
              // alphabetically by name (ascending)
              return t1.getName().compareTo(t2.getName());
          });
     

          return sorted;
     }

}
