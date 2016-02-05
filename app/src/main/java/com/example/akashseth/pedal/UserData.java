package com.example.akashseth.pedal;

/**
 * Created by Akash seth on 1/3/2016.
 */
public class UserData {
    public String timeElapsed;
    public String distance;
    public String avgSpeed;
    public String lastActive;
    public String startActivityTime;
    public int userId;
    public String calories;

    public UserData() {

    }

    public UserData(String timeElapsed, String distance, String avgSpeed, String lastActive, int userId, String startActivityTime, String calories) {
        this.timeElapsed = timeElapsed;
        this.distance = distance;
        this.avgSpeed = avgSpeed;
        this.lastActive = lastActive;
        this.userId = userId;
        this.startActivityTime = startActivityTime;
        this.calories = calories;
    }

    public UserData(String timeElapsed, String distance, String avgSpeed, String lastActive, String startActivityTime, String calories) {
        this.timeElapsed = timeElapsed;
        this.distance = distance;
        this.avgSpeed = avgSpeed;
        this.lastActive = lastActive;
        this.startActivityTime = startActivityTime;
        this.calories = calories;
    }

    public String getTimeElapsed() {
        return this.timeElapsed;
    }

    public String getDistance() {
        return this.distance;
    }

    public String getAvgSpeed() {
        return this.avgSpeed;
    }

    public String getLastActive() {
        return this.lastActive;
    }

    public int getUserId() {
        return this.userId;
    }

    public String getStartActivityTime() {
        return this.startActivityTime;
    }

    public String getCalories() {
        return this.calories;
    }
}

