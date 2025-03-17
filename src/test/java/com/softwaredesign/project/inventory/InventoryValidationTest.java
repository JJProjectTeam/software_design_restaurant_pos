package com.softwaredesign.project.inventory;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import java.util.*;
import java.util.concurrent.*;

import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.kitchen.StationType;

/**
 * Test suite to validate the inventory validation logic when placing orders.
 * Tests whether orders are rejected when there are insufficient ingredients.
 */
public class InventoryValidationTest {
    private Inventory inventory;
    private InventoryStockTracker inventoryTracker;
    private OrderManager orderManager;
    private Menu menu;
    private CollectionPoint collectionPoint;
    private StationManager stationManager;
    private Table table;

    @Before
    public void setUp() {
        // Initialize services
        inventory = new Inventory();
        collectionPoint = new CollectionPoint();
        stationManager = new StationManager(collectionPoint);
        inventoryTracker = new InventoryStockTracker();
        orderManager = new OrderManager(collectionPoint, stationManager, inventoryTracker);

        // Set up inventory observer
        inventory.attach(inventoryTracker);

        // Initialize inventory with test data
        inventory.addIngredient("Beef Patty", 0, 5.0, StationType.GRILL); // Depleted ingredient
        inventory.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Lettuce", 10, 0.5, StationType.PREP);

        // Create menu with inventory
        menu = new Menu(inventory);

        // Create table with menu and capacity
        table = new Table(1, menu, 4); // Table number 1 with capacity 4
    }

    @Test
    public void testOrderRejectionWithInsufficientIngredient() {
        // Create test recipe
        TestRecipe testRecipe = new TestRecipe("Test Burger", inventory);

        TestCustomer customer = new TestCustomer(testRecipe);
        table.addCustomer(customer);

        // Create waiter with necessary components
        Waiter waiter = new Waiter(15.0, orderManager, menu, inventoryTracker);
        waiter.assignTable(table);

        // Attempt to take order - should return false with our new implementation
        boolean orderPlaced = waiter.takeTableOrder(table);
        
        // Verify the order was rejected
        assertFalse("Order should be rejected due to insufficient ingredients", orderPlaced);

        // Verify inventory remains unchanged
        assertEquals(0, inventoryTracker.getCurrentStock("Beef Patty"));
        assertEquals(10, inventoryTracker.getCurrentStock("Bun"));
        assertEquals(10, inventoryTracker.getCurrentStock("Lettuce"));
    }

    @Test
    public void testSuccessfulOrderWithSufficientIngredients() {
        // Update inventory to have sufficient ingredients
        inventory.addIngredient("Beef Patty", 5, 5.0, StationType.GRILL); // Add more beef patties

        // Create test recipe
        TestRecipe testRecipe = new TestRecipe("Test Burger", inventory);

        // Create test customer with the recipe
        TestCustomer customer = new TestCustomer(testRecipe);
        table.addCustomer(customer);

        // Create waiter with necessary components
        Waiter waiter = new Waiter(15.0, orderManager, menu, inventoryTracker);
        waiter.assignTable(table);

        // Take the order - should succeed
        boolean orderPlaced = waiter.takeTableOrder(table);
        assertTrue("Order should be placed successfully", orderPlaced);

        // Process the order to build the meal
        List<Recipe> recipes = orderManager.processOrder();
        for (Recipe recipe : recipes) {
            recipe.buildMeal(); // This will consume the ingredients
        }

        // Verify inventory was updated correctly
        assertEquals(4, inventoryTracker.getCurrentStock("Beef Patty")); // One patty used
        assertEquals(9, inventoryTracker.getCurrentStock("Bun")); // One bun used
        assertEquals(9, inventoryTracker.getCurrentStock("Lettuce")); // One lettuce used
    }

    @Test
    public void testMultipleCustomerOrdersWithSufficientIngredients() {
        // Update inventory to have sufficient ingredients for multiple orders
        inventory.addIngredient("Beef Patty", 5, 5.0, StationType.GRILL);

        // Create test recipes for two customers
        TestRecipe recipe1 = new TestRecipe("Test Burger 1", inventory);
        TestRecipe recipe2 = new TestRecipe("Test Burger 2", inventory);

        // Add two customers to the table
        TestCustomer customer1 = new TestCustomer(recipe1);
        TestCustomer customer2 = new TestCustomer(recipe2);
        table.addCustomer(customer1);
        table.addCustomer(customer2);

        // Create waiter and take order
        Waiter waiter = new Waiter(15.0, orderManager, menu, inventoryTracker);
        waiter.assignTable(table);
        boolean orderPlaced = waiter.takeTableOrder(table);
        assertTrue("Order should be placed successfully", orderPlaced);

        // Process the orders
        List<Recipe> recipes = orderManager.processOrder();
        for (Recipe recipe : recipes) {
            recipe.buildMeal();
        }

        // Verify inventory was updated for both orders
        assertEquals(3, inventoryTracker.getCurrentStock("Beef Patty")); // Two patties used
        assertEquals(8, inventoryTracker.getCurrentStock("Bun")); // Two buns used
        assertEquals(8, inventoryTracker.getCurrentStock("Lettuce")); // Two lettuce used
    }

    @Test
    public void testOrderRejectionWhenOneIngredientRunsOut() {
        // Set up inventory with just enough for one order
        inventory.addIngredient("Beef Patty", 1, 5.0, StationType.GRILL);

        // First order should succeed
        TestRecipe recipe1 = new TestRecipe("Test Burger 1", inventory);
        TestCustomer customer1 = new TestCustomer(recipe1);
        table.addCustomer(customer1);

        Waiter waiter = new Waiter(15.0, orderManager, menu, inventoryTracker);
        waiter.assignTable(table);
        boolean firstOrderPlaced = waiter.takeTableOrder(table);
        assertTrue("First order should be placed successfully", firstOrderPlaced);

        // Process first order
        List<Recipe> recipes = orderManager.processOrder();
        for (Recipe recipe : recipes) {
            recipe.buildMeal();
        }

        // Verify first order consumed ingredients
        assertEquals(0, inventoryTracker.getCurrentStock("Beef Patty"));
        assertEquals(9, inventoryTracker.getCurrentStock("Bun"));
        assertEquals(9, inventoryTracker.getCurrentStock("Lettuce"));

        // Create new table for second order
        Table table2 = new Table(2, menu, 4);
        TestRecipe recipe2 = new TestRecipe("Test Burger 2", inventory);
        TestCustomer customer2 = new TestCustomer(recipe2);
        table2.addCustomer(customer2);
        waiter.assignTable(table2);

        // Second order should fail due to no beef patties
        boolean secondOrderPlaced = waiter.takeTableOrder(table2);
        assertFalse("Second order should be rejected due to insufficient ingredients", secondOrderPlaced);

        // Verify inventory remains unchanged after failed order
        assertEquals(0, inventoryTracker.getCurrentStock("Beef Patty"));
        assertEquals(9, inventoryTracker.getCurrentStock("Bun"));
        assertEquals(9, inventoryTracker.getCurrentStock("Lettuce"));
    }

    @Test
    public void testLowInventoryEdgeCase() {
        // Set up inventory with exactly enough for one order
        inventory.addIngredient("Beef Patty", 1, 5.0, StationType.GRILL);
        inventory.addIngredient("Bun", 1, 1.0, StationType.PREP);
        inventory.addIngredient("Lettuce", 1, 0.5, StationType.PREP);

        // Order should succeed when we have exactly enough
        TestRecipe testRecipe = new TestRecipe("Test Burger", inventory);
        TestCustomer customer = new TestCustomer(testRecipe);
        table.addCustomer(customer);

        Waiter waiter = new Waiter(15.0, orderManager, menu, inventoryTracker);
        waiter.assignTable(table);
        boolean orderPlaced = waiter.takeTableOrder(table);
        assertTrue("Order should be placed successfully with exactly enough ingredients", orderPlaced);

        // Process the order
        List<Recipe> recipes = orderManager.processOrder();
        for (Recipe currentRecipe : recipes) {
            currentRecipe.buildMeal();
        }

        // Verify all ingredients were used
        assertEquals(0, inventoryTracker.getCurrentStock("Beef Patty"));
        assertEquals(0, inventoryTracker.getCurrentStock("Bun"));
        assertEquals(0, inventoryTracker.getCurrentStock("Lettuce"));
    }

    @Test
    public void testConcurrentOrdersWithLimitedIngredients() throws InterruptedException, ExecutionException {
        // Set up inventory with just enough ingredients for ONE order
        inventory.addIngredient("Beef Patty", 1, 5.0, StationType.GRILL);
        
        // Create two tables with customers who both want burgers
        Table table1 = new Table(1, menu, 1);
        TestRecipe recipe1 = new TestRecipe("Test Burger 1", inventory);
        TestCustomer customer1 = new TestCustomer(recipe1);
        table1.addCustomer(customer1);
        
        Table table2 = new Table(2, menu, 1);
        TestRecipe recipe2 = new TestRecipe("Test Burger 2", inventory);
        TestCustomer customer2 = new TestCustomer(recipe2);
        table2.addCustomer(customer2);
        
        // Create two waiters
        Waiter waiter1 = new Waiter(15.0, orderManager, menu, inventoryTracker);
        waiter1.assignTable(table1);
        
        Waiter waiter2 = new Waiter(15.0, orderManager, menu, inventoryTracker);
        waiter2.assignTable(table2);
        
        // Use a CountDownLatch to make both waiters try to place orders at the same time
        CountDownLatch startLatch = new CountDownLatch(1);
        
        // Set up a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Create the tasks that will run concurrently
        Callable<Boolean> waiter1Task = () -> {
            startLatch.await(); // Wait for the signal to start
            return waiter1.takeTableOrder(table1);
        };
        
        Callable<Boolean> waiter2Task = () -> {
            startLatch.await(); // Wait for the signal to start
            return waiter2.takeTableOrder(table2);
        };
        
        // Submit tasks to the executor
        Future<Boolean> waiter1Future = executor.submit(waiter1Task);
        Future<Boolean> waiter2Future = executor.submit(waiter2Task);
        
        // Start both tasks simultaneously
        startLatch.countDown();
        
        // Get the results (blocks until both tasks complete)
        boolean waiter1Success = waiter1Future.get();
        boolean waiter2Success = waiter2Future.get();
        
        // Shutdown the executor
        executor.shutdown();
        
        // Verify that exactly one order succeeded and one failed
        assertTrue("Either waiter1 or waiter2 should succeed, but not both", 
                  (waiter1Success && !waiter2Success) || (!waiter1Success && waiter2Success));
        
        // Process any successful orders
        List<Recipe> recipes = orderManager.processOrder();
        if (!recipes.isEmpty()) {
            for (Recipe recipe : recipes) {
                recipe.buildMeal();
            }
        }
        
        // Verify the ingredient was consumed
        assertEquals(0, inventoryTracker.getCurrentStock("Beef Patty"));
    }

    private class TestRecipe extends Recipe {
        public TestRecipe(String name, InventoryService inventoryService) {
            super(name, inventoryService);
        }

        @Override
        protected void initializeBaseIngredients() {
            addIngredient(new Ingredient("Beef Patty", inventoryService));
            addIngredient(new Ingredient("Bun", inventoryService));
            addIngredient(new Ingredient("Lettuce", inventoryService));
        }

        @Override
        protected void initializeTasks() {
            // No tasks needed for testing
        }
        
        @Override
        public Recipe copy() {
            return new TestRecipe(getName(), inventoryService);
        }
    }

    private class TestCustomer extends DineInCustomer {
        private Recipe recipeToSelect;

        public TestCustomer(Recipe recipeToSelect) {
            super();
            this.recipeToSelect = recipeToSelect;
            finishBrowsing(); // Make sure the customer is ready to order
        }

        @Override
        public Recipe selectRecipeFromMenu(Menu menu) {
            return recipeToSelect;
        }

        @Override
        public void requestRecipeModification(Menu menu) {
            // Do nothing - we don't want random modifications in our test
        }

        @Override
        public List<Ingredient> getRemovedIngredients() {
            return new ArrayList<>();
        }

        @Override
        public List<Ingredient> getAddedIngredients() {
            return new ArrayList<>();
        }
    }
}
