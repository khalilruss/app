package com.example.bountyhunterapi;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class Game {

    @SerializedName("id")
    private UUID id;
    @SerializedName("creator")
    private User creator;
    @SerializedName("players")
    private List<String> players;
    @SerializedName("accepted")
    private List<Player> accepted;
    @SerializedName("joined")
    private List<Player> joined;
    @SerializedName("acceptedCount")
    private int acceptedCount;
    @SerializedName("invitedCount")
    private int invitedCount;
    @SerializedName("radius")
    private double radius;
    @SerializedName("center")
    private JSONObject center;

    public Game(UUID id, User creator, List<String> players, List<Player> accepted, List<Player> joined, int acceptedCount, int invitedCount, double radius, JSONObject center) {
        this.id = id;
        this.creator = creator;
        this.players = players;
        this.accepted = accepted;
        this.joined = joined;
        this.acceptedCount = acceptedCount;
        this.invitedCount = invitedCount;
        this.radius = radius;
        this.center = center;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public List<Player> getAccepted() {
        return accepted;
    }

    public void setAccepted(List<Player> accepted) {
        this.accepted = accepted;
    }

    public List<Player> getJoined() {
        return joined;
    }

    public void setJoined(List<Player> joined) {
        this.joined = joined;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public int getInvitedCount() {
        return invitedCount;
    }

    public void setInvitedCount(int invitedCount) {
        this.invitedCount = invitedCount;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public JSONObject getCenter() {
        return center;
    }

    public void setCenter(JSONObject center) {
        this.center = center;
    }
}
