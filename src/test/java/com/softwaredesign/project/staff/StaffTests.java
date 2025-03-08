package com.softwaredesign.project.staff;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.staff.chefstrategies.*;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;
import com.softwaredesign.project.menu.BurgerRecipe;
import java.util.List;
import java.util.Objects;

public class StaffTests {
    private Waiter waiter;
    private Chef chef;
    private Menu menu;
    private OrderManager orderManager;

    @Before
    public void setUp() {
        InventoryService inventoryService = new Inventory();
        inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        
        menu = new Menu(inventoryService);
        CollectionPoint collectionPoint = new CollectionPoint();
        StationManager stationManager = new StationManager(collectionPoint);
        orderManager = new OrderManager(collectionPoint, stationManager);
        waiter = new Waiter(15.0, 1.0, orderManager, menu);
        chef = new Chef(20.0, 1.5, new ShortestQueueFirst(), stationManager);
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
        chef.getAssignedStations().clear(); // Clear any existing assignments
        chef.getAssignedStations().add(grillStation);
        chef.getAssignedStations().add(prepStation);
        
        // First test with no backlog - should return null
        Station nextStation = chef.chooseNextStation();
        assertNull("Chef should not choose a station when there's no backlog", nextStation);
        
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
        assertEquals(15.0, waiter.getPayPerHour(), 0.01);
        assertEquals(20.0, chef.getPayPerHour(), 0.01);
    }
}