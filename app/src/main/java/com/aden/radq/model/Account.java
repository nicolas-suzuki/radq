package com.aden.radq.model;

import com.google.firebase.database.Exclude;

@SuppressWarnings("ALL")
public class Account {

    private String id;
    private String name;
    private String email;
    private String password;

    public Account(){
    }

    @Exclude
    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        this.email = email;
    }

    public final void setPassword(String password) {
        this.password = password;
    }
}


