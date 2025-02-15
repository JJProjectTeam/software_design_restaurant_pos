package com.softwaredesign.project.kitchen;

public class PlatingStation extends Station {
    public PlatingStation() {
        super(3); // Takes 3 ticks to plate
    }

    @Override
    protected void processOrder(Order order) {
        if (order.isGrilled()) {
            order.setPlated(true);
            order.setComplete(true);
        }
    }
}
