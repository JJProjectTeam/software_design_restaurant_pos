package com.softwaredesign.project.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.menu.BurgerRecipe;
import com.softwaredesign.project.menu.KebabRecipe;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.orderfulfillment.FloorManager;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.ChefManager;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.DynamicChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.SimpleChefStrategy;

/**
 * Utility class to help set up and progress through demo scenarios.
 * This is not an Entity, just a helper for demonstration purposes.
 */
public class DemoHelper {
    private FloorManager floorManager;
    private Kitchen kitchen;
    private OrderManager orderManager;
    private SeatingPlan seatingPlan;
    private ChefManager chefManager;
    private Inventory inventory;
    
    private int demoStep = 0;
    private int tickCount = 0;
    private int gameLengthTicks;
    private boolean hasStartedDemo = false;
    private final int DEMO_DELAY = 5;
    private JsonNode config;

    
    public DemoHelper(FloorManager floorManager, Kitchen kitchen, OrderManager orderManager, 
                      SeatingPlan seatingPlan, ChefManager chefManager, Inventory inventory) {
        this.floorManager = floorManager;
        this.kitchen = kitchen;
        this.orderManager = orderManager;
        this.seatingPlan = seatingPlan;
        this.chefManager = chefManager;
        this.inventory = inventory;

        try {
            String configPath = "src/main/config.json";
            String jsonContent = Files.readString(Paths.get(configPath));
            ObjectMapper mapper = new ObjectMapper();
            this.config = mapper.readTree(jsonContent);
        } catch (IOException e) {
            System.out.println("[DemoHelper] Failed to load config: " + e.getMessage());
            this.config = null;
        }

        this.gameLengthTicks = config.path("gameLengthTicks").asInt(0);
    }
    
    /**
     * Called on each tick to update the demo sequence
     * @return True if the demo performed any actions this tick
     */
    public boolean update(int tickCount) {
        this.tickCount = tickCount;
        
        // Check if it's time to start the demo
        if (!hasStartedDemo) {
            if (tickCount >= DEMO_DELAY) {
                hasStartedDemo = true;
                System.out.println("\n=== STARTING DEMO SEQUENCE ===\n");
                return true;
            }
            return false;
        }
        
        // Process the appropriate demo step
        return processCurrentDemoStep();
    }
    

    
    /**
     * Process the current step in the demo sequence
     * @return True if any action was taken
     */
    private boolean processCurrentDemoStep() {
        boolean actionTaken = false;
        
        switch (demoStep) {
            case 0:
                // Demo Step 1: Seat customers at tables
                actionTaken = seatCustomers();
                break;
                
            case 1:
                // Demo Step 2: Create orders
                actionTaken = createOrders();
                break;
                
            case 2:
                // Demo Step 3: Monitor kitchen processing
                actionTaken = monitorKitchenProcessing();
                break;
                
            case 3:
                // Demo Step 4: Check station status
                actionTaken = checkStationStatus();
                break;
                
            default:
                // Ongoing monitoring
                actionTaken = ongoingMonitoring();
                break;
        }
        
        return actionTaken;
    }
    
    /**
     * Demo Step 1: Seat customers at tables
     */
    public boolean seatCustomers() {
        System.out.println("\n=== SEATING CUSTOMERS ===");
        boolean customersSeated = false;
        
        // Generate a random group size between 1 and maxGroupSize (from config)
        int maxGroupSize = getMaxGroupSizeFromConfig();
        int groupSize = new Random().nextInt(maxGroupSize) + 1; // +1 to ensure at least 1 customer
        
        System.out.println("Attempting to seat a group of " + groupSize + " customers");
        
        // Create the customer group
        List<DineInCustomer> customerGroup = new ArrayList<>();
        for (int i = 0; i < groupSize; i++) {
            DineInCustomer customer = new DineInCustomer();
            customer.finishBrowsing(); // Ready to order
            customerGroup.add(customer);
        }
        
        // Try to seat the group
        Table table = floorManager.seatCustomer(customerGroup.get(0));
        
        if (table != null) {
            System.out.println("Seated a group of " + groupSize + " customers at table " + table.getTableNumber());
            customersSeated = true;
        } else {
            System.out.println("Could not find a table for a group of " + groupSize + " customers");
        }
        
        if (customersSeated) {
            demoStep++;
        }
        return customersSeated;
    }
    
    /**
     * Gets the maximum group size from the configuration file
     * @return The maximum group size, or 10 as default if not found
     */
    private int getMaxGroupSizeFromConfig() {
        try {
            // Get maxGroupSize from config
            int maxGroupSize = config.path("diningRoomRules").path("maxGroupSize").asInt(10);
            return maxGroupSize;
        } catch (Exception e) {
            System.out.println("[DemoHelper] Error loading config values: " + e.getMessage());
            // Return default value if config loading fails
            return 10;
        }
    }
    
    /**
     * Demo Step 2: Create orders for tables
     */
    private boolean createOrders() {
        System.out.println("\n=== CREATING ORDERS ===");
        boolean ordersCreated = false;
        
        // Create orders for seated customers
        for (Table table : getOccupiedTables()) {
            if (!table.isOrderPlaced()) {
                Order order = table.getTableNumber() % 2 == 0 ? 
                    createBurgerOrder() : createKebabOrder();
                // Add order to the OrderManager
                orderManager.addOrder(order);
                System.out.println("Created " + 
                    (table.getTableNumber() % 2 == 0 ? "burger" : "kebab") + 
                    " order for table " + table.getTableNumber());
                ordersCreated = true;
            }
        }
        
        if (ordersCreated) {
            demoStep++;
        }
        return ordersCreated;
    }
    
    /**
     * Demo Step 3: Monitor kitchen processing
     */
    private boolean monitorKitchenProcessing() {
        System.out.println("\n=== KITCHEN PROCESSING ===");
        
        // Simply log that kitchen is processing orders
        System.out.println("Kitchen is processing orders via the tick system...");
        
        // Have chefs check for work
        for (Chef chef : chefManager.getAllChefs()) {
            if (!chef.isWorking()) {
                System.out.println("Chef " + chef.getName() + " checking for work...");
                // Unregister from current station if not working
                if (chef.getCurrentStation() != null) {
                    chef.getCurrentStation().unregisterChef();
                }
                chef.checkForWork();
            }
        }
        
        // Move to next step after monitoring
        demoStep++;
        return true;
    }
    
    /**
     * Demo Step 4: Check station status
     */
    private boolean checkStationStatus() {
        System.out.println("\n=== CHECKING STATION STATUS ===");
        
        // Log the status of all stations
        StationManager stationManager = kitchen.getStationManager();
        for (Station station : stationManager.getAllStations()) {
            System.out.println("Station " + station.getType() + " status:");
            System.out.println("  - Has chef: " + (station.getAssignedChef() != null));
            if (station.getAssignedChef() != null) {
                System.out.println("  - Chef: " + station.getAssignedChef().getName());
            }
            System.out.println("  - Is busy: " + station.isBusy());
            System.out.println("  - Current recipe: " + 
                (station.getCurrentRecipe() != null ? station.getCurrentRecipe().getName() : "None"));
            System.out.println("  - Current task: " + 
                (station.getCurrentTask() != null ? station.getCurrentTask().getName() : "None"));
            System.out.println("  - Backlog size: " + station.getBacklog().size());
        }
        
        // Move to ongoing monitoring
        demoStep++;
        return true;
    }
    
    /**
     * Ongoing monitoring of restaurant operations
     */
    private boolean ongoingMonitoring() {
        boolean actionTaken = false;
        
        // Periodically check for completed orders
        if (tickCount % 5 == 0) {
            System.out.println("\n=== STATION STATUS UPDATE ===");
            StationManager sm = kitchen.getStationManager();
            for (Station station : sm.getAllStations()) {
                if (station.getCurrentTask() != null || station.getAssignedChef() != null) {
                    System.out.println("Station " + station.getType() + ":");
                    System.out.println("  - Chef: " + 
                        (station.getAssignedChef() != null ? station.getAssignedChef().getName() : "None"));
                    System.out.println("  - Task: " + 
                        (station.getCurrentTask() != null ? station.getCurrentTask().getName() : "None"));
                    System.out.println("  - Progress: " + station.getCookingProgress() + 
                        (station.getCurrentTask() != null ? "/" + station.getCurrentTask().getCookingWorkRequired() : ""));
                    actionTaken = true;
                }
            }
        }
        
        // Every 3 ticks, check for tables with customers ready to order
        if (tickCount % 3 == 0) {
            // Check for customers ready to order and create orders
            boolean ordersCreated = createOrdersForReadyTables();
            if (ordersCreated) {
                actionTaken = true;
            }
        }
        
        return actionTaken;
    }
    
    /**
     * Create orders for tables with customers ready to order
     * @return true if any orders were created
     */
    private boolean createOrdersForReadyTables() {
        boolean ordersCreated = false;
        
        // Create orders for seated customers who are ready to order
        for (Table table : getOccupiedTables()) {
            if (!table.isOrderPlaced() && table.isEveryoneReadyToOrder()) {
                Order order = table.getTableNumber() % 2 == 0 ? 
                    createBurgerOrder() : createKebabOrder();
                // Add order to the OrderManager
                orderManager.addOrder(order);
                // Mark the table as having an order placed
                table.markOrderPlaced();
                System.out.println("Created " + 
                    (table.getTableNumber() % 2 == 0 ? "burger" : "kebab") + 
                    " order for table " + table.getTableNumber());
                ordersCreated = true;
            }
        }
        
        return ordersCreated;
    }
    
    /**
     * Create a burger order
     */
    private Order createBurgerOrder() {
        Order order = new Order(orderManager.generateOrderId());
        order.addRecipes(new BurgerRecipe(inventory));
        return order;
    }
    
    /**
     * Create a kebab order
     */
    private Order createKebabOrder() {
        Order order = new Order(orderManager.generateOrderId());
        order.addRecipes(new KebabRecipe(inventory));
        return order;
    }
    
    /**
     * Get a list of tables that have customers
     */
    private List<Table> getOccupiedTables() {
        List<Table> occupiedTables = new ArrayList<>();
        for (Table table : seatingPlan.getAllTables()) {
            if (!table.getCustomers().isEmpty()) {
                occupiedTables.add(table);
            }
        }
        return occupiedTables;
    }
    
    /**
     * Assign chef strategies for the demo
     */
    public void setupChefStrategies() {
        List<Chef> chefs = chefManager.getAllChefs();
        
        // Create different chef strategies
        ChefStrategy dynamicStrategy = new DynamicChefStrategy(kitchen.getStationManager());
        ChefStrategy simpleStrategy = new SimpleChefStrategy();
        
        // Assign strategies to existing chefs
        for (int i = 0; i < chefs.size(); i++) {
            Chef chef = chefs.get(i);
            chef.setWorkStrategy(i % 2 == 0 ? dynamicStrategy : simpleStrategy);
            System.out.println("Assigned " + (i % 2 == 0 ? "dynamic" : "simple") + 
                " strategy to chef " + chef.getName());
        }
    }
    
    public boolean isGameOver(){
        return tickCount >= gameLengthTicks;
    }
} 