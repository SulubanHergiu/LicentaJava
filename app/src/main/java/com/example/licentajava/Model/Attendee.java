package com.example.licentajava.Model;

public class Attendee {
    private String name;
    private String  email;
    private String startTime;
    private String endTime;

    public Attendee() {
    }

    public Attendee(String name, String email, String startTime, String nedTime) {
        this.name = name;
        this.email = email;
        this.startTime = startTime;
        this.endTime = nedTime;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String nedTime) {
        this.endTime = nedTime;
    }
}
