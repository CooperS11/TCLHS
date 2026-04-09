package com.LHSprojects.TCLHS.service;

import java.util.*;
import java.util.stream.Collectors;

import com.LHSprojects.TCLHS.Repository.Repository;
import com.LHSprojects.TCLHS.model.Tutor;

public class ManualFilter {

    private Map<String, List<String>> filters;
    private Repository repo;

    public ManualFilter(Map<String, List<String>> filters, Repository repo) {
        this.filters = filters;
        this.repo = repo;
    }

    public List<Tutor> filterTutors() {

        List<Tutor> tutors = repo.getAllTutors();

        if (filters == null || filters.isEmpty()) {
            return tutors;
        }

        for (String key : filters.keySet()) {

            List<String> values = filters.get(key);

            switch (key.toLowerCase()) {

                case "subject":
                    tutors = tutors.stream()
                            .filter(tutor -> {
                                List<String> courses = tutor.getCourses();

                                
                                return courses.stream()
                                        .anyMatch(course ->
                                                values.contains(course.toLowerCase()));
                            })
                            .collect(Collectors.toList());
                    break;

                case "rating":
                    tutors = tutors.stream()
                            .filter(tutor ->
                                    values.contains(String.valueOf(tutor.getRating())))
                            .collect(Collectors.toList());
                    break;

                default:
                    break;
            }
        }

        return tutors;
    }
}