package com.softwaredesign.project.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Station {
    //Only one instance of each type of station is allowed ~singleton~
    private static Map<StationType, Station> instances = new EnumMap<>(StationType.class);
    private final StationType type;
    private List<Order> backlog;

    private Station(StationType type) {
        this.type = type;
        this.backlog = new ArrayList<>();
    }

    public static Station getInstance(StationType type) {
        return instances.computeIfAbsent(type, k -> new Station(k));
    }

    public StationType getType() {
        return type;
    }

    public void addOrder(Order order) {
        backlog.add(order);
    }

    public LocalDateTime getOldestOrderTime() {
        // return backlog.stream()
        //         .map(Order::getOrderTime)
        //         .min(LocalDateTime::compareTo)
        //         .orElse(LocalDateTime.now());
        return LocalDateTime.now(); //TODO order must be given an id either on creation or when added to stations
    }

    public int getBacklogSize() {
        return backlog.size();
    }
}


