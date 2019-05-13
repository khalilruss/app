package com.example.bountyhunterapi;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

public class Player {
    @SerializedName("user")
    private User user;
    @SerializedName("photo")
    private String image;
    @SerializedName("type")
    private String type;
    @SerializedName("location")
    private JSONObject location;

    public Player(User user, String image, String type, JSONObject location) {
        this.user = user;
        this.image = image;
        this.type = type;
        this.location = location;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getLocation() {
        return location;
    }

    public void setLocation(JSONObject location) {
        this.location = location;
    }
}
