package com.softwaredesign.project;

import static org.junit.Assert.*;
import org.junit.Test;

import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.InventoryStockTracker;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.staff.staffspeeds.BaseSpeed;
import com.softwaredesign.project.staff.staffspeeds.ISpeedComponent;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryService;

public class AppTest {
    //TODO: Tests to add - waiter assignment? Can we handle if more customers come than we can seat gracefully?
    @Test
    public void testSystemInitialization() {
        Inventory  inventory = new Inventory();
        InventoryStockTracker inventoryStockTracker = new InventoryStockTracker();
        inventory.attach(inventoryStockTracker);
        // Null pointer exception on menu if we pass empty inventoryService
        inventory.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventory.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        
        
        Menu menu = new Menu(inventory);

        CollectionPoint collectionPoint = new CollectionPoint();
        StationManager stationManager = new StationManager(collectionPoint);
        OrderManager orderManager = new OrderManager(collectionPoint, stationManager);
        SeatingPlan seatingPlan = new SeatingPlan(5, 40, 15, menu);
        ISpeedComponent baseSpeed = new BaseSpeed();
        Waiter waiter = new Waiter(15.0, baseSpeed, orderManager, menu, inventoryStockTracker);

        assertNotNull("Inventory service should not be null", inventory);
        assertTrue("Bun stock should be positive", inventory.getStock("Bun") > 0);
        
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
        SeatingPlan seatingPlan = new SeatingPlan(5, 40,  4, menu);
        
        for (var table : seatingPlan.getAllTables()) {
            assertTrue(table.getTableCapacity() >= 1);
            assertTrue(table.getTableCapacity() <= 4);
        }
    }
}
