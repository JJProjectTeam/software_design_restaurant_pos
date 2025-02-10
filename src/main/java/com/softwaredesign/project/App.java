package com.softwaredesign.project;

import com.softwaredesign.project.model.OrderManager;
import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.command.*;

public class App {
    private final OrderCommandInvoker commandInvoker;

    public App() {
        this.commandInvoker = new OrderCommandInvoker();
    }

    public void processOrder(Order order) {
        OrderCommand command = new ProcessOrderCommand(order);
        commandInvoker.executeCommand(command);
    }

    public void cancelOrder(Order order) {
        OrderCommand command = new CancelOrderCommand(order);
        commandInvoker.executeCommand(command);
    }

    public void pauseOrder(Order order) {
        OrderCommand command = new PauseOrderCommand(order);
        commandInvoker.executeCommand(command);
    }

    public void undoLastOperation() {
        commandInvoker.undoLastCommand();
    }

    public static void main(String[] args) {
        App app = new App();
        
        OrderManager orderManager = new OrderManager();
        Order order = orderManager.createOrder();
        // Example usage
        System.out.println("Initial state: " + order);
        
        // Process the order
        app.processOrder(order);
        System.out.println("After processing: " + order);
        
        // Cancel the order
        app.cancelOrder(order);
        System.out.println("After cancelling: " + order);
        
        // Undo the cancellation - should restore to previous state
        app.undoLastOperation();
        System.out.println("After undo: " + order);
    }
}
