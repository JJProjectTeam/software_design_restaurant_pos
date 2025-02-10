package com.softwaredesign.project.orderState;

import com.softwaredesign.project.model.Order;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrderStateTest {

    @Test
    public void testInitialState() {
        Order order = new Order("123");
        assertTrue(order.getState() instanceof TodoState);
    }

    @Test
    public void testTodoToDoingStateTransition() {
        Order order = new Order("123");
        order.processOrder();
        assertTrue(order.getState() instanceof DoingState);
    }

    @Test
    public void testDoingToDoneStateTransition() {
        Order order = new Order("123");
        order.processOrder(); // Todo -> Doing
        order.processOrder(); // Doing -> Done
        assertTrue(order.getState() instanceof DoneState);
    }

    @Test
    public void testCancelFromDoingState() {
        Order order = new Order("123");
        order.processOrder(); // Move to Doing state
        order.cancelOrder();
        assertTrue(order.getState() instanceof InactiveState);
    }

    @Test
    public void testCancelFromTodoState() {
        Order order = new Order("123");
        order.cancelOrder();
        assertTrue(order.getState() instanceof InactiveState);
    }

    @Test
    public void testCompleteOrderFlow() {
        Order order = new Order("123");
        
        // Initial state
        assertTrue(order.getState() instanceof TodoState);
        
        // Process to Doing
        order.processOrder();
        assertTrue(order.getState() instanceof DoingState);
        
        // Process to Done
        order.processOrder();
        assertTrue(order.getState() instanceof DoneState);
    }

    @Test
    public void testPauseInDoingState() {
        Order order = new Order("123");
        order.processOrder(); // Move to Doing state
        order.pauseOrder();
        // Pause doesn't change state, just logs message
        assertTrue(order.getState() instanceof DoingState);
    }

    @Test
    public void testProcessOrderFromDoneState() {
        Order order = new Order("123");
        order.processOrder(); // Todo -> Doing
        order.processOrder(); // Doing -> Done
        
        // Attempting to process a done order should not change its state
        order.processOrder();
        assertTrue(order.getState() instanceof DoneState);
    }
}
