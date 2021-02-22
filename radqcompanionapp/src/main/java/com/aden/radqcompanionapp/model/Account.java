package com.aden.radqcompanionapp.model;

import com.google.firebase.database.Exclude;

@SuppressWarnings("ALL")
public class Account {
    private String id;
    private String name;
    private String email;
    private String password;
    private String phoneKey;

    public Account(){

    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneKey() {
        return phoneKey;
    }

    public void setPhoneKey(String phoneKey) {
        this.phoneKey = phoneKey;
    }
}


