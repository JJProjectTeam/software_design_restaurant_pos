package com.softwaredesign.project;

import com.softwaredesign.project.controller.DiningRoomController;
import com.softwaredesign.project.view.RestaurantApplication;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.orderfulfillment.Table;

public class RestaurantDriver {
    public static void main(String[] args) {
        try {
            // First create and initialize the restaurant application
            RestaurantApplication app = new RestaurantApplication();
            
            // Create menu with inventory
            System.out.println("[RestaurantDriver] Initializing inventory and menu");
            Inventory inventoryService = new Inventory();
            inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
            inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
            Menu menu = new Menu(inventoryService);

            // Create the dining room controller
            System.out.println("[RestaurantDriver] Creating dining room controller");
            DiningRoomController controller = new DiningRoomController(menu, 5, 12);

            // Setup initial restaurant state
            System.out.println("[RestaurantDriver] Setting up initial restaurant state");
            controller.assignWaiterToTable(1, 'A');
            controller.assignWaiterToTable(2, 'B');
            controller.assignWaiterToTable(3, 'A');

            // Add some initial customers
            Table table1 = controller.getSeatingPlan().getTable(1);
            table1.addCustomer(new DineInCustomer());
            table1.addCustomer(new DineInCustomer());

            Table table2 = controller.getSeatingPlan().getTable(2);
            table2.addCustomer(new DineInCustomer());

            // Run the application
            System.out.println("[RestaurantDriver] Starting restaurant application");
            app.run();

        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Error running restaurant application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
