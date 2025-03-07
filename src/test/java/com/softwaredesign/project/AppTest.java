package com.softwaredesign.project;

import static org.junit.Assert.*;
import org.junit.Test;

import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;

public class AppTest {
    //TODO: Tests to add - waiter assignment? Can we handle if more customers come than we can seat gracefully?
    @Test
    public void testSystemInitialization() {
        InventoryService inventoryService = new Inventory();
        // Null pointer exception on menu if we pass empty inventoryService
        inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        
        
        Menu menu = new Menu(inventoryService);

        CollectionPoint collectionPoint = new CollectionPoint();
        StationManager stationManager = new StationManager();
        OrderManager orderManager = new OrderManager(collectionPoint, stationManager);
        SeatingPlan seatingPlan = new SeatingPlan(5, 15, menu);
        Waiter waiter = new Waiter(15.0, 1.0, orderManager, menu);

        assertNotNull("Inventory service should not be null", inventoryService);
        assertTrue("Bun stock should be positive", inventoryService.getStock("Bun") > 0);
        
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
        inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        
        
        Menu menu = new Menu(inventoryService);
        SeatingPlan seatingPlan = new SeatingPlan(5, 15, menu);
        
        for (var table : seatingPlan.getAllTables()) {
            assertTrue(table.getTableCapacity() >= 1);
            assertTrue(table.getTableCapacity() <= 4);
        }
    }
}
