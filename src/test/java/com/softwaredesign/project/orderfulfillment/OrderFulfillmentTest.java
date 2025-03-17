package com.softwaredesign.project.orderfulfillment;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import com.softwaredesign.project.model.customer.DineInCustomer;
import com.softwaredesign.project.model.inventory.Inventory;
import com.softwaredesign.project.model.inventory.InventoryService;
import com.softwaredesign.project.model.inventory.InventoryStockTracker;
import com.softwaredesign.project.model.kitchen.Kitchen;
import com.softwaredesign.project.model.kitchen.StationManager;
import com.softwaredesign.project.model.kitchen.StationType;
import com.softwaredesign.project.model.menu.BurgerRecipe;
import com.softwaredesign.project.model.menu.Menu;
import com.softwaredesign.project.model.order.Meal;
import com.softwaredesign.project.model.order.Order;
import com.softwaredesign.project.model.order.OrderManager;
import com.softwaredesign.project.model.order.Recipe;
import com.softwaredesign.project.model.order.RecipeTask;
import com.softwaredesign.project.model.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.model.orderfulfillment.Table;
import com.softwaredesign.project.model.staff.Waiter;

public class OrderFulfillmentTest {
    private OrderManager orderManager;
    private CollectionPoint collectionPoint;
    private Kitchen kitchen;
    private Waiter waiter;
    private Inventory inventory;
    private Menu menu;
    private InventoryStockTracker inventoryStockTracker;

    @Before
    public void setUp() {
        // Create ingredients for inventory: 
        inventory = new Inventory();
        inventoryStockTracker = new InventoryStockTracker();
        inventory.attach(inventoryStockTracker);
        inventory.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventory.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        inventory.addIngredient("Ketchup", 10, 0.5, StationType.PREP);
        inventory.addIngredient("Onion", 10, 0.5, StationType.PREP);
        inventory.addIngredient("Pickle", 10, 0.5, StationType.PREP);
        inventory.addIngredient("Mayo", 10, 0.5, StationType.PREP);

        collectionPoint = new CollectionPoint();
        StationManager stationManager = new StationManager(collectionPoint);
        orderManager = new OrderManager(collectionPoint, stationManager);
        kitchen = new Kitchen(orderManager,  collectionPoint, stationManager);
        menu = new Menu(inventory);
        
        // Create staff members
        waiter = new Waiter(15.0, orderManager, menu, inventoryStockTracker);
    }

    @Test(expected = NullPointerException.class)
    public void testOrderFulfillmentFlow() {
        // Create a table with customers
        Table table = new Table(1, menu, 4);
        DineInCustomer customer1 = new DineInCustomer();
        DineInCustomer customer2 = new DineInCustomer();
        table.addCustomer(customer1);
        table.addCustomer(customer2);
        
        // Assign table to waiter
        waiter.assignTable(table);

        // Take order
        customer1.finishBrowsing();
        customer2.finishBrowsing();
        customer1.setSelectedRecipe(new BurgerRecipe(inventory));
        customer2.setSelectedRecipe(new BurgerRecipe(inventory));

        // Expecting a NullPointerException due to a null Recipe being processed
        waiter.takeTableOrder(table);
    }
    @Test
    public void testOrderManagerToCompletionFlow() {
        // Generate an orderId and create an order manually.
        String orderId = orderManager.generateOrderId();
        Order order = new Order(orderId);
        
        // Create two valid recipes.
        Recipe recipe1 = new BurgerRecipe(inventory);
        Recipe recipe2 = new BurgerRecipe(inventory);
        
        // Add the recipes to the order.
        order.addRecipes(recipe1, recipe2);
        
        // Add the order to the OrderManager.
        orderManager.addOrder(order);
        
        // Process the order via OrderManager.
        List<Recipe> recipes = orderManager.processOrder();
        assertNotNull("Recipes list should not be null", recipes);
        assertEquals("There should be 2 recipes", 2, recipes.size());
        for (Recipe r : recipes) {
            assertEquals("Order ID should match", orderId, r.getOrderId());
        }
        
        // Let the kitchen process the order.
        kitchen.getRecipes();
        
        // Simulate completing all tasks for each recipe and build meals.
        for (Recipe r : recipes) {
            for (RecipeTask task : r.getTasks()) {
                task.setCompleted(true);
            }
            if (r.allTasksCompleted()) {
                Meal m = r.buildMeal();
                collectionPoint.addCompletedMeal(m);
            }
        }
        
        // Verify that the collection point has the completed order.
        assertTrue("Collection point should have ready orders", collectionPoint.hasReadyOrders());
        List<Meal> meals = collectionPoint.collectNextOrder();
        assertNotNull("Collected meals should not be null", meals);
        assertEquals("There should be 2 meals in the order", 2, meals.size());
        for (Meal m : meals) {
            assertEquals("Meal order id should match order id", orderId, m.getOrderId());
        }
    }
}