//Temporary storage of public data using ConcurrentHashMap for easy data retrieval in the backend. 
//Method to retrive public profile data from database and give it to a sorting algorithm in service.
//Method to retrive chats if we're doing that?
//The cache updates when data is commited to actual DB


package com.LHSprojects.TCLHS.Repository;

import org.springframework.stereotype.Component;

import com.LHSprojects.TCLHS.model.Tutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Component
public class Repository {

    private final ConcurrentHashMap<String, Tutor> cache = new ConcurrentHashMap<>();

    public Repository(){}

    public List<Tutor> getAllTutors() {
    return Collections.unmodifiableList(new ArrayList<>(cache.values()));
    }

    
    public Tutor getTutor(String id) {
        return cache.get(id);
    }

   
    public void saveTutor(Tutor tutor) {
        if (tutor == null || tutor.getId() == null) return;
        cache.put(tutor.getId(), tutor);
        }

    
    public void deleteTutor(String id) {
            cache.remove(id);
    }

    
    public void setAllTutors(List<Tutor> tutors) {
        cache.clear();
        if (tutors != null) {
            for (Tutor tutor : tutors) {
                if (tutor != null && tutor.getId() != null) {
                    cache.put(tutor.getId(), tutor);
                }
            }
        }
    }

    
    public boolean updateIfExists(Tutor tutor) {
        if (tutor == null || tutor.getId() == null) return false;

        return cache.replace(tutor.getId(), tutor) != null;
    }

    public int size() {
        return cache.size();
    }
}
