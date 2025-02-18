package com.softwaredesign.project;

import static org.junit.Assert.*;
import org.junit.Test;

import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Station;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Inventory;

public class AppTest {
    @Test
    public void testSystemInitialization() {
        InventoryService inventoryService = new Inventory();
        // Null pointer exception on menu if we pass empty inventoryService
        inventoryService.addIngredient("burger", 10, 5.0, Station.GRILL);
        inventoryService.addIngredient("bun", 20, 1.0, Station.PREP);
        inventoryService.addIngredient("lettuce", 15, 0.5, Station.PREP);
        
        Menu menu = new Menu(inventoryService);
        OrderManager orderManager = new OrderManager();
        SeatingPlan seatingPlan = new SeatingPlan(5, 15, menu);
        Waiter waiter = new Waiter(15.0, 1.0, orderManager, menu);

        assertNotNull("Inventory service should not be null", inventoryService);
        assertTrue("Burger stock should be positive", inventoryService.getStock("burger") > 0);
        
        assertNotNull("Menu should not be null", menu);
        assertNotNull("Order manager should not be null", orderManager);
        assertNotNull("Seating plan should not be null", seatingPlan);
        assertNotNull("Waiter should not be null", waiter);
        
        assertTrue(seatingPlan.getAllTables().size() > 0);
    }

    @Test
    public void testValidSeatingPlanConfiguration() {
        InventoryService inventoryService = new Inventory();
        // Null pointer exception on menu if we pass empty inventoryService
        inventoryService.addIngredient("burger", 10, 5.0, Station.GRILL);
        inventoryService.addIngredient("bun", 20, 1.0, Station.PREP);
        inventoryService.addIngredient("lettuce", 15, 0.5, Station.PREP);
        
        Menu menu = new Menu(inventoryService);
        SeatingPlan seatingPlan = new SeatingPlan(5, 15, menu);
        
        for (var table : seatingPlan.getAllTables()) {
            assertTrue(table.getTableCapacity() >= 1);
            assertTrue(table.getTableCapacity() <= 4);
        }
    }
}
