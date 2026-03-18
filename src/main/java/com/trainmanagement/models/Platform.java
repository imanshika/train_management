package com.trainmanagement.models;

public class Platform {
    private final String id;
    private int platformNumber;
    private String stationName;

    public Platform(String id, int platformNumber, String stationName) {
        this.id = id;
        this.platformNumber = platformNumber;
        this.stationName = stationName;
    }

    public String getId() {
        return id;
    }

    public int getPlatformNumber() {
        return platformNumber;
    }

    public void setPlatformNumber(int platformNumber) {
        this.platformNumber = platformNumber;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    @Override
    public String toString() {
        return "Platform{" +
                "id='" + id + '\'' +
                ", platformNumber=" + platformNumber +
                ", stationName='" + stationName + '\'' +
                '}';
    }
}
