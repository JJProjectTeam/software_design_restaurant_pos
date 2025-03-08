package com.softwaredesign.project.controller;

import java.util.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.menu.BurgerRecipe;
import com.softwaredesign.project.menu.KebabRecipe;
import com.softwaredesign.project.view.*;
import com.softwaredesign.project.kitchen.*;
import com.softwaredesign.project.order.*;
import com.softwaredesign.project.inventory.*;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.LongestQueueFirstStrategy;
import com.softwaredesign.project.staff.chefstrategies.OldestOrderFirstStrategy;
import com.softwaredesign.project.staff.chefstrategies.SimpleChefStrategy;

public class ConfigurationController extends BaseController {
    private Kitchen kitchen;
    private OrderManager orderManager;
    private InventoryService inventory;
    private CollectionPoint collectionPoint;
    private StationManager stationManager;
    private List<Waiter> waiters;
    private List<Table> tables;
    private Menu menu;
    private RestaurantViewMediator mediator;
    private boolean configurationComplete = false;

    public ConfigurationController() {
        super("Configuration");
        this.mediator = RestaurantViewMediator.getInstance();
        mediator.registerController("Configuration", this);
        this.waiters = new ArrayList<>();
        this.tables = new ArrayList<>();
        setupBaseComponents();
    }

    private void setupBaseComponents() {
        // Create basic components needed for the restaurant
        this.inventory = new Inventory();
        this.collectionPoint = new CollectionPoint();
        this.stationManager = new StationManager(collectionPoint);
        this.orderManager = new OrderManager(collectionPoint, stationManager);
        this.kitchen = new Kitchen(orderManager, collectionPoint);
        this.menu = new Menu(inventory);
    }

    // Methods to read from views and create restaurant entities
    public void createRestaurantComponents() {
        ChefConfigurationView chefView = (ChefConfigurationView) mediator.getView("ChefConfiguration");
        DiningConfigurationView diningView = (DiningConfigurationView) mediator.getView("DiningConfiguration");
        MenuConfigurationView menuView = (MenuConfigurationView) mediator.getView("MenuConfiguration");

        createChefs(chefView.getChefs());
        createWaitersAndTables(diningView.getWaiters(), diningView.getNumberOfTables());
        createMenuItems(menuView.getSelectedRecipes());
    }

    private void createChefs(Map<String, ChefConfigurationView.ChefData> chefData) {
        for (var entry : chefData.entrySet()) {
            var data = entry.getValue();
            
            // Create chef strategy based on selection
            ChefStrategy strategy = createChefStrategy(data.getStrategy());
            
            // Create chef stations
            List<Station> stations = new ArrayList<>();
            for (String stationType : data.getStations()) {
                Station station = new Station(StationType.valueOf(stationType.toUpperCase()), collectionPoint);
                stations.add(station);
                stationManager.addStation(station);
            }
            
            Chef chef = new Chef(data.getCostPerHour(), data.getSpeed(), strategy, stationManager);
            for (Station station : stations) {
                chef.assignToStation(station.getType());
            }
        }
    }
    //TODO: add actual strategies
    private ChefStrategy createChefStrategy(String strategyName) {
        return switch (strategyName.toUpperCase()) {
            case "FIFO", "OLDEST" -> new OldestOrderFirstStrategy();
            case "LIFO", "NEWEST" -> new LongestQueueFirstStrategy(); 
            default -> new SimpleChefStrategy(); 
        };
    }

    private void createWaitersAndTables(Map<String, DiningConfigurationView.WaiterData> waiterData, int tableCount) {
        // Create tables
        tables.clear();
        //TODO allow user to set max table size
        SeatingPlan seatingPlan = new SeatingPlan(4, tableCount, menu);
        // Create waiters
        waiters.clear();

        // Get all tables from seating plan
        List<Table> allTables = seatingPlan.getAllTables();
        int tablesPerWaiter = allTables.size() / waiterData.size();
        int extraTables = allTables.size() % waiterData.size();
        int tableIndex = 0; // Track which tables have been assigned

        for (var entry : waiterData.entrySet()) {
            var data = entry.getValue();
            Waiter waiter = new Waiter(data.getCostPerHour(), data.getSpeed(), orderManager, menu);
            
            // Calculate how many tables this waiter should get
            int tablesToAssign = tablesPerWaiter + (waiters.size() < extraTables ? 1 : 0);
            
            // Assign tables to this waiter
            for (int i = 0; i < tablesToAssign && tableIndex < allTables.size(); i++) {
                waiter.assignTable(allTables.get(tableIndex));
                tableIndex++;
            }
            
            waiters.add(waiter);
        }
    }

    private void createMenuItems(Set<String> selectedRecipes) {
        // The Menu class doesn't have a method to clear or add items directly
        // Instead, we'll create a new Menu with the selected recipes
        
        // First, create a new Menu with the inventory service
        this.menu = new Menu(inventory);
        
        // For each selected recipe, create the appropriate Recipe object and add it to the menu
        for (String recipeName : selectedRecipes) {
            try {
                // Create the appropriate recipe based on the name
                Recipe recipe = null;
                
                // This is a simplified approach - in a real implementation, you'd have a more
                // flexible way to create recipes based on names
                if (recipeName.equalsIgnoreCase("Burger")) {
                    recipe = new BurgerRecipe(inventory);
                } else if (recipeName.equalsIgnoreCase("Kebab")) {
                    recipe = new KebabRecipe(inventory);
                } else {
                    // For unknown recipes, create a generic one (if possible)
                    System.err.println("Unknown recipe: " + recipeName + " - skipping");
                    continue;
                }
                
                // The Menu class doesn't have an explicit method to add items
                // We'll need to modify the Menu class to support this, or use reflection
                // For now, we'll just log that we would add the recipe
                System.out.println("Would add recipe to menu: " + recipeName);
                
                // In a real implementation, you'd do something like:
                // menu.addRecipe(recipe);
            } catch (Exception e) {
                System.err.println("Error creating menu item: " + recipeName + " - " + e.getMessage());
            }
        }
    }

    // Getters for restaurant components
    public Kitchen getKitchen() { return kitchen; }
    public OrderManager getOrderManager() { return orderManager; }
    public InventoryService getInventory() { return inventory; }
    public List<Waiter> getWaiters() { return Collections.unmodifiableList(waiters); }
    public List<Table> getTables() { return Collections.unmodifiableList(tables); }
    public Menu getMenu() { return menu; }

    public void submitConfiguration() {
        createRestaurantComponents();
        configurationComplete = true;
        mediator.notifyConfigurationComplete();
    }

    public boolean isConfigurationComplete() {
        return configurationComplete;
    }

    public void updateView(){
        
    }
    
}
