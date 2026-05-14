package com.LHSprojects.TCLHS.service;

import com.LHSprojects.TCLHS.model.Account;

public class EditAccount {
    //Creates temporary data representation for person who ALREADY has info
    public static void signIn(String id, String firstName, String lastName, int gradeLevel, String email, String bio, String pronouns) {
        new Account(id, firstName, lastName, gradeLevel, email, bio, pronouns);
        //Write acc to database (paul's job)
    }












}
