package com.softwaredesign.project.kitchen;

public class GrillStation extends Station {
    public GrillStation() {
        super(5); // Takes 5 ticks to grill
    }

    @Override
    protected void processOrder(Order order) {
        order.setGrilled(true);
    }
}
