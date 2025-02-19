package com.softwaredesign.project.controller;

import com.softwaredesign.project.view.DiningRoomView;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.customer.DineInCustomer;
import java.util.HashMap;
import java.util.Map;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.view.RestaurantApplication;
import com.softwaredesign.project.view.ViewType;

public class DiningRoomController {
    private final RestaurantApplication app;
    private SeatingPlan seatingPlan;
    private Map<Integer, Character> tableToWaiter;

    public DiningRoomController(RestaurantApplication app, Menu menu, int totalTables, int totalSeats) {
        this.app = app;
        this.seatingPlan = new SeatingPlan(totalTables, totalSeats, menu);
        this.tableToWaiter = new HashMap<>();
        
        // Set this controller on the dining room view
        DiningRoomView diningRoomView = getDiningRoomView();
        if (diningRoomView != null) {
            diningRoomView.setController(this);
        }
    }

    public void assignWaiterToTable(int tableNumber, char waiterId) {
        tableToWaiter.put(tableNumber, waiterId);
        updateTableView(seatingPlan.getTable(tableNumber));
    }

    public void updateAllTableViews() {
        for (Table table : seatingPlan.getAllTables()) {
            updateTableView(table);
        }
    }

    private void updateTableView(Table table) {
        DiningRoomView diningRoomView = getDiningRoomView();
        if (diningRoomView != null && diningRoomView.getWindow() != null) {
            int tableNumber = table.getTableNumber();
            int capacity = table.getTableCapacity();
            int occupied = table.getCustomers().size();
            String status = determineTableStatus(table);
            char waiterPresent = tableToWaiter.getOrDefault(tableNumber, '-');

            diningRoomView.AddOrUpdateRow(tableNumber, capacity, occupied, status, waiterPresent);
        }
    }

    private String determineTableStatus(Table table) {
        if (table.getCustomers().isEmpty()) {
            return "Empty";
        } else if (table.isEveryoneReadyToOrder()) {
            return "Ready";
        } else {
            return "Browsing";
        }
    }

    private DiningRoomView getDiningRoomView() {
        return (DiningRoomView) app.getViews().get(ViewType.DINING_ROOM);
    }

    public SeatingPlan getSeatingPlan() {
        return seatingPlan;
    }

    // Driver program to demonstrate functionality
    public static void main(String[] args) {
        try {
            // Create and initialize the restaurant application
            RestaurantApplication app = new RestaurantApplication();
            
            // Create menu with inventory
            Inventory inventoryService = new Inventory();
            inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
            inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
            inventoryService.addIngredient("Mustard", 10, 0.5, StationType.PREP);
            inventoryService.addIngredient("Ketchup", 10, 0.5, StationType.PREP);
            inventoryService.addIngredient("Onion", 10, 0.5, StationType.PREP);
            inventoryService.addIngredient("Pickle", 10, 0.5, StationType.PREP);
            inventoryService.addIngredient("Mayo", 10, 0.5, StationType.PREP);
            Menu menu = new Menu(inventoryService);

            // Create the dining room controller with initial setup
            DiningRoomController controller = new DiningRoomController(app, menu, 5, 12);

            // Setup initial restaurant state
            controller.assignWaiterToTable(1, 'A'); // Waiter A assigned to table 1
            controller.assignWaiterToTable(2, 'B'); // Waiter B assigned to table 2
            controller.assignWaiterToTable(3, 'A'); // Waiter A also handles table 3

            // Add some initial customers
            Table table1 = controller.getSeatingPlan().getTable(1);
            table1.addCustomer(new DineInCustomer());
            table1.addCustomer(new DineInCustomer());

            Table table2 = controller.getSeatingPlan().getTable(2);
            table2.addCustomer(new DineInCustomer());

            // Run the application - user can navigate to dining room through UI
            app.run();

        } catch (Exception e) {
            System.err.println("Error running restaurant application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
