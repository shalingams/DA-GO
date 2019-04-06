package com.sccodesoft.dago.Model;

public class TripHostory {
    private String date,distance,duration,fare,from,to;

    public TripHostory() {
    }

    public TripHostory(String date, String distance, String duration, String fare, String from, String to) {
        this.date = date;
        this.distance = distance;
        this.duration = duration;
        this.fare = fare;
        this.from = from;
        this.to = to;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
