package com.example.bountyhunterapi;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.UUID;

public class User {

    public void setId(UUID id) {
        this.id = id;
    }

    @SerializedName("id")
    private UUID id;
    @SerializedName("firstName")
    private String firstName;
    @SerializedName("lastName")
    private String lastName;
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("email")
    private String email;
    @SerializedName("active")
    private boolean active;
    @SerializedName("verified")
    private boolean verified;
    @SerializedName("created_at")
    private Date created_at;
    @SerializedName("updated_at")
    private Date updated_at;

    public User(UUID id, String firstName, String lastName, String username, String password, String email, boolean active, boolean verified, Date created_at, Date updated_at) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = active;
        this.verified = verified;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public User(UUID id, String firstName, String lastName, String username, Date created_at, Date updated_at) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.created_at = created_at;
        this.updated_at = updated_at;

    }

    public UUID getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmail() {
        return this.email;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public Date getCreated_at() {
        return this.created_at;
    }

    public Date getUpdated_at() {
        return this.updated_at;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj){
            return true;
        }
        if (obj==null||getClass()!=obj.getClass()){
            return false;
        }
        User userToCompare =(User)obj;
        return this.username.equals(userToCompare.getUsername())&&this.getEmail().equals(userToCompare.getEmail());

    }
}
