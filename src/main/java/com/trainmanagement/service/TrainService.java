package com.trainmanagement.service;

import com.trainmanagement.enums.TrainType;
import com.trainmanagement.exceptions.TrainNotFoundException;
import com.trainmanagement.models.Train;
import com.trainmanagement.repositories.TrainRepository;

import java.util.UUID;

public class TrainService {

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    TrainRepository trainRepository;

    public Train addTrain(String trainNumber, String name, TrainType trainType){
        String id = UUID.randomUUID().toString();
        Train train = new Train(id, name, trainNumber, trainType);
        trainRepository.save(train);
        return  train;
    }

    public Train removeTrain(String id) {
        return trainRepository.deleteById(id)
                .orElseThrow(() -> new TrainNotFoundException("Train not found with id: " + id));
    }

    public Train getById(String id){
        return trainRepository.findById(id).orElseThrow(() -> new TrainNotFoundException("Train not found with id: " + id));
    }

}
