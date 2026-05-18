package com.LHSprojects.TCLHS.service;

import com.LHSprojects.TCLHS.Repository.Repository;
import com.LHSprojects.TCLHS.Repository.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class DataLoaderService {

    @Autowired
    private Repository repository;

    @Autowired
    private TutorRepository tutorRepository;

    @PostConstruct
    public void loadData() {
        try {
            repository.setAllTutors(tutorRepository.getAllTutors());
            System.out.println("Loaded " + repository.getAllTutors().size() + " tutors from DB");

        } catch (Exception e) {
            System.err.println("Failed to load tutors from DB: " + e.getMessage());
            
        }
    }
}