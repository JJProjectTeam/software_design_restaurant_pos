package com.softwaredesign.project;

import java.util.List;

import com.softwaredesign.project.controller.*;
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
    private InventoryService inventoryService;
    private SeatingPlan seatingPlan;
    
    
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
            
            initializeConfiguration();
            
            // Show the welcome view explicitly
            app.showView(ViewType.WELCOME);
            
            app.run();
            
            waitForConfiguration();

            
            createEntitiesFromConfiguration();
            app.showView(ViewType.DINING_ROOM);

            //TODO - this is a dummy tick placeholder
            while (true){
                passEntitiesToGamePlay();
                Thread.sleep(1000);
            }

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
        this.inventoryService = configController.getInventoryService();
        this.seatingPlan = configController.getSeatingPlan();
    }




    private void initializeOperation() {        
        // Create gameplay controllers with configured components
        diningRoomController = new DiningRoomController(
            menu,
            configController.getSeatingPlan()
        );

        kitchenController = new KitchenController(
            kitchen
        );

        //TODO populate inventory
        inventoryController = new InventoryController();

        // Register gameplay controllers with mediator
        mediator.registerController("DiningRoom", diningRoomController);
        mediator.registerController("Kitchen", kitchenController);
        mediator.registerController("Inventory", inventoryController);
        
        System.out.println("[RestaurantDriver] Restaurant initialized and ready for operation");
    }
    public void passEntitiesToGamePlay(){
        //TODO this will be called on each 'tick'
        diningRoomController.updateView();
        kitchenController.updateView();
        inventoryController.updateView();
    }

    public static void main(String[] args) {
        RestaurantDriver driver = new RestaurantDriver();
        driver.start();
    }
}
