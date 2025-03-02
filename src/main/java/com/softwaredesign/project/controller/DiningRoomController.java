package com.softwaredesign.project.controller;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.view.DiningRoomView;
import com.softwaredesign.project.view.View;

import java.util.HashMap;
import java.util.Map;
import com.softwaredesign.project.menu.Menu;

public class DiningRoomController extends BaseController {
    private SeatingPlan seatingPlan;
    private Map<Integer, Character> tableToWaiter;
    private RestaurantViewMediator mediator;

    public DiningRoomController(Menu menu, int totalTables, int totalSeats) {
        super("DiningRoom");
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
        updateRow(seatingPlan.getTable(tableNumber));
    }

    @Override
    public void updateView() {
        mediator.notifyViewUpdate("DiningRoom");
    }

    private void updateRow(Table table) {
        int tableNumber = table.getTableNumber();
        char waiterId = tableToWaiter.getOrDefault(tableNumber, ' ');
        System.out.println("[DiningRoomController] Updating view for table " + tableNumber);
        
        // Notify all registered views
        for (View view : mediator.getViews("DiningRoom")) {
            if (view instanceof DiningRoomView) {
                ((DiningRoomView) view).onTableUpdate(
                    tableNumber,
                    table.getTableCapacity(),
                    table.getCustomers().size(),
                    determineTableStatus(table),
                    waiterId
                );
            }
            else {
                System.out.println("[DiningRoomController] View is not a DiningRoomView, skipping update");
            }
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

    public SeatingPlan getSeatingPlan() {
        return seatingPlan;
    }
}
