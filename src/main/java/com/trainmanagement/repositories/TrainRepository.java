package com.trainmanagement.repositories;

import com.trainmanagement.models.Train;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TrainRepository {
    private final Map<String, Train> trains = new ConcurrentHashMap<>();

    public void save(Train train) {
        trains.put(train.getId(), train);
    }

    public Optional<Train> findById(String id) {
        return Optional.ofNullable(trains.get(id));
    }

    public List<Train> findAll() {
        return new ArrayList<>(trains.values());
    }

    public boolean existsById(String id) {
        return trains.containsKey(id);
    }

    public Optional<Train> deleteById(String id) {
        return Optional.ofNullable(trains.remove(id));
    }
}
