package com.softwaredesign.project.model.orderfulfillment;

import java.util.ArrayList;
import java.util.List;
import com.softwaredesign.project.model.customer.DineInCustomer;
import com.softwaredesign.project.model.menu.Menu;
import com.softwaredesign.project.model.placeholders.Recipe;

public class Table {
    private int tableNumber;
    private List<DineInCustomer> customers;
    private boolean isReadyToOrder;
    private Menu menu;

    public Table(int tableNumber, Menu menu) {
        this.tableNumber = tableNumber;
        this.customers = new ArrayList<>();
        this.menu = menu;
    }

    public void addCustomer(DineInCustomer customer) {
        customers.add(customer);
    }

    public boolean isEveryoneReadyToOrder() {
        return customers.stream().allMatch(DineInCustomer::isDoneBrowsing);
    }

    public List<Recipe> takeTableOrder() {
        if (!isEveryoneReadyToOrder()) {
            throw new IllegalStateException("Not all customers are ready to order");
        }
        List<Recipe> tableOrders = new ArrayList<>();
        for (DineInCustomer customer : customers) {
            tableOrders.add(customer.getOrder(menu));
        }
        return tableOrders;
    }
}
