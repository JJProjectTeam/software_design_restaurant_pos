package com.softwaredesign.project.orderState;

public interface OrderState {
    void processOrder();
    void cancelOrder();
    void pauseOrder();
}
