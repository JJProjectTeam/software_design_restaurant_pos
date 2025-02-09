package com.softwaredesign.project.model;

public interface State {
    void processOrder(Order order);
    void cancelOrder(Order order);
    void pauseOrder(Order order);
    void resumeOrder(Order order);
    void completeOrder(Order order);
}
