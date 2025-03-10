package com.softwaredesign.project;

import java.util.HashMap;
import java.util.List;

import com.softwaredesign.project.controller.*;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.view.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.Waiter;

public class RestaurantDriver {

    //CONSTANTS = move to another file
    private static final int NUMBER_OF_SEATS = 40;

    private RestaurantApplication app;
    private RestaurantViewMediator mediator;
    private ConfigurationController configController;
    private DiningRoomController diningRoomController;
    private KitchenController kitchenController;
    private InventoryController inventoryController;
    private static final int TOTALSEATS = 40;

    private List<Waiter> waiters;
    private List<Chef> chefs;
    private Kitchen kitchen;
    private Menu menu;
    private OrderManager orderManager;
    private List<Table> tables;
    private Inventory inventory;
    private SeatingPlan seatingPlan;
    
    
    public RestaurantDriver() {
        try{
            this.app = new RestaurantApplication();
            this.mediator = RestaurantViewMediator.getInstance();
            
            // Set this driver instance in the application for restart functionality
            this.app.setDriver(this);
        }
        catch (Exception e){
            System.err.println("[RestaurantDriver] Fatal error running application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void start() {
        try {
            System.out.println("[RestaurantDriver] Starting application...");
            
            initializeConfiguration();
            app.showView(ViewType.WELCOME);

            // Create a timer for the game loop
            java.util.Timer gameTimer = new java.util.Timer();
            gameTimer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        if (configController.isConfigurationComplete()) {
                            // Only run once when configuration is complete
                            if (kitchen == null) {
                                System.out.println("[RestaurantDriver] Configuration complete, initializing game");
                                createEntitiesFromConfiguration();
                                initializeOperation();
                                
                                // Show dining room view and do initial update
                                app.showView(ViewType.DINING_ROOM);
                                Thread.sleep(100); // Small delay for view initialization
                            }

                            // Update views one at a time to avoid concurrent modification
                            synchronized(mediator) {
                                diningRoomController.updateView();
                                Thread.sleep(50);
                                kitchenController.updateView();
                                Thread.sleep(50);
                                inventoryController.updateView();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[RestaurantDriver] Error in game loop: " + e.getMessage());
                        e.printStackTrace();
                        gameTimer.cancel();
                    }
                }
            }, 0, 1000); // Check every second

            // This will block until the window is closed
            app.run();
            
            // Cleanup when window closes
            gameTimer.cancel();
            System.out.println("[RestaurantDriver] Application terminated");

        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Fatal error running application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeConfiguration() {
        configController = new ConfigurationController();
        mediator.registerController("Configuration", configController);
    }

    private void waitForConfiguration() {
        // This could be improved with proper synchronization
        while (!configController.isConfigurationComplete()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void createEntitiesFromConfiguration(){
        this.waiters = configController.getWaiters();
        this.chefs = configController.getChefs();
        this.kitchen = configController.getKitchen();
        this.menu = configController.getMenu();
        this.orderManager = configController.getOrderManager();
        this.inventory = configController.getInventory();
        this.seatingPlan = configController.getSeatingPlan();
        System.out.println("SEATINGPLAN: "+ seatingPlan.getAllTables().size());
    }

    private void initializeOperation() {        
        // Create gameplay controllers with configured components
        diningRoomController = new DiningRoomController(
            configController.getSeatingPlan()
        );

        kitchenController = new KitchenController(
            kitchen
        );

        //TODO populate inventory
        inventoryController = new InventoryController(inventory);

        // Register gameplay controllers with mediator
        mediator.registerController("DiningRoom", diningRoomController);
        mediator.registerController("Kitchen", kitchenController);
        mediator.registerController("Inventory", inventoryController);
        
        System.out.println("[RestaurantDriver] Restaurant initialized and ready for operation");
    }
    public synchronized void passEntitiesToGamePlay(){
        //TODO this will be called on each 'tick'
        try {
            if (diningRoomController != null) {
                diningRoomController.updateView();
            }
            
            if (kitchenController != null) {
                kitchenController.updateView();
            }
            
            if (inventoryController != null) {
                inventoryController.updateView();
            }
        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Error updating views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Completely restarts the application by resetting all components and reinitializing
     */
    public synchronized void restart() {
        System.out.println("[RestaurantDriver] Performing full application restart");
        
        // Stop any ongoing operations
        try {
            // Reset all entity references
            this.waiters = null;
            this.chefs = null;
            this.kitchen = null;
            this.menu = null;
            this.orderManager = null;
            this.tables = null;
            this.inventory = null;
            this.seatingPlan = null;
            
            // Reset controllers
            this.diningRoomController = null;
            this.kitchenController = null;
            this.inventoryController = null;
            
            // Reset configuration controller but keep the mediator
            this.configController = null;
            
            // Reinitialize configuration
            initializeConfiguration();
            
            // Tell the application to show the welcome view
            // Use SwingUtilities.invokeLater to ensure UI updates happen on the EDT
            javax.swing.SwingUtilities.invokeLater(() -> {
                app.showView(ViewType.WELCOME);
            });
            
            System.out.println("[RestaurantDriver] Application restart complete");
        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Error during restart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RestaurantDriver driver = new RestaurantDriver();
        driver.start();
    }
}
//TODOS:
/*
 * make inventory + pass to controller and view
 * When im not sleepy - are we just updating views with what has changed, or repainting each time? 
 * Test updating funcionality in driver
 * 
 * Money system, both during game and at configuraiton
 * 
 * Some kind of recipeTypes, and an inventory config file that can be set up
 * 
 * In config file - set things like restuarant seats, initialbudget, chef/waiter charge strategy etc. 
 * 
 * Help menu
 *
 */