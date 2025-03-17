package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.menu.Menu;

import com.softwaredesign.project.inventory.InventoryStockTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Waiter extends StaffMember {
    private static final Logger logger = LoggerFactory.getLogger(Waiter.class);
    private List<Table> assignedTables;
    private OrderManager orderManager;
    private Menu menu;
    private InventoryStockTracker inventoryStockTracker;

    public Waiter(double payPerHour, OrderManager orderManager, Menu menu,
            InventoryStockTracker inventoryStockTracker) {
        super(payPerHour);
        this.assignedTables = new ArrayList<>();
        this.orderManager = orderManager;
        this.menu = menu;
        this.inventoryStockTracker = inventoryStockTracker;
        
        // Set the inventory stock tracker in the order manager if not already set
        if (orderManager != null) {
            orderManager.setInventoryStockTracker(inventoryStockTracker);
        }
    }

    public void assignTable(Table table) {
        assignedTables.add(table);
    }

    /**
     * Takes an order from a table and adds it to the OrderManager.
     * @param table The table to take an order from
     */
    public boolean takeTableOrder(Table table) {
        if (!assignedTables.contains(table)) {
            throw new IllegalArgumentException("This table is not assigned to this waiter");
        }

        if (!table.isEveryoneReadyToOrder()) {
            throw new IllegalStateException("Not everyone at the table is ready to order");
        }

        String orderId = orderManager.generateOrderId();
        Order tableOrder = new Order(orderId);

        // First collect all orders without checking inventory
        for (DineInCustomer customer : table.getCustomers()) {
            try {
                Recipe customerRecipe = customer.selectRecipeFromMenu(menu);
                customer.requestRecipeModification(menu);
                tableOrder.addRecipes(customerRecipe);
                for (Ingredient ingredient : customer.getRemovedIngredients()) {
                    tableOrder.addModification(customerRecipe, ingredient, false);
                }
                for (Ingredient ingredient : customer.getAddedIngredients()) {
                    tableOrder.addModification(customerRecipe, ingredient, true);
                }
            } catch (IllegalArgumentException e) {
                logger.info(e.getMessage());
                return false;
            }
        }
        
        // Check if we can reserve all required ingredients
        if (!inventoryStockTracker.reserveIngredientsForOrder(orderId, tableOrder.getIngredients())) {
            logger.info("Not enough ingredients to fulfill the order");
            return false;
        }

        // Ingredients are reserved, so add the order to the order manager
        orderManager.addOrder(tableOrder);
        
        // Mark the table's order as placed
        table.markOrderPlaced();
        return true;
    }
    
    /**
     * Takes an order from a table and returns the order ID.
     * @param table The table to take an order from
     * @return The ID of the created order, or null if the order could not be placed
     */
    public String takeTableOrderAndReturnId(Table table) {
        if (!assignedTables.contains(table)) {
            throw new IllegalArgumentException("This table is not assigned to this waiter");
        }

        if (!table.isEveryoneReadyToOrder()) {
            throw new IllegalStateException("Not everyone at the table is ready to order");
        }

        String orderId = orderManager.generateOrderId();
        Order tableOrder = new Order(orderId);

        for (DineInCustomer customer : table.getCustomers()) {
            Recipe customerRecipe = customer.selectRecipeFromMenu(menu);
            if (customerRecipe == null) {
                return null;
            }
            customer.requestRecipeModification(menu); // TODO: does this work?
            tableOrder.addRecipes(customerRecipe);
            for (Ingredient ingredient : customer.getRemovedIngredients()) {
                tableOrder.addModification(customerRecipe, ingredient, false);
            }
            for (Ingredient ingredient : customer.getAddedIngredients()) {
                tableOrder.addModification(customerRecipe, ingredient, true);
            }
        }
        
        // Check if we can reserve all required ingredients
        if (!inventoryStockTracker.reserveIngredientsForOrder(orderId, tableOrder.getIngredients())) {
            logger.info("Not enough ingredients to fulfill the order");
            // Release any reservations we might have made (though there shouldn't be any)
            inventoryStockTracker.releaseReservedIngredients(orderId);
            return null;
        }

        orderManager.addOrder(tableOrder);
        
        // Mark the table's order as placed
        table.markOrderPlaced();
        
        return orderId;
    }
    
    /**
     * Delivers a meal to a specific table.
     * @param meal The meal to deliver
     * @param table The table to deliver to
     */
    public void deliverMealToTable(Meal meal, Table table) {
        if (!assignedTables.contains(table)) {
            throw new IllegalArgumentException("This table is not assigned to this waiter");
        }
        
        System.out.println("Waiter delivering meal to table " + table.getTableNumber());
        
        // Add the meal to the table's pending meals queue
        table.addPendingMeal(meal);
    }

    public List<Table> getAssignedTables() {
        return new ArrayList<>(assignedTables);
    }
}
