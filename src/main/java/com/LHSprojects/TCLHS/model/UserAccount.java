package com.LHSprojects.TCLHS.model;

import java.time.OffsetDateTime;
import java.util.List;

public class UserAccount {
    private String id;
    private OffsetDateTime createdAt;
    private String email;
    private String password;
    private String name;
    private String pronouns;
    private String bio;
    private List<String> subjects;
    private String profilePic;
    private Integer gradeLevel;

    public UserAccount(String id, OffsetDateTime createdAt, String email, String password, String name, String pronouns, String bio, List<String> subjects, String profilePic, Integer gradeLevel) {
        this.id = id;
        this.createdAt = createdAt;
        this.email = email;
        this.password = password;
        this.name = name;
        this.pronouns = pronouns;
        this.bio = bio;
        this.subjects = subjects;
        this.profilePic = profilePic;
        this.gradeLevel = gradeLevel;
    }

    public String getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getPronouns() {
        return pronouns;
    }

    public String getBio() {
        return bio;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public Integer getGradeLevel() {
        return gradeLevel;
    }
}
