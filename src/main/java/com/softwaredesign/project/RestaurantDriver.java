package com.softwaredesign.project;

import com.softwaredesign.project.controller.*;
import com.softwaredesign.project.view.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;

public class RestaurantDriver {
    private RestaurantApplication app;
    private RestaurantViewMediator mediator;
    private ConfigurationController configController;
    private DiningRoomController diningRoomController;
    private KitchenController kitchenController;
    private static final int TOTALSEATS = 40;
    
    public RestaurantDriver() {
        try{
            this.app = new RestaurantApplication();
            this.mediator = RestaurantViewMediator.getInstance();
        }
        catch (Exception e){
            System.err.println("[RestaurantDriver] Fatal error running application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void start() {
        try {
            System.out.println("[RestaurantDriver] Starting application...");
            
            // Initialize configuration phase
            initializeConfiguration();
            
            // Show the welcome view explicitly
            app.showView(ViewType.WELCOME);
            
            // Run the application - this starts the event loop
            System.out.println("[RestaurantDriver] Running application...");
            app.run();
            
            // The following code won't execute until the application is closed
            // because app.run() starts an event loop
            
            // Wait for configuration to complete
            waitForConfiguration();
            
            // Initialize restaurant operation phase
            initializeOperation();
            
        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Fatal error running application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeConfiguration() {
        // Create and register configuration controller
        configController = new ConfigurationController();
        
        // Let the UI initialize
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("[RestaurantDriver] UI initialization interrupted: " + e.getMessage());
        }
        
        // The welcome view is now shown in the start method
        // app.showView(ViewType.WELCOME);
        
        // Register for configuration completion notification
        mediator.registerConfigurationListener(this::onConfigurationComplete);
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

    private void onConfigurationComplete() {
        System.out.println("[RestaurantDriver] Configuration completed, initializing restaurant...");
        configController.createRestaurantComponents();
        initializeOperation();
    }

    private void initializeOperation() {
        // Initialize menu recipes for gameplay
        configController.getMenu().initializeRecipes();
        
        // Create operational controllers with configured components
        diningRoomController = new DiningRoomController(
            configController.getMenu(),
            configController.getTables().size(),
            TOTALSEATS

        );

        kitchenController = new KitchenController(
            configController.getKitchen()
        );

        // Register operational controllers with mediator
        mediator.registerController("DiningRoom", diningRoomController);
        mediator.registerController("Kitchen", kitchenController);

        // Switch to dining room view
        app.showView(ViewType.DINING_ROOM);
        
        System.out.println("[RestaurantDriver] Restaurant initialized and ready for operation");
    }

    public static void main(String[] args) {
        RestaurantDriver driver = new RestaurantDriver();
        driver.start();
    }
}
