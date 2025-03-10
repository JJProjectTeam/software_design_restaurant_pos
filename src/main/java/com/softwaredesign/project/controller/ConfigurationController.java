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
import java.util.stream.Collectors;

public class ConfigurationController extends BaseController {
    private Kitchen kitchen;
    private OrderManager orderManager;
    private InventoryService inventory;
    private CollectionPoint collectionPoint;
    private StationManager stationManager;
    private List<Waiter> waiters;
    private List<Chef> chefs;
    private List<Station> stations;
    private Menu menu;
    private RestaurantViewMediator mediator;
    private SeatingPlan seatingPlan;
    private boolean configurationComplete = false;

    //TODO this should probably be loaded from a config or something!!! Not hard coded here
    private List<Recipe> possibleRecipes = new ArrayList<>();

    //constructor registers with mediator
    public ConfigurationController() {
        super("Configuration");
        this.inventory = new Inventory(); // Initialize with concrete Inventory class
        this.mediator = RestaurantViewMediator.getInstance();
        this.possibleRecipes = new ArrayList<>(); // Initialize the list
        
        // Order matters here
        setupBaseComponents();
        mediator.registerController("Configuration", this);
        
        // Only initialize menu after everything else is set up
        initializeMenuConfiguration();
        
    }

    //this should set up the components
    private void setupBaseComponents() {        
        try {
            // Add base ingredients
            //TODO YO should ingredients really be being done by strings? idk
            inventory.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
            inventory.addIngredient("Bun", 10, 1.0, StationType.PREP);
            inventory.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
            inventory.addIngredient("Tomato", 10, 1.0, StationType.PREP);
            inventory.addIngredient("Cheese", 10, 1.0, StationType.PREP);
            inventory.addIngredient("Kebab Meat", 10, 1.0, StationType.GRILL);
            inventory.addIngredient("Lamb", 100, 3.00, StationType.GRILL);
            inventory.addIngredient("Pita Bread", 100, 0.60, StationType.PLATE);
            inventory.addIngredient("Onions", 100, 0.25, StationType.PREP);
            inventory.addIngredient("Tomatoes", 100, 0.80, StationType.PREP);
            
            // Condiments
            inventory.addIngredient("Garlic Sauce", 100, 0.20, StationType.PREP);
            inventory.addIngredient("Ketchup", 100, 0.20, StationType.PREP);
            inventory.addIngredient("Mayo", 100, 0.25, StationType.PREP);
            inventory.addIngredient("Pickle", 100, 0.30, StationType.PREP);
            
        } catch (Exception e) {
            System.err.println("[ConfigurationController] Error setting up base components: " + e.getMessage());
            throw new RuntimeException("Failed to setup base components", e);
        }
        
        // Kebab ingredients
        inventory.addIngredient("Lamb", 100, 3.00, StationType.GRILL);
        inventory.addIngredient("Pita Bread", 100, 0.60, StationType.PLATE);
        inventory.addIngredient("Onion", 100, 0.25, StationType.PREP);
        inventory.addIngredient("Tzatziki", 100, 0.80, StationType.PREP);
        
        // Condiments
        inventory.addIngredient("Mustard", 100, 0.20, StationType.PREP);
        inventory.addIngredient("Ketchup", 100, 0.20, StationType.PREP);
        inventory.addIngredient("Mayo", 100, 0.25, StationType.PREP);
        inventory.addIngredient("Pickle", 100, 0.30, StationType.PREP);
        
        this.collectionPoint = new CollectionPoint();
        this.stationManager = new StationManager(collectionPoint);
        this.orderManager = new OrderManager(collectionPoint, stationManager);
        this.kitchen = new Kitchen(orderManager, collectionPoint);
        this.menu = new Menu(inventory);

        possibleRecipes.add(new BurgerRecipe(inventory));
        possibleRecipes.add(new KebabRecipe(inventory));
    }

    // Methods to read from views and create restaurant entities
    public void createRestaurantComponents() {
        
        // Get configuration data from views
        ChefConfigurationView chefView = (ChefConfigurationView) mediator.getView(ViewType.CHEF_CONFIGURATION);
        DiningConfigurationView diningView = (DiningConfigurationView) mediator.getView(ViewType.DINING_CONFIGURATION);
        MenuConfigurationView menuView = (MenuConfigurationView) mediator.getView(ViewType.MENU_CONFIGURATION);
        
        // Get station counts
        int grillStationCount = chefView.getStationCounts().get("GRILL");
        int prepStationCount = chefView.getStationCounts().get("PREP");
        int plateStationCount = chefView.getStationCounts().get("PLATE");
        
        // Create chefs
        createChefs(chefView.getChefs());
        
        // Create waiters and tables
        createWaitersAndTables(diningView.getWaiters(), diningView.getNumberOfTables(), diningView.getTableCapacity());
        
        // Create menu items
        createMenuItems(menuView.getSelectedRecipes());
        
        // Create stations based on configuration
        createStations(grillStationCount, prepStationCount, plateStationCount);
        
        // Set configuration as complete
        configurationComplete = true;
        System.out.println("[ConfigurationController] Configuration complete");
        
        // Notify mediator that configuration is complete
        // mediator.notifyConfigurationComplete();
    }
    
    private void createDefaultConfiguration() {        
        // Create default chef
        List<String> defaultStations = Arrays.asList("Grill", "Prep", "Plate");
        ChefStrategy strategy = new SimpleChefStrategy();
        Chef chef = new Chef(200.0, 2, strategy, stationManager);
        
        // Create default stations
        for (String stationType : defaultStations) {
            Station station = new Station(StationType.valueOf(stationType.toUpperCase()), collectionPoint);
            stationManager.addStation(station);
            chef.assignToStation(StationType.valueOf(stationType.toUpperCase()));
        }
        
        // Create default tables
        seatingPlan = new SeatingPlan(4, 40, 5, menu);
        
        // Create default waiter
        Waiter waiter = new Waiter(20.0, 2, orderManager, menu);
        
        // Assign tables to waiter
        for (Table table : seatingPlan.getAllTables()) {
            waiter.assignTable(table);
        }
        
        waiters.add(waiter);
        
        // Create default menu items
        Set<String> defaultRecipes = new HashSet<>(Arrays.asList("Burger", "Kebab"));
        createMenuItems(defaultRecipes);
    }

    private void createChefs(Map<String, ChefConfigurationView.ChefData> chefData) {
        System.out.println("[ConfigurationController] Creating chefs from configuration");
        chefs = new ArrayList<>();
        stations = new ArrayList<>();  // Initialize the stations list
        
        for (ChefConfigurationView.ChefData data : chefData.values()) {
            try {
                // Create chef strategy
                ChefStrategy strategy = createChefStrategy(data.getStrategy());
                
                // Create chef with data
                Chef chef = new Chef(
                    data.getCostPerHour(),
                    data.getSpeed(),
                    strategy, 
                    stationManager
                );
                
                // Add stations to chef's qualifications
                for (String stationType : data.getStations()) {
                    chef.assignToStation(StationType.valueOf(stationType.toUpperCase()));
                }
                
                chefs.add(chef);
                System.out.println("[ConfigurationController] Created chef: " + chef.getName() 
                    + " with " + data.getStations().size() + " qualifications");
                
            } catch (Exception e) {
                System.err.println("[ConfigurationController] Error creating chef: " + e.getMessage());
                throw new RuntimeException("Failed to create chef: " + data.getName(), e);
            }
        }
        System.out.println("[ConfigurationController] Created " + chefs.size() + " chefs");
    }

    //TODO: add actual strategies
    private ChefStrategy createChefStrategy(String strategyName) {
        return switch (strategyName.toUpperCase()) {
            case "FIFO", "OLDEST" -> new OldestOrderFirstStrategy();
            case "LIFO", "NEWEST" -> new LongestQueueFirstStrategy(); 
            default -> new SimpleChefStrategy(); 
        };
    }

    private void createWaitersAndTables(Map<String, DiningConfigurationView.WaiterData> waiterData, int tableCount, int tableCapacity) {
        // Initialize waiters list first
        this.waiters = new ArrayList<>();

        // Create tables
        System.out.println("[ConfigurationController] Creating tables - Count: " + tableCount + ", Capacity: " + tableCapacity);
        seatingPlan = new SeatingPlan(tableCount, 40, tableCapacity, menu);
        System.out.println("[ConfigurationController] Created seating plan with " + seatingPlan.getAllTables().size() + " tables");

        // Get all tables from seating plan
        List<Table> allTables = seatingPlan.getAllTables();
        
        // Calculate table distribution
        int tablesPerWaiter = allTables.size() / waiterData.size();
        int extraTables = allTables.size() % waiterData.size();
        int tableIndex = 0;

        System.out.println("[ConfigurationController] Distributing " + allTables.size() + " tables among " + waiterData.size() + " waiters");
        
        for (var entry : waiterData.entrySet()) {
            var data = entry.getValue();
            Waiter waiter = new Waiter(data.getCostPerHour(), data.getSpeed(), orderManager, menu);
            
            // Calculate tables for this waiter
            int tablesToAssign = tablesPerWaiter + (waiters.size() < extraTables ? 1 : 0);
            System.out.println("[ConfigurationController] Assigning " + tablesToAssign + " tables to waiter");
            
            // Assign tables
            for (int i = 0; i < tablesToAssign && tableIndex < allTables.size(); i++) {
                waiter.assignTable(allTables.get(tableIndex));
                tableIndex++;
            }
            
            waiters.add(waiter);
            System.out.println("[ConfigurationController] Added waiter with " + tablesToAssign + " tables");
        }
        
        System.out.println("[ConfigurationController] Created " + waiters.size() + " waiters");
    }

    private void createMenuItems(Set<String> selectedRecipes) {

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
                
                //TODO i dont think we mande a menuuuu
                // menu.addRecipe(recipe);
            } catch (Exception e) {
                System.err.println("Error creating menu item: " + recipeName + " - " + e.getMessage());
            }
        }
    }

    /**
     * Create stations based on the configuration
     */
    private void createStations(int grillCount, int prepCount, int plateCount) {

        // Clear existing stations if any
        stationManager = new StationManager(collectionPoint);
        
        // Create Grill stations
        for (int i = 0; i < grillCount; i++) {
            Station grillStation = new Station(StationType.GRILL, collectionPoint);
            stationManager.addStation(grillStation);
        }
        
        // Create Prep stations
        for (int i = 0; i < prepCount; i++) {
            Station prepStation = new Station(StationType.PREP, collectionPoint);
            stationManager.addStation(prepStation);
        }
        
        // Create Plate stations
        for (int i = 0; i < plateCount; i++) {
            Station plateStation = new Station(StationType.PLATE, collectionPoint);
            stationManager.addStation(plateStation);
        }
        
        // Create a new kitchen with the station manager
        kitchen = new Kitchen(orderManager, collectionPoint);
        
        // Set the kitchen reference in each station
        for (Station station : stationManager.getAllStations()) {
            station.setKitchen(kitchen);
        }
        
    }

    // Getters for restaurant components
    public Kitchen getKitchen() { return kitchen; }
    public OrderManager getOrderManager() { return orderManager; }
    public InventoryService getInventoryService() { return inventory; }
    public List<Waiter> getWaiters() { return Collections.unmodifiableList(waiters); }
    public Menu getMenu() { return menu; }

    public boolean isConfigurationComplete() {
        return configurationComplete;
    }

    public void updateView(){
        initializeMenuConfiguration();
    }
    @Override
    public void onUserInput(){
        createRestaurantComponents();
    }

    public List<Chef> getChefs() {
        return chefs;
    }
    public List<Station> getStations() {
        return stations;
    }
    public CollectionPoint getCollectionPoint() {
        return collectionPoint;
    }
    public StationManager getStationManager() {
        return stationManager;
    }
    public SeatingPlan getSeatingPlan() {
        return seatingPlan;
    }

    private void initializeMenuConfiguration() {
        try {
            MenuConfigurationView menuView = (MenuConfigurationView) mediator.getView(ViewType.MENU_CONFIGURATION);
            if (menuView == null) {
                throw new RuntimeException("Menu configuration view not found");
            }
            
            // Convert recipes to view format
            Map<String, List<String>> recipeData = new HashMap<>();
            for (Recipe recipe : possibleRecipes) {
                List<String> ingredients = recipe.getIngredients().stream()
                    .map(Ingredient::getName)
                    .collect(Collectors.toList());
                recipeData.put(recipe.getName(), ingredients);
            }
            
            menuView.setPossibleRecipes(recipeData);
            
        } catch (Exception e) {
            System.err.println("[ConfigurationController] Error initializing menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
