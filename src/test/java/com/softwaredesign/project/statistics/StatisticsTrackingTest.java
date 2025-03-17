package com.softwaredesign.project.statistics;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryStockTracker;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.model.BankBalanceSingleton;
import com.softwaredesign.project.model.StatisticsSingleton;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.staff.staffspeeds.BaseSpeed;

/**
 * Test suite to validate the statistics tracking functionality.
 * Tests whether statistics are properly updated at various points in the
 * restaurant simulation.
 */
public class StatisticsTrackingTest {
    private StatisticsSingleton stats;
    private BankBalanceSingleton bankBalance;
    private Inventory inventory;
    private CollectionPoint collectionPoint;
    private StationManager stationManager;
    private OrderManager orderManager;
    private Menu menu;
    private SeatingPlan seatingPlan;
    private InventoryStockTracker inventoryTracker;

    @Before
    public void setUp() {
        // Reset singletons
        StatisticsSingleton.reset();
        BankBalanceSingleton.reset();

        // Get singleton instances
        stats = StatisticsSingleton.getInstance();
        bankBalance = BankBalanceSingleton.getInstance();
        bankBalance.setBankBalance(1000.0);

        // Initialize core components
        inventory = new Inventory();
        collectionPoint = new CollectionPoint();
        stationManager = new StationManager(collectionPoint);
        orderManager = new OrderManager(collectionPoint, stationManager);
        inventoryTracker = new InventoryStockTracker();
        inventory.attach(inventoryTracker);

        // Set up inventory with test ingredients
        inventory.addIngredient("Beef Patty", 10, 5.0, StationType.GRILL);
        inventory.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventory.addIngredient("Lettuce", 10, 0.5, StationType.PREP);

        // Create menu with inventory
        menu = new Menu(inventory);

        // Initialize recipes in the menu (this is necessary for the menu to work
        // properly)
        menu.initializeRecipes();

        // Create seating plan
        seatingPlan = new SeatingPlan(5, 20, 4, menu);
    }

    @After
    public void tearDown() {
        // Reset singletons after each test
        StatisticsSingleton.reset();
        BankBalanceSingleton.reset();
    }

    @Test
    public void testBasicStatTrackingOperations() {
        // Test basic operations
        stats.setStat("testInt", 5);
        stats.setStat("testDouble", 10.5);
        stats.setStat("testString", "hello");

        assertEquals(5, stats.getInt("testInt"));
        assertEquals(10.5, stats.getDouble("testDouble"), 0.001);
        assertEquals("hello", stats.getString("testString"));

        // Test increment operations
        stats.incrementStat("testInt");
        stats.incrementStat("testDouble", 1.5);

        assertEquals(6, stats.getInt("testInt"));
        assertEquals(12.0, stats.getDouble("testDouble"), 0.001);

        // Test convenience methods
        assertEquals(6, stats.getInt("testInt"));
        assertEquals(12.0, stats.getDouble("testDouble"), 0.001);
        assertEquals("hello", stats.getString("testString"));

        // Test contains method
        assertTrue(stats.containsStat("testInt"));
        assertFalse(stats.containsStat("nonExistentStat"));
    }

    @Test
    public void testCustomerSeatingStatistics() {
        // Create customer group
        List<DineInCustomer> customerGroup = new ArrayList<>();
        customerGroup.add(new DineInCustomer());
        customerGroup.add(new DineInCustomer());

        // Verify initial stats
        assertEquals(0, stats.getInt("customersSeated"));
        assertEquals(0, stats.getInt("groupsServed"));

        // Seat the group
        Table table = seatingPlan.findTableForGroup(customerGroup);

        // Check stats after seating
        assertEquals(2, stats.getInt("customersSeated"));
        assertEquals(1, stats.getInt("groupsServed"));

        // Seat a second group
        List<DineInCustomer> secondGroup = new ArrayList<>();
        secondGroup.add(new DineInCustomer());
        Table table2 = seatingPlan.findTableForGroup(secondGroup);

        // Check stats after seating second group
        assertEquals(3, stats.getInt("customersSeated"));
        assertEquals(2, stats.getInt("groupsServed"));
    }

    @Test
    public void testOrderProcessingStatistics() {
        // Create an order
        Order order = new Order("Order-1001");
        TestRecipe recipe = new TestRecipe("Test Burger", inventory);
        order.addRecipes(recipe);

        // Verify initial stats
        assertEquals(0, stats.getInt("ordersReceived"));
        assertEquals(0, stats.getInt("totalRecipesOrdered"));
        assertEquals(0, stats.getInt("ordersProcessed"));

        // Add order to order manager
        orderManager.addOrder(order);

        // Check stats after adding order
        assertEquals(1, stats.getInt("ordersReceived"));
        assertEquals(1, stats.getInt("totalRecipesOrdered"));

        // Process the order
        List<Recipe> processedRecipes = orderManager.processOrder();

        // Check stats after processing
        assertEquals(1, stats.getInt("ordersProcessed"));
        assertEquals(1, processedRecipes.size());
    }

    @Test
    public void testMealCompletionStatistics() {
        // Create an order and register it
        String orderId = "Order-1002";
        Order order = new Order(orderId);
        TestRecipe recipe = new TestRecipe("Test Burger", inventory);
        order.addRecipes(recipe);
        collectionPoint.registerOrder(orderId, 1);

        // Verify initial stats
        assertEquals(0, stats.getInt("mealsCompleted"));
        assertEquals(0, stats.getInt("ordersCompleted"));
        assertEquals(0, stats.getInt("ordersCollected"));

        // Create a completed meal and add it
        Meal meal = new Meal("Test Burger", recipe.getIngredients(), inventory, orderId);
        collectionPoint.addCompletedMeal(meal);

        // Check stats after completing meal
        assertEquals(1, stats.getInt("mealsCompleted"));
        assertEquals(1, stats.getInt("ordersCompleted"));

        // Collect the order
        List<Meal> meals = collectionPoint.collectNextOrder();

        // Check stats after collecting
        assertEquals(1, stats.getInt("ordersCollected"));

        // Verify bank balance was updated
        double expectedRevenue = 0.0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            expectedRevenue += ingredient.getPrice();
        }
        assertEquals(1000.0 + expectedRevenue, bankBalance.getBankBalance(), 0.001);

        // Check revenue via StatisticsSingleton
        assertEquals(1000.0 + expectedRevenue, stats.getTotalRevenue(), 0.001);
    }

    @Test
    public void testTableOrderingStatistics() {
        // Create a table and add customers
        Table table = new Table(1, menu, 4);
        List<DineInCustomer> customers = new ArrayList<>();
        TestCustomer customer1 = new TestCustomer(new TestRecipe("Test Burger 1", inventory));
        TestCustomer customer2 = new TestCustomer(new TestRecipe("Test Burger 2", inventory));
        table.addCustomer(customer1);
        table.addCustomer(customer2);

        // Verify initial stats
        assertEquals(0, stats.getInt("tablesOrdered"));
        assertEquals(0, stats.getInt("recipesOrderedByTables"));

        // Take table order
        List<Recipe> tableOrders = table.takeTableOrder();

        // Check stats after ordering
        assertEquals(1, stats.getInt("tablesOrdered"));
        assertEquals(2, stats.getInt("recipesOrderedByTables"));
    }

    @Test
    public void testTotalRevenueFromBankBalance() {
        // Set initial bank balance
        bankBalance.setBankBalance(1000.0);

        // Verify initial revenue from stats
        assertEquals(1000.0, stats.getTotalRevenue(), 0.001);
        assertEquals("$1000.00", stats.getFormattedTotalRevenue());

        // Update bank balance
        bankBalance.updateBankBalance(250.0);

        // Verify updated revenue from stats
        assertEquals(1250.0, stats.getTotalRevenue(), 0.001);
        assertEquals("$1250.00", stats.getFormattedTotalRevenue());
    }

    @Test
    public void testGetAllStatsFormatted() {
        // Set some stats
        stats.setStat("customersSeated", 10);
        stats.setStat("ordersCompleted", 5);
        bankBalance.setBankBalance(1500.0);

        // Get formatted stats
        Map<String, String> formattedStats = stats.getAllStatsFormatted();

        // Verify formatted stats
        assertEquals("10", formattedStats.get("customersSeated"));
        assertEquals("5", formattedStats.get("ordersCompleted"));
        assertEquals("$1500.00", formattedStats.get("totalRevenue"));
    }

    @Test
    public void testGetStatsSummary() {
        // Set some stats
        stats.setStat("customersSeated", 10);
        stats.setStat("ordersCompleted", 5);
        stats.setStat("testDouble", 15.75);
        bankBalance.setBankBalance(1500.0);

        // Get stats summary
        List<String> summary = stats.getStatsSummary();

        // Verify summary contains expected entries
        boolean foundCustomers = false;
        boolean foundOrders = false;
        boolean foundRevenue = false;
        boolean foundDouble = false;

        for (String entry : summary) {
            if (entry.startsWith("Customers Seated: 10"))
                foundCustomers = true;
            if (entry.startsWith("Orders Completed: 5"))
                foundOrders = true;
            if (entry.startsWith("Total Revenue: $1500.00"))
                foundRevenue = true;
            if (entry.startsWith("Test Double: 15.75"))
                foundDouble = true;
        }

        assertTrue("Customers seated entry not found", foundCustomers);
        assertTrue("Orders completed entry not found", foundOrders);
        assertTrue("Total revenue entry not found", foundRevenue);
        assertTrue("Test double entry not found", foundDouble);
    }

    @Test
    public void testFullSimulationScenario() {
        // Set up a waiter
        Waiter waiter = new Waiter(15.0, new BaseSpeed(1), orderManager, menu, inventoryTracker);

        // 1. Create and seat customers
        List<DineInCustomer> customerGroup = new ArrayList<>();
        TestCustomer customer = new TestCustomer(new TestRecipe("Test Burger", inventory));
        customerGroup.add(customer);

        // Print initial stats
        System.out.println("Before seating - customersSeated: " + stats.getInt("customersSeated"));
        System.out.println("Before seating - groupsServed: " + stats.getInt("groupsServed"));

        // Seat the group
        Table table = seatingPlan.findTableForGroup(customerGroup);
        assertNotNull("Table should be assigned for the customer group", table);

        // Print stats after seating
        System.out.println("After seating - customersSeated: " + stats.getInt("customersSeated"));
        System.out.println("After seating - groupsServed: " + stats.getInt("groupsServed"));

        waiter.assignTable(table);

        // 2. Call table.takeTableOrder() to properly track table ordering statistics
        List<Recipe> tableOrders = table.takeTableOrder();
        assertFalse("Table should have orders", tableOrders.isEmpty());

        // 2b. Take order from table using waiter (for order manager)
        boolean orderTaken = waiter.takeTableOrder(table);
        assertTrue("Order should be taken successfully", orderTaken);

        // Verify order was added to OrderManager
        List<Order> pendingOrders = orderManager.getOrders();
        assertFalse("Should have pending orders", pendingOrders.isEmpty());

        // 3. Process order in the system
        List<Recipe> recipes = orderManager.processOrder();
        assertFalse("Should have recipes to process", recipes.isEmpty());
        Recipe recipe = recipes.get(0);

        // 4. Process the order to create a meal
        Meal meal = new Meal(recipe.getName(), recipe.getIngredients(), inventory, recipe.getOrderId());

        // 5. Add completed meal to collection point
        collectionPoint.addCompletedMeal(meal);

        // 6. Collect the order
        List<Meal> meals = collectionPoint.collectNextOrder();

        // Print all stats before verification
        System.out.println("Final stats:");
        for (String stat : stats.getAllStatsFormatted().keySet()) {
            System.out.println(stat + ": " + stats.getAllStatsFormatted().get(stat));
        }

        // Verify all statistics were updated correctly
        assertEquals(1, stats.getInt("customersSeated"));
        assertEquals(1, stats.getInt("groupsServed"));
        assertEquals(1, stats.getInt("tablesOrdered"));
        assertEquals(1, stats.getInt("recipesOrderedByTables"));
        assertEquals(1, stats.getInt("ordersReceived"));
        assertEquals(1, stats.getInt("totalRecipesOrdered"));
        assertEquals(1, stats.getInt("ordersProcessed"));
        assertEquals(1, stats.getInt("mealsCompleted"));
        assertEquals(1, stats.getInt("ordersCompleted"));
        assertEquals(1, stats.getInt("ordersCollected"));

        // Calculate expected revenue
        double expectedRevenue = 1000.0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            expectedRevenue += ingredient.getPrice();
        }

        // Verify revenue tracking
        assertEquals(expectedRevenue, stats.getTotalRevenue(), 0.001);
    }

    @Test
    public void testResetAllStats() {
        // Set some stats
        stats.setStat("customersSeated", 10);
        stats.setStat("ordersCompleted", 5);

        // Verify stats were set
        assertEquals(10, stats.getInt("customersSeated"));
        assertEquals(5, stats.getInt("ordersCompleted"));

        // Reset all stats
        stats.resetAllStats();

        // Verify stats were reset
        assertEquals(0, stats.getInt("customersSeated"));
        assertEquals(0, stats.getInt("ordersCompleted"));
    }

    /**
     * Helper class for testing recipe functionality
     */
    private class TestRecipe extends Recipe {
        public TestRecipe(String name, Inventory inventory) {
            super(name, inventory);
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
            return new TestRecipe(getName(), (Inventory)inventoryService);
        }
    }

    /**
     * Helper class for testing customer functionality
     */
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