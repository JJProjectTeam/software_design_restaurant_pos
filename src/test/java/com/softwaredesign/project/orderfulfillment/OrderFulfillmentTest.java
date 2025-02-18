package com.softwaredesign.project.orderfulfillment;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;


public class OrderFulfillmentTest {
    private OrderManager orderManager;
    private CollectionPoint collectionPoint;
    private Kitchen kitchen;
    private Waiter waiter;
    private InventoryService inventoryService;
    private Menu menu;

    @Before
    public void setUp() {
        // Create ingredients for inventory: 
        inventoryService = new Inventory();
        inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Ketchup", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Onion", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Pickle", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Mayo", 10, 0.5, StationType.PREP);

        collectionPoint = new CollectionPoint();
        StationManager stationManager = new StationManager();
        orderManager = new OrderManager(collectionPoint, stationManager);
        kitchen = new Kitchen(orderManager, inventoryService, collectionPoint);
        menu = new Menu(inventoryService);
        waiter = new Waiter(15.0, 1.0, orderManager, menu);
    }

    @Test
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
        waiter.takeTableOrder(table);

        // Kitchen processes the order
        kitchen.prepareRecipes();

        // Check CollectionPoint for completed order
        assertTrue("Should have ready orders", collectionPoint.hasReadyOrders());
        
        // Collect the completed order
        List<Meal> completedMeals = collectionPoint.collectNextOrder();
        
        // Verify the order
        assertNotNull("Completed meals should not be null", completedMeals);
        assertEquals("Should have 2 meals", 2, completedMeals.size());
        
        // Verify all meals have the same orderId
        String orderId = completedMeals.get(0).getOrderId();
        for (Meal meal : completedMeals) {
            assertEquals("All meals should have the same orderId", orderId, meal.getOrderId());
        }
    }
}