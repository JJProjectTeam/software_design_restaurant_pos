package com.softwaredesign.project.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.order.Order;

public class Station {
    private List<Order> backlog;

    public Station() {
        this.backlog = new ArrayList<>();
    }

    public LocalDateTime getOldestOrderTime() {
        // TODO: Implement getOldestOrderTime
        // Original implementation:
        // return backlog.stream()
        //         .map(Order::getOrderTime)
        //         .min(LocalDateTime::compareTo)
        //         .orElse(LocalDateTime.now());
        return LocalDateTime.now(); // Placeholder return
    }

    public int getBacklogSize() {
        return backlog.size();
    }
}
