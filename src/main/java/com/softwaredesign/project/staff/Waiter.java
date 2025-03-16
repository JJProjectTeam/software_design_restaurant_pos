package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.menu.Menu;

import com.softwaredesign.project.staff.staffspeeds.ISpeedComponent;
import com.softwaredesign.project.inventory.InventoryStockTracker;

public class Waiter extends StaffMember {
    private List<Table> assignedTables;
    private OrderManager orderManager;
    private Menu menu;
    private InventoryStockTracker inventoryStockTracker;

    public Waiter(double payPerHour, ISpeedComponent speedDecorator, OrderManager orderManager, Menu menu, InventoryStockTracker inventoryStockTracker) {
        super(payPerHour, speedDecorator);
        this.assignedTables = new ArrayList<>();
        this.orderManager = orderManager;
        this.menu = menu;
        this.inventoryStockTracker = inventoryStockTracker;
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

        String orderId = orderManager.generateOrderId();
        Order tableOrder = new Order(orderId);

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

                if (!inventoryStockTracker.canFulfillOrder(tableOrder.getIngredients())) {
                    throw new IllegalStateException("Not enough ingredients to fulfill the order");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                // TODO: Handle what to do if the order cannot be fulfilled
            }
        }

        orderManager.addOrder(tableOrder);
    }

    public List<Table> getAssignedTables() {
        return new ArrayList<>(assignedTables);
    }
}
