package com.softwaredesign.project.kitchen;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.softwaredesign.project.order.Order;

public class Station {
    private final StationType type;
    private List<Order> backlog;

    public Station(StationType type) {
        this.type = type;
        this.backlog = new ArrayList<>();
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


