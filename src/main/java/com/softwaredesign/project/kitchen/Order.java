package com.softwaredesign.project.kitchen;

import com.softwaredesign.project.engine.Entity;

public class Order extends Entity {
    private final int orderId;
    private boolean isGrilled;
    private boolean isPlated;
    private boolean isComplete;

    public Order(int orderId) {
        this.orderId = orderId;
        this.isGrilled = false;
        this.isPlated = false;
        this.isComplete = false;
    }

    @Override
    public void readState() {
        // No state reading needed for orders
    }

    @Override
    public void writeState() {
        // No state writing needed for orders
    }

    public int getOrderId() {
        return orderId;
    }

    public boolean isGrilled() {
        return isGrilled;
    }

    public void setGrilled(boolean grilled) {
        isGrilled = grilled;
    }

    public boolean isPlated() {
        return isPlated;
    }

    public void setPlated(boolean plated) {
        isPlated = plated;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}
