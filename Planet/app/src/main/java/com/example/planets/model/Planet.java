package com.example.planets.model;

public class Planet {
    private String PlanetName;
    private int imageResource;
    private String status;

    public Planet(int imageResource, String planetName) {
        PlanetName = planetName;
        this.imageResource = imageResource;

    }

    public String getPlanetName() {
        return PlanetName;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getStatus() {
        return status;
    }

    public void setPlanetName(String planetName) {
        PlanetName = planetName;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
