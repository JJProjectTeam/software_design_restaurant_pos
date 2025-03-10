package com.softwaredesign.project.controller;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.view.DiningRoomView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.customer.DineInCustomer;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.softwaredesign.project.menu.Menu;

public class DiningRoomController extends BaseController {
    private SeatingPlan seatingPlan;
    private Map<Integer, Character> tableToWaiter;
    private RestaurantViewMediator mediator;

    public DiningRoomController(Menu menu, SeatingPlan seatingPlan) {
        super("DiningRoom");
        this.seatingPlan = seatingPlan;
        this.tableToWaiter = new HashMap<>();
        this.mediator = RestaurantViewMediator.getInstance();
        
        // Register with mediator
        mediator.registerController("DiningRoom", this);
    }

    public void assignWaiterToTable(int tableNumber, char waiterId) {
        tableToWaiter.put(tableNumber, waiterId);
        notifyViewsOfTableUpdate(seatingPlan.getTable(tableNumber));
    }

    public void addCustomerToTable(int tableNumber, DineInCustomer customer) {
        Table table = seatingPlan.getTable(tableNumber);
        table.addCustomer(customer);
        notifyViewsOfTableUpdate(table);
    }

    private void notifyViewsOfTableUpdate(Table table) {
        // First update our internal state
        int tableNumber = table.getTableNumber();
        char waiterId = tableToWaiter.getOrDefault(tableNumber, ' ');
        
        // Then notify views through the mediator
        List<View> views = mediator.getViews("DiningRoom");
        for (View view : views) {
            if (view instanceof DiningRoomView) {
                DiningRoomView diningView = (DiningRoomView) view;
                diningView.onTableUpdate(
                    tableNumber,
                    table.getTableCapacity(),
                    table.getCustomers().size(),
                    determineTableStatus(table),
                    waiterId
                );
            }
        }
    }

    @Override
    public void updateView() {
        // Update all tables
        for (Table table : seatingPlan.getAllTables()) {
            int tableNumber = table.getTableNumber();
            char waiterId = tableToWaiter.getOrDefault(tableNumber, ' ');
            
            // Notify views through the mediator
            List<View> views = mediator.getViews("DiningRoom");
            for (View view : views) {
                if (view instanceof DiningRoomView) {
                    DiningRoomView diningView = (DiningRoomView) view;
                    diningView.onTableUpdate(
                        tableNumber,
                        table.getTableCapacity(),
                        table.getCustomers().size(),
                        determineTableStatus(table),
                        waiterId
                    );
                }
            }
        }
    }

    public void refreshAllTables() {
        updateView();
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
