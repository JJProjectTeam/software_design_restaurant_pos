package com.softwaredesign.project.orderfulfillment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.staff.Waiter;

/**
 * Manages the restaurant floor operations in a tick-based system.
 * Responsible for tables, customers, and waiters.
 */
public class FloorManager extends Entity {
    private SeatingPlan seatingPlan;
    private List<Waiter> waiters;
    private OrderManager orderManager;
    private CollectionPoint collectionPoint;
    
    // Tracking variables for operations
    private List<Table> tablesToProcess;
    
    // Map to track which order belongs to which table
    private Map<String, Table> orderToTableMap;
    
    // List of meals waiting to be delivered to tables
    private Map<Table, List<Meal>> mealsToDeliver;
    
    // Random number generator for customer spawning
    private Random random = new Random();
    
    // Configuration values
    private int maxGroupSize = 10; // Default value, will be overridden from config
    
    public FloorManager(SeatingPlan seatingPlan, List<Waiter> waiters, OrderManager orderManager) {
        this.seatingPlan = seatingPlan;
        this.waiters = new ArrayList<>(waiters);
        this.orderManager = orderManager;
        this.tablesToProcess = new ArrayList<>();
        this.orderToTableMap = new HashMap<>();
        this.mealsToDeliver = new HashMap<>();
        
        // Load configuration values
        loadConfigValues();
    }
    
    /**
     * Loads configuration values from config.json
     */
    private void loadConfigValues() {
        try {
            String configPath = "src/main/config.json";
            String jsonContent = Files.readString(Paths.get(configPath));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode config = mapper.readTree(jsonContent);
            
            // Get maxGroupSize from config
            this.maxGroupSize = config.path("diningRoomRules").path("maxGroupSize").asInt(10);
            System.out.println("[FloorManager] Loaded maxGroupSize from config: " + maxGroupSize);
        } catch (Exception e) {
            System.out.println("[FloorManager] Error loading config values: " + e.getMessage());
            // Keep default values if config loading fails
        }
    }
    
    /**
     * Spawns a random number of customers and attempts to seat them.
     * This method creates a group of 0 to maxGroupSize customers and tries to find a table for them.
     */
    public void spawnCustomers() {
        // Generate a random group size between 0 and maxGroupSize
        int groupSize = random.nextInt(maxGroupSize + 1);
        
        if (groupSize > 0) {
            System.out.println("[FloorManager] Attempting to spawn a group of " + groupSize + " customers");
            
            // Create the customer group
            List<DineInCustomer> customerGroup = new ArrayList<>();
            for (int i = 0; i < groupSize; i++) {
                customerGroup.add(new DineInCustomer());
            }
            
            // Try to seat the group
            Table table = seatCustomers(customerGroup);
            
            if (table != null) {
                System.out.println("[FloorManager] Seated a group of " + groupSize + 
                                  " customers at table " + table.getTableNumber());
            } else {
                System.out.println("[FloorManager] Could not find a table for a group of " + 
                                  groupSize + " customers");
            }
        }
    }
    
    /**
     * Sets the CollectionPoint for the FloorManager to collect completed orders.
     * @param collectionPoint The CollectionPoint to use
     */
    public void setCollectionPoint(CollectionPoint collectionPoint) {
        this.collectionPoint = collectionPoint;
    }
    
    /**
     * Assigns a waiter to a table.
     * @param waiter The waiter to assign
     * @param table The table to assign the waiter to
     */
    public void assignWaiterToTable(Waiter waiter, Table table) {
        waiter.assignTable(table);
    }
    
    /**
     * Seats a group of customers at an available table.
     * @param customers The customers to seat
     * @return The table where customers were seated, or null if no suitable table available
     */
    public Table seatCustomers(List<DineInCustomer> customers) {
        return seatingPlan.findTableForGroup(customers);
    }
    
    /**
     * Seats a single customer at an available table.
     * @param customer The customer to seat
     * @return The table where the customer was seated, or null if no suitable table available
     */
    public Table seatCustomer(DineInCustomer customer) {
        List<DineInCustomer> singleCustomer = new ArrayList<>();
        singleCustomer.add(customer);
        return seatCustomers(singleCustomer);
    }
    
    /**
     * Gets a list of tables that have customers ready to order.
     * @return List of tables with customers ready to order
     */
    public List<Table> getTablesReadyToOrder() {
        List<Table> readyTables = new ArrayList<>();
        for (Table table : seatingPlan.getAllTables()) {
            if (!table.getCustomers().isEmpty() && !table.isOrderPlaced() && table.isEveryoneReadyToOrder()) {
                readyTables.add(table);
            }
        }
        return readyTables;
    }
    
    /**
     * Takes orders from tables where customers are ready.
     */
    public void processReadyTables() {
        List<Table> readyTables = getTablesReadyToOrder();
        for (Table table : readyTables) {
            // Find an available waiter
            Waiter waiter = getAvailableWaiter();
            if (waiter != null) {
                // Assign waiter if table doesn't have one
                if (!waiter.getAssignedTables().contains(table)) {
                    waiter.assignTable(table);
                }
                
                try {
                    // Take the order and remember which table it belongs to
                    String orderId = waiter.takeTableOrderAndReturnId(table);
                    if (orderId != null) {
                        orderToTableMap.put(orderId, table);
                        System.out.println("Waiter took order " + orderId + " from table " + table.getTableNumber());
                    }
                } catch (IllegalStateException e) {
                    System.out.println("Could not take order from table " + table.getTableNumber() + ": " + e.getMessage());
                }
            } else {
                System.out.println("No available waiter to take order from table " + table.getTableNumber());
                // Mark table for processing in next tick
                if (!tablesToProcess.contains(table)) {
                    tablesToProcess.add(table);
                }
            }
        }
    }
    
    /**
     * Collects any completed orders from the CollectionPoint.
     */
    private void collectCompletedOrders() {
        if (collectionPoint == null) {
            System.out.println("[FloorManager] No CollectionPoint set, cannot collect completed orders");
            return;
        }
        
        while (collectionPoint.hasReadyOrders()) {
            List<Meal> completedMeals = collectionPoint.collectNextOrder();
            if (completedMeals != null && !completedMeals.isEmpty()) {
                String orderId = completedMeals.get(0).getOrderId();
                Table targetTable = orderToTableMap.get(orderId);
                
                if (targetTable != null) {
                    System.out.println("[FloorManager] Collected completed order " + orderId + 
                                      " for table " + targetTable.getTableNumber());
                    
                    // Add meals to the delivery queue
                    if (!mealsToDeliver.containsKey(targetTable)) {
                        mealsToDeliver.put(targetTable, new ArrayList<>());
                    }
                    mealsToDeliver.get(targetTable).addAll(completedMeals);
                } else {
                    System.out.println("[FloorManager] Warning: Collected order " + orderId + 
                                      " but couldn't find which table it belongs to");
                }
            }
        }
    }
    
    /**
     * Delivers ready meals to tables using available waiters.
     */
    private void deliverMealsToTables() {
        if (mealsToDeliver.isEmpty()) {
            return;
        }
        
        // Process one table at a time to ensure fairness
        List<Table> tablesWithMeals = new ArrayList<>(mealsToDeliver.keySet());
        
        for (Table table : tablesWithMeals) {
            List<Meal> meals = mealsToDeliver.get(table);
            if (meals == null || meals.isEmpty()) {
                continue;
            }
            
            Waiter waiter = getWaiterForTable(table);
            if (waiter != null) {
                List<Meal> mealsToRemove = new ArrayList<>();
                
                for (Meal meal : meals) {
                    waiter.deliverMealToTable(meal, table);
                    mealsToRemove.add(meal);
                }
                
                meals.removeAll(mealsToRemove);
                if (meals.isEmpty()) {
                    mealsToDeliver.remove(table);
                }
            } else {
                System.out.println("[FloorManager] No waiter available to deliver meals to table " + 
                                  table.getTableNumber());
            }
        }
    }
    
    /**
     * Gets a waiter that is assigned to the given table, or any available waiter if none is assigned.
     * @param table The table to find a waiter for
     * @return A waiter that can serve the table, or null if none available
     */
    private Waiter getWaiterForTable(Table table) {
        // First, try to find a waiter already assigned to this table
        for (Waiter waiter : waiters) {
            if (waiter.getAssignedTables().contains(table)) {
                return waiter;
            }
        }
        
        // If no waiter is assigned, use any available waiter
        return getAvailableWaiter();
    }
    
    /**
     * Finds an available waiter from the staff.
     * @return An available waiter, or null if none available
     */
    private Waiter getAvailableWaiter() {
        // Simple implementation - return the first waiter
        // In a more complex system, this could check workload, proximity to table, etc.
        return waiters.isEmpty() ? null : waiters.get(0);
    }
    
    /**
     * Update customer browsing status. In a real application, this would consider factors
     * like how long they've been browsing, customer profile, etc.
     */
    private void updateCustomerBrowsingStatus() {
        for (Table table : seatingPlan.getAllTables()) {
            for (DineInCustomer customer : table.getCustomers()) {
                if (!customer.isDoneBrowsing()) {
                    // Simple logic - 50% chance to finish browsing each tick
                    if (Math.random() > 0.5) {
                        customer.finishBrowsing();
                        System.out.println("Customer at table " + table.getTableNumber() + " finished browsing");
                    }
                }
            }
        }
    }
    
    /**
     * Prompts customers to eat their meals if they have been delivered.
     */
    private void promptCustomersToEat() {
        for (Table table : seatingPlan.getAllTables()) {
            if (table.hasPendingMeals()) {
                List<DineInCustomer> customersToRemove = new ArrayList<>();
                
                for (DineInCustomer customer : table.getCustomers()) {
                    Meal meal = table.getNextPendingMeal();
                    if (meal != null) {
                        customer.eatMeal(meal);
                        System.out.println("Customer at table " + table.getTableNumber() + 
                                         " is eating " + meal.getName());
                        
                        // After eating, customer will leave
                        customersToRemove.add(customer);
                    }
                }
                
                // Remove customers who have eaten
                for (DineInCustomer customer : customersToRemove) {
                    table.removeCustomer(customer);
                    System.out.println("Customer has finished eating and left table " + table.getTableNumber());
                }
                
                // Check if the table is now empty and reset its state if it is
                if (table.getCustomers().isEmpty()) {
                    table.resetTableState();
                    System.out.println("All customers have left table " + table.getTableNumber() + ", table state reset");
                }
            }
        }
    }
    
    @Override
    public void readState() {
        // In readState, we don't modify any state, just observe
    }
    
    @Override
    public void writeState() {
        // Spawn new customers each tick
        spawnCustomers();
        
        // Update customer browsing status
        updateCustomerBrowsingStatus();
        
        // Process tables with ready customers
        processReadyTables();
        
        // Collect completed orders from the CollectionPoint
        collectCompletedOrders();
        
        // Deliver meals to tables
        deliverMealsToTables();
        
        // Prompt customers to eat their meals and process departures
        promptCustomersToEat();
        
        // Process any tables marked for processing but not handled in previous ticks
        if (!tablesToProcess.isEmpty()) {
            List<Table> processed = new ArrayList<>();
            for (Table table : tablesToProcess) {
                Waiter waiter = getAvailableWaiter();
                if (waiter != null) {
                    if (!waiter.getAssignedTables().contains(table)) {
                        waiter.assignTable(table);
                    }
                    
                    try {
                        String orderId = waiter.takeTableOrderAndReturnId(table);
                        if (orderId != null) {
                            orderToTableMap.put(orderId, table);
                        }
                        System.out.println("Waiter took order from previously queued table " + table.getTableNumber());
                        processed.add(table);
                    } catch (IllegalStateException e) {
                        // If not ready to order anymore, remove from processing queue
                        if (!table.isEveryoneReadyToOrder()) {
                            processed.add(table);
                        }
                    }
                }
            }
            tablesToProcess.removeAll(processed);
        }
    }
    
    // Getters and setters
    
    public SeatingPlan getSeatingPlan() {
        return seatingPlan;
    }
    
    public List<Waiter> getWaiters() {
        return new ArrayList<>(waiters);
    }
    
    public void addWaiter(Waiter waiter) {
        if (!waiters.contains(waiter)) {
            waiters.add(waiter);
        }
    }
    
    public void removeWaiter(Waiter waiter) {
        waiters.remove(waiter);
    }
} 