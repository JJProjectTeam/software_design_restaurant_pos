package com.softwaredesign.project.controller;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import java.util.HashMap;
import java.util.Map;
import com.softwaredesign.project.menu.Menu;

public class DiningRoomController {
    private SeatingPlan seatingPlan;
    private Map<Integer, Character> tableToWaiter;
    private RestaurantViewMediator mediator;

    public DiningRoomController(Menu menu, int totalTables, int totalSeats) {
        System.out.println("[DiningRoomController] Initializing controller...");
        this.seatingPlan = new SeatingPlan(totalTables, totalSeats, menu);
        this.tableToWaiter = new HashMap<>();
        this.mediator = RestaurantViewMediator.getInstance();
        
        // Register with mediator
        mediator.registerController("DiningRoom", this);
    }

    public void assignWaiterToTable(int tableNumber, char waiterId) {
        System.out.println("[DiningRoomController] Assigning waiter " + waiterId + " to table " + tableNumber);
        tableToWaiter.put(tableNumber, waiterId);
        updateTableView(seatingPlan.getTable(tableNumber));
    }

    public void updateAllTableViews() {
        System.out.println("[DiningRoomController] Updating all table views");
        for (Table table : seatingPlan.getAllTables()) {
            updateTableView(table);
        }
    }

    private void updateTableView(Table table) {
        int tableNumber = table.getTableNumber();
        int capacity = table.getTableCapacity();
        int occupied = table.getCustomers().size();
        String status = determineTableStatus(table);
        char waiterPresent = tableToWaiter.getOrDefault(tableNumber, '-');

        System.out.println("[DiningRoomController] Updating table " + tableNumber + 
                         " (capacity: " + capacity + 
                         ", occupied: " + occupied + 
                         ", status: " + status + 
                         ", waiter: " + waiterPresent + ")");

        mediator.notifyViewsOfType("DiningRoom", tableNumber, capacity, occupied, status, waiterPresent);
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

    public SeatingPlan getSeatingPlan() {
        return seatingPlan;
    }
}
