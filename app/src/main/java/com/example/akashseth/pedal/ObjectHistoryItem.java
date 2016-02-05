package com.example.akashseth.pedal;

/**
 * Created by Akash seth on 1/17/2016.
 */
public class ObjectHistoryItem {
    public int activityMoodIcon;
    public String activityModeText, date, time, totalDistance, totalTime, totalFriends;

    public ObjectHistoryItem(int activityMoodIcon, String activityModeText, String date, String time, String totalDistance, String totalTime, String totalFriends) {
        this.activityMoodIcon = activityMoodIcon;
        this.activityModeText = activityModeText;
        this.date = date;
        this.time = time;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.totalFriends = totalFriends;
    }
}
