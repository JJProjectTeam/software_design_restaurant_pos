package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.placeholders.Ingredient;
import com.softwaredesign.project.placeholders.Order;
import com.softwaredesign.project.placeholders.OrderManager;
import com.softwaredesign.project.placeholders.Recipe;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.menu.Menu;

public class Waiter extends StaffMember {
    private List<Table> assignedTables;
    private OrderManager orderManager;
    private Menu menu;

    public Waiter(double payPerHour, double speedMultiplier, OrderManager orderManager, Menu menu) {
        super(payPerHour, speedMultiplier);
        this.assignedTables = new ArrayList<>();
        this.orderManager = orderManager;
        this.menu = menu;
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

        Order tableOrder = new Order(LocalDateTime.now());
        
        for (DineInCustomer customer : table.getCustomers()) {
            Recipe customerRecipe = customer.selectRecipeFromMenu(menu);
            customer.requestRecipeModification(menu);
            tableOrder.addRecipe(customerRecipe);
            for (Ingredient ingredient : customer.getRemovedIngredients()) {
                tableOrder.addModification(customerRecipe, ingredient, false);
            }
            for (Ingredient ingredient : customer.getAddedIngredients()) {
                tableOrder.addModification(customerRecipe, ingredient, true);
            }
        }
    

        orderManager.submitOrder(tableOrder);
    }

    public List<Table> getAssignedTables() {
        return new ArrayList<>(assignedTables);
    }
}
