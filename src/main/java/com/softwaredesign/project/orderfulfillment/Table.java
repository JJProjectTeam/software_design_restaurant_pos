package com.softwaredesign.project.orderfulfillment;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.placeholders.Recipe;

public class Table {
    private int tableNumber;
    private int tableCapacity;
    private List<DineInCustomer> customers;
    private Menu menu;

    public Table(int tableNumber, Menu menu, int tableCapacity) {
        this.tableNumber = tableNumber;
        this.customers = new ArrayList<>();
        this.menu = menu;
        this.tableCapacity = tableCapacity;
    }

    public void addCustomer(DineInCustomer customer) {
        customers.add(customer);
    }

    public List<DineInCustomer> getCustomers() {
        return new ArrayList<>(customers);
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
            tableOrders.add(customer.selectRecipeFromMenu(menu));
        }
        return tableOrders;
    }
    public int getTableNumber() {
        return tableNumber;
    }
    public int getTableCapacity() {
        return tableCapacity;
    }
}
