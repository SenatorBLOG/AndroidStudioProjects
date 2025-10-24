package com.example.asteroid.model;

public class Asteroid {
    private String designation;
    private int iconResource;
    private String trackingStatus = "";

    public Asteroid(int iconResource, String trackingStatus) {
        this.designation = designation;
        this.iconResource = iconResource;
        this.trackingStatus = trackingStatus;
    }

    public String getDesignation() {
        return designation;
    }

    public int getIconResource() {
        return iconResource;
    }

    public String getTrackingStatus() {
        return trackingStatus;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public void setTrackingStatus(String trackingStatus) {
        this.trackingStatus = trackingStatus;
    }
}
