package com.softwaredesign.project.staff;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.model.inventory.Inventory;
import com.softwaredesign.project.model.inventory.InventoryStockTracker;
import com.softwaredesign.project.model.kitchen.Station;
import com.softwaredesign.project.model.kitchen.StationManager;
import com.softwaredesign.project.model.kitchen.StationType;
import com.softwaredesign.project.model.menu.BurgerRecipe;
import com.softwaredesign.project.model.menu.Menu;
import com.softwaredesign.project.model.order.OrderManager;
import com.softwaredesign.project.model.order.Recipe;
import com.softwaredesign.project.model.order.RecipeTask;
import com.softwaredesign.project.model.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.model.orderfulfillment.Table;
import com.softwaredesign.project.model.staff.Chef;
import com.softwaredesign.project.model.staff.Waiter;
import com.softwaredesign.project.model.staff.chefstrategies.ChefStrategy;
import com.softwaredesign.project.model.staff.chefstrategies.LongestQueueFirstStrategy;
import com.softwaredesign.project.model.staff.chefstrategies.SimpleChefStrategy;
import com.softwaredesign.project.model.staff.staffspeeds.BaseSpeed;
import com.softwaredesign.project.model.staff.staffspeeds.ISpeedComponent;

public class StaffTests {
    private Waiter waiter;
    private Chef chef;
    private Menu menu;
    private OrderManager orderManager;

    @Before
    public void setUp() {
        Inventory inventory = new Inventory();
        InventoryStockTracker inventoryStockTracker = new InventoryStockTracker();
        inventory.attach(inventoryStockTracker);
        inventory.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventory.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        
        menu = new Menu(inventory);
        CollectionPoint collectionPoint = new CollectionPoint();
        StationManager stationManager = new StationManager(collectionPoint);
        orderManager = new OrderManager(collectionPoint, stationManager);
        
        // Create staff with base speed
        ISpeedComponent baseSpeed = new BaseSpeed();
        waiter = new Waiter(15.0, orderManager, menu, inventoryStockTracker);
        ChefStrategy simpleStrategy = new SimpleChefStrategy();
        chef = new Chef("Test Chef", 20.0, baseSpeed, simpleStrategy, stationManager);
    }

    @Test
    public void testWaiterTableAssignment() {
        Table table = new Table(1, menu, 4);
        waiter.assignTable(table);
        assertTrue(waiter.getAssignedTables().contains(table));
    }

    @Test
    public void testChefStrategyChange() {
        // Use the LongestQueueFirstStrategy
        ChefStrategy newStrategy = new LongestQueueFirstStrategy();
        chef.setWorkStrategy(newStrategy);
        
        // Create stations of different types
        CollectionPoint collectionPoint = new CollectionPoint();
        Station grillStation = new Station(StationType.GRILL, collectionPoint);
        Station prepStation = new Station(StationType.PREP, collectionPoint);
        
        // Make sure the stations don't have a chef assigned
        grillStation.unregisterChef();
        prepStation.unregisterChef();
        
        // Manually add stations to chef's assigned stations list
        chef.clearStationAssignments();
        chef.addStationAssignment(grillStation);
        chef.addStationAssignment(prepStation);
        
        // First test with no backlog - should return null
        Station nextStation = chef.chooseNextStation();
        // The station might not be null (chef stays visible), but it should have no backlog
        assertTrue("Chef's station should have no backlog when there's no work", 
            nextStation == null || nextStation.getBacklogSize() == 0);
        
        // Now add a task to the grill station's backlog
        RecipeTask grillTask = new RecipeTask("Grill Task", StationType.GRILL, 5);
        
        // Create a properly initialized inventory for the recipe
        Inventory inventory = new Inventory();
        inventory.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventory.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        
        // We need to set a recipe for the task
        Recipe mockRecipe = new BurgerRecipe(inventory);
        mockRecipe.setOrderId("test-order-1");
        grillTask.setRecipe(mockRecipe);
        
        // Add task to grill station
        grillStation.addTask(grillTask);
        
        // Now test with backlog - should return a station
        nextStation = chef.chooseNextStation();
        assertNotNull("Chef should choose a station when there's a backlog", nextStation);
        assertEquals("Chef should choose the station with a backlog", StationType.GRILL, nextStation.getType());
    }

    @Test
    public void testStaffPayRate() {
        assertEquals(15.0, waiter.getPay(), 0.01);
        assertEquals(20.0, chef.getPay(), 0.01);
    }
}