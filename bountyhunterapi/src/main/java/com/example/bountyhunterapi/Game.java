package com.example.bountyhunterapi;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

public class Game {

    @SerializedName("id")
    private UUID id;
    @SerializedName("creator")
    private User creator;
    @SerializedName("players")
    private List<String> players;
    @SerializedName("joined")
    private List<Player> joined;
    @SerializedName("invitedCount")
    private int invitedCount;
    @SerializedName("joinedCount")
    private int joinedCount;

    public Game(UUID id, User creator, List<String> players, List<Player> joined, int invitedCount, int joinedCount) {
        this.id = id;
        this.creator = creator;
        this.players = players;
        this.joined = joined;
        this.invitedCount = invitedCount;
        this.joinedCount = joinedCount;
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

    public List<Player> getJoined() {
        return joined;
    }

    public void setJoined(List<Player> joined) {
        this.joined = joined;
    }

    public int getInvitedCount() {
        return invitedCount;
    }

    public void setInvitedCount(int invitedCount) {
        this.invitedCount = invitedCount;
    }

    public int getJoinedCount() {
        return joinedCount;
    }

    public void setJoinedCount(int joinedCount) {
        this.joinedCount = joinedCount;
    }


}
