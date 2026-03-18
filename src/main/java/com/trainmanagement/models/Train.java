package com.trainmanagement.models;

import com.trainmanagement.enums.TrainType;

public class Train {
    private final String id;
    private String trainNumber;
    private String name;
    private TrainType trainType;

    public Train(String id, String trainNumber, String name, TrainType trainType) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.name = name;
        this.trainType = trainType;
    }

    public String getId() {
        return id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TrainType getTrainType() {
        return trainType;
    }

    public void setTrainType(TrainType trainType) {
        this.trainType = trainType;
    }

    @Override
    public String toString() {
        return "Train{" +
                "id='" + id + '\'' +
                ", trainNumber='" + trainNumber + '\'' +
                ", name='" + name + '\'' +
                ", trainType=" + trainType +
                '}';
    }
}
