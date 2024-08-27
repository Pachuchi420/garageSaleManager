package cc.pachuchi.garagesalemanager;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatBot {
    // Singleton instance
    private static ChatBot instance;

    // Fields to store ChatBot settings
    private String recipientContact;
    private int intervalMinutes;
    private String startTime;  // Stored as "HH:mm"
    private String endTime;    // Stored as "HH:mm"
    private boolean isActive;

    // Private constructor to prevent instantiation
    private ChatBot() {
        // Set default values
        this.startTime = "00:00";
        this.endTime = "00:00";
        this.intervalMinutes = 5; // Default interval in minutes
        this.isActive = false; // Default inactive state
    }

    // Method to get the single instance of ChatBot
    public static ChatBot getInstance() {
        if (instance == null) {
            instance = new ChatBot();
        }
        return instance;
    }

    // Getters and Setters
    public String getRecipientContact() {
        return recipientContact;
    }

    public void setRecipientID(String recipientID) {
        this.recipientContact = recipientID;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public int getIntervalInMilliseconds() {
        return intervalMinutes * 60 * 1000;
    }

    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Method to check if the current time is within the start and end times
    public boolean isWithinTimeFrame() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime start = LocalTime.parse(this.startTime, formatter);
        LocalTime end = LocalTime.parse(this.endTime, formatter);

        return !currentTime.isBefore(start) && !currentTime.isAfter(end);
    }

    @Override
    public String toString() {
        return "ChatBot{" +
                "recipientID='" + recipientContact + '\'' +
                ", intervalMinutes=" + intervalMinutes +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}