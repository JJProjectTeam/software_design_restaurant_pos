package com.softwaredesign.project.model.orderfulfillment;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

import com.softwaredesign.project.model.customer.DineInCustomer;
import com.softwaredesign.project.model.menu.Menu;
import com.softwaredesign.project.model.order.Meal;
import com.softwaredesign.project.model.order.Recipe;
import com.softwaredesign.project.model.singletons.StatisticsSingleton;

public class Table {
    private int tableNumber;
    private int tableCapacity;
    private List<DineInCustomer> customers;
    private Menu menu;
    private boolean isOrdering;
    private boolean orderPlaced;
    
    // Queue of meals waiting to be eaten by customers
    private Queue<Meal> pendingMeals;

    public Table(int tableNumber, Menu menu, int tableCapacity) {
        this.tableNumber = tableNumber;
        this.customers = new ArrayList<>();
        this.menu = menu;
        this.tableCapacity = tableCapacity;
        this.isOrdering = false;
        this.orderPlaced = false;
        this.pendingMeals = new LinkedList<>();
    }

    public void addCustomer(DineInCustomer customer) {
        customers.add(customer);
    }

    /**
     * Removes a customer from the table.
     * @param customer The customer to remove
     * @return True if the customer was removed, false if they weren't at this table
     */
    public boolean removeCustomer(DineInCustomer customer) {
        return customers.remove(customer);
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
        isOrdering = true;
        List<Recipe> tableOrders = new ArrayList<>();
        for (DineInCustomer customer : customers) {
            tableOrders.add(customer.selectRecipeFromMenu(menu));
        }
        isOrdering = false;
        orderPlaced = true;

        // Track statistics
        StatisticsSingleton.getInstance().incrementStat("tablesOrdered");
        StatisticsSingleton.getInstance().incrementStat("recipesOrderedByTables", tableOrders.size());

        return tableOrders;
    }
    
    /**
     * Mark this table's order as placed
     */
    public void markOrderPlaced() {
        this.orderPlaced = true;
    }
    
    /**
     * Adds a meal to this table's pending meals queue.
     * @param meal The meal to add
     */
    public void addPendingMeal(Meal meal) {
        if (meal != null) {
            pendingMeals.add(meal);
            System.out.println("Meal " + meal.getName() + " delivered to table " + tableNumber);
        }
    }
    
    /**
     * Checks if this table has any pending meals.
     * @return true if there are pending meals, false otherwise
     */
    public boolean hasPendingMeals() {
        return !pendingMeals.isEmpty();
    }
    
    /**
     * Gets the next pending meal for this table.
     * @return The next meal to be eaten, or null if none
     */
    public Meal getNextPendingMeal() {
        return pendingMeals.poll();
    }
    
    /**
     * Gets the number of pending meals at this table.
     * @return The number of pending meals
     */
    public int getPendingMealCount() {
        return pendingMeals.size();
    }
    

    public int getTableNumber() {
        return tableNumber;
    }

    public int getTableCapacity() {
        return tableCapacity;
    }

    public void setTableCapacity(int tableCapacity) {
        this.tableCapacity = tableCapacity;
    }

    public boolean isOrdering() {
        return isOrdering;
    }

    public boolean isOrderPlaced() {
        return orderPlaced;
    }
    
    /**
     * Resets the table state after all customers have left.
     * This ensures the table is ready for new customers.
     */
    public void resetTableState() {
        this.orderPlaced = false;
        System.out.println("Table " + tableNumber + " has been reset and is ready for new customers");
    }
}
