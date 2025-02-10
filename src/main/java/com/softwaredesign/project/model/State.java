package com.softwaredesign.project.model;

public interface State {
    default void processOrder(Order order) {
        throw new IllegalStateException("Cannot process order in current state");
    }
    
    default void cancelOrder(Order order) {
        throw new IllegalStateException("Cannot cancel order in current state");
    }
    
    default void pauseOrder(Order order) {
        throw new IllegalStateException("Cannot pause order in current state");
    }
    
    default void resumeOrder(Order order) {
        throw new IllegalStateException("Cannot resume order in current state");
    }
    
    default void completeOrder(Order order) {
        throw new IllegalStateException("Cannot complete order in current state");
    }
}
