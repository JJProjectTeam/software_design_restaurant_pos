package com.softwaredesign.project;

import com.softwaredesign.project.controller.DiningRoomController;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.view.RestaurantApplication;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.StationType;

public class RestaurantDriver {

    public static void main(String[] args) {
        try {
            System.out.println("[RestaurantDriver] Starting application...");
            
            // Initialize inventory and menu
            System.out.println("[RestaurantDriver] Initializing inventory and menu");
            Inventory inventoryService = new Inventory();
            inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
            inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
            Menu menu = new Menu(inventoryService);
            
            // Initialize controllers
            DiningRoomController diningRoomController = new DiningRoomController(menu, 5, 12);
            
            // Create and start the application UI
            RestaurantApplication app = new RestaurantApplication();
            
            // Let the UI initialize first
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("[RestaurantDriver] UI initialization interrupted: " + e.getMessage());
            }
            
            // Set up initial state through controllers
            System.out.println("[RestaurantDriver] Setting up initial state...");
            diningRoomController.assignWaiterToTable(1, 'A');
            diningRoomController.assignWaiterToTable(2, 'B');
            diningRoomController.assignWaiterToTable(3, 'A');
            
            // Add some initial customers
            diningRoomController.addCustomerToTable(1, new DineInCustomer());
            diningRoomController.addCustomerToTable(1, new DineInCustomer());
            diningRoomController.addCustomerToTable(2, new DineInCustomer());
            
            // Start the application
            System.out.println("[RestaurantDriver] Running application...");
            app.run();
        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Fatal error running application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
