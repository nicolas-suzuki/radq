package com.aden.radqcompanionapp.model;

import com.aden.radqcompanionapp.adapter.FirebaseConnector;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Account {
    private String id;
    private String name;
    private String email;
    private String password;

    public Account(){

    }

    public void saveContact(){
        DatabaseReference databaseReference = FirebaseConnector.getFirebase();
        databaseReference.child("accounts").child(getId()).setValue(this);
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

    @Exclude
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


