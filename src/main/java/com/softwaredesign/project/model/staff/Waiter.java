package com.softwaredesign.project.model.staff;

import java.util.ArrayList;
import java.util.List;
import com.softwaredesign.project.model.orderfulfillment.Table;
import java.time.LocalDateTime;
import com.softwaredesign.project.model.placeholders.Recipe;
import com.softwaredesign.project.model.placeholders.OrderManager;

public class Waiter extends StaffMember {
    private List<Table> assignedTables;
    private OrderManager orderManager;

    public Waiter(double payPerHour, double speedMultiplier, OrderManager orderManager) {
        super(payPerHour, speedMultiplier);
        this.assignedTables = new ArrayList<>();
        this.orderManager = orderManager;
    }

    public void assignTable(Table table) {
        assignedTables.add(table);
    }

    public void takeTableOrder(Table table) {
        if (!assignedTables.contains(table)) {
            throw new IllegalArgumentException("This table is not assigned to this waiter");
        }

        if (!table.isEveryoneReadyToOrder()) {
            throw new IllegalStateException("Not everyone at the table is ready to order");
        }

        List<Recipe> tableOrders = table.takeTableOrder();
        for (Recipe order : tableOrders) {
            // TODO convert this to an order object
            orderManager.submitOrder(order);
        }
    }

    public List<Table> getAssignedTables() {
        return new ArrayList<>(assignedTables);
    }
}
