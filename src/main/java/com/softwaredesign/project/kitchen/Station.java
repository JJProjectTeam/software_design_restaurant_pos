package com.softwaredesign.project.kitchen;

import com.softwaredesign.project.engine.Entity;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Station extends Entity {
    protected Queue<Order> orderQueue;
    protected Order currentOrder;
    protected int processingTime;
    protected int currentProcessingTime;

    public Station(int processingTime) {
        this.orderQueue = new LinkedList<>();
        this.processingTime = processingTime;
        this.currentProcessingTime = 0;
    }

    @Override
    public void readState() {
        if (currentOrder == null && !orderQueue.isEmpty()) { // if there is an order ready to be processed
            currentOrder = orderQueue.poll(); // read it in
            currentProcessingTime = 0;        // reset tick
        }

        if (currentOrder != null) { // if there is an order being processed
            currentProcessingTime++; // tick up
            if (currentProcessingTime >= processingTime) { // if the tick count reaches the processing time
                processOrder(currentOrder);                // process the order (i.e. finish up!)
                currentOrder = null;
            }
        }
    }

    @Override
    public void writeState() {
        // No state updates needed in write phase
    }

    public void addOrder(Order order) {
        orderQueue.offer(order);
    }

    protected abstract void processOrder(Order order);
}
