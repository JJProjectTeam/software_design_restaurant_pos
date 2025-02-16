package com.softwaredesign.project.placeholders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Station {
    private List<Order> backlog;

    public Station() {
        this.backlog = new ArrayList<>();
    }

    public LocalDateTime getOldestOrderTime() {
        return backlog.stream()
                .map(Order::getOrderTime)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    public int getBacklogSize() {
        return backlog.size();
    }
}
