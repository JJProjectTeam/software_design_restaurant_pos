package com.softwaredesign.project.model.staff;

import java.util.ArrayList;
import java.util.List;
import com.softwaredesign.project.model.orderfulfillment.Table;
import java.time.LocalDateTime;
import com.softwaredesign.project.model.placeholders.Recipe;
import com.softwaredesign.project.model.placeholders.OrderManager;
import com.softwaredesign.project.model.placeholders.Order;
import com.softwaredesign.project.model.customer.DineInCustomer;
import com.softwaredesign.project.model.placeholders.Ingredient;
import com.softwaredesign.project.model.menu.Menu;

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
