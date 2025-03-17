package com.softwaredesign.project.demo;

import java.util.ArrayList;
import java.util.List;

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
    private boolean hasStartedDemo = false;
    private final int DEMO_DELAY = 5;
    
    public DemoHelper(FloorManager floorManager, Kitchen kitchen, OrderManager orderManager, 
                      SeatingPlan seatingPlan, ChefManager chefManager, Inventory inventory) {
        this.floorManager = floorManager;
        this.kitchen = kitchen;
        this.orderManager = orderManager;
        this.seatingPlan = seatingPlan;
        this.chefManager = chefManager;
        this.inventory = inventory;
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
    private boolean seatCustomers() {
        System.out.println("\n=== SEATING CUSTOMERS ===");
        boolean customersSeated = false;
        
        // Add customers to tables
        for (Table table : seatingPlan.getAllTables()) {
            if (table.getCustomers().isEmpty()) {
                DineInCustomer customer = new DineInCustomer();
                table.addCustomer(customer);
                customer.finishBrowsing(); // Ready to order
                System.out.println("Seated customer at table " + table.getTableNumber());
                customersSeated = true;
                if (table.getTableNumber() >= 3) break; // Limit to 3 tables for demo
            }
        }
        
        if (customersSeated) {
            demoStep++;
        }
        return customersSeated;
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
        
        return actionTaken;
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
} 