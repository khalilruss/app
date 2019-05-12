package com.example.bountyhunterapi;

import android.location.Location;
import android.util.Base64;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Player {
    @SerializedName("user")
    private User user;
    private String image;

    public Player(User user, String image) {
        this.user = user;
        this.image = image;
    }
}
