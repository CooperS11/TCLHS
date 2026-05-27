//Temporary storage of public data using ConcurrentHashMap for easy data retrieval in the backend. 
//Method to retrive public profile data from database and give it to a sorting algorithm in service.
//Method to retrive chats if we're doing that?
//The cache updates when data is commited to actual DB


package com.LHSprojects.TCLHS.Repository;

import org.springframework.stereotype.Component;

import com.LHSprojects.TCLHS.model.Tutor;
import com.LHSprojects.TCLHS.service.Link;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Component
public class Repository {

    private final ConcurrentHashMap<String, Tutor> tutorCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Link> linkCache = new ConcurrentHashMap<>();

    public Repository(){}

    public List<Tutor> getAllTutors() {
    return Collections.unmodifiableList(new ArrayList<>(tutorCache.values()));
    }

    
    public Tutor getTutor(String id) {
        return tutorCache.get(id);
    }

   
    public void saveTutor(Tutor tutor) {
        if (tutor == null || tutor.getId() == null) return;
        tutorCache.put(tutor.getId(), tutor);
        }

    
    public void deleteTutor(String id) {
            tutorCache.remove(id);
    }

    
    public void setAllTutors(List<Tutor> tutors) {
        tutorCache.clear();
        if (tutors != null) {
            for (Tutor tutor : tutors) {
                if (tutor != null && tutor.getId() != null) {
                    tutorCache.put(tutor.getId(), tutor);
                }
            }
        }
    }

    
    public boolean updateIfExists(Tutor tutor) {
        if (tutor == null || tutor.getId() == null) return false;

        return tutorCache.replace(tutor.getId(), tutor) != null;
    }

    public int size() {
        return tutorCache.size();
    }

    public void clear() {
        tutorCache.clear();
    }

    public List<Link> getAllLinks() {
        return Collections.unmodifiableList(new ArrayList<>(linkCache.values()));
    
    
    }

    public Link getLink(String id) {
        return linkCache.get(id);
    }
    public void saveLink(Link link) {
        if (link == null || link.getId() == null) return;
        linkCache.put(link.getId(), link);
    }
    public void deleteLink(String id){

        linkCache.remove(id);

    }

}
