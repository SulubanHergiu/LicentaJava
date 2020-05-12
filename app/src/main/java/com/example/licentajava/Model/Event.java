package com.example.licentajava.Model;

public class Event {
    private String id;
    private String name;
    private String description;
    private String place;
    private String start_time;


    public Event(String id, String name, String description, String place, String start_time) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.place = place;
        this.start_time = start_time;

    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", place='" + place + '\'' +
                ", start_time='" + start_time + '\'' +
                '}';
    }
}
