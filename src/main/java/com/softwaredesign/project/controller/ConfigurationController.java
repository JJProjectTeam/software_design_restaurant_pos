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
        System.out.println("[ConfigurationController] Initializing controller");
        this.inventory = new Inventory(); // Initialize with concrete Inventory class
        this.mediator = RestaurantViewMediator.getInstance();
        this.possibleRecipes = new ArrayList<>(); // Initialize the list
        
        // Order matters here
        setupBaseComponents();
        mediator.registerController("Configuration", this);
        
        // Only initialize menu after everything else is set up
        System.out.println("[ConfigurationController] Delayed menu initialization");
        initializeMenuConfiguration();
        
        System.out.println("[ConfigurationController] Controller initialized");
    }

    //this should set up the components
    private void setupBaseComponents() {        
        System.out.println("[ConfigurationController] Setting up base components");
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
            
            System.out.println("[ConfigurationController] Base ingredients added to inventory");
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
        
        // Notify mediator that configuration is complete
        // mediator.notifyConfigurationComplete();
    }
    
    private void createDefaultConfiguration() {
        System.out.println("[ConfigurationController] Creating default configuration");
        
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
        SeatingPlan seatingPlan = new SeatingPlan(4, 40, 5, menu);
        
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
        for (var entry : chefData.entrySet()) {
            var data = entry.getValue();
            
            // Create chef strategy based on selection
            ChefStrategy strategy = createChefStrategy(data.getStrategy());
            
            // Create chef stations
            for (String stationType : data.getStations()) {
                Station station = new Station(StationType.valueOf(stationType.toUpperCase()), collectionPoint);
                stationManager.addStation(station);
                stations.add(station);
            }
            
            Chef chef = new Chef(data.getCostPerHour(), data.getSpeed(), strategy, stationManager);
            for (Station station : stations) {
                chef.assignToStation(station.getType());
            }
            chefs.add(chef);
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

    private void createWaitersAndTables(Map<String, DiningConfigurationView.WaiterData> waiterData, int tableCount, int tableCapacity) {
        // Create tables
        SeatingPlan seatingPlan = new SeatingPlan(tableCount, 40, tableCapacity, menu); //TODO read from a config file for magic number for number of seats
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

    /**
     * Create stations based on the configuration
     */
    private void createStations(int grillCount, int prepCount, int plateCount) {
        System.out.println("[ConfigurationController] Creating stations - Grill: " + grillCount + 
                          ", Prep: " + prepCount + 
                          ", Plate: " + plateCount);
        
        // Clear existing stations if any
        stationManager = new StationManager(collectionPoint);
        
        // Create Grill stations
        for (int i = 0; i < grillCount; i++) {
            Station grillStation = new Station(StationType.GRILL, collectionPoint);
            stationManager.addStation(grillStation);
            System.out.println("[ConfigurationController] Created Grill station " + (i + 1));
        }
        
        // Create Prep stations
        for (int i = 0; i < prepCount; i++) {
            Station prepStation = new Station(StationType.PREP, collectionPoint);
            stationManager.addStation(prepStation);
            System.out.println("[ConfigurationController] Created Prep station " + (i + 1));
        }
        
        // Create Plate stations
        for (int i = 0; i < plateCount; i++) {
            Station plateStation = new Station(StationType.PLATE, collectionPoint);
            stationManager.addStation(plateStation);
            System.out.println("[ConfigurationController] Created Plate station " + (i + 1));
        }
        
        // Create a new kitchen with the station manager
        kitchen = new Kitchen(orderManager, collectionPoint);
        
        // Set the kitchen reference in each station
        for (Station station : stationManager.getAllStations()) {
            station.setKitchen(kitchen);
        }
        
        System.out.println("[ConfigurationController] Created kitchen with " + stationManager.getAllStations().size() + " stations");
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
        System.out.println("[ConfigurationController] Updating views");
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
            System.out.println("[ConfigurationController] Getting menu configuration view");
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
            
            System.out.println("[ConfigurationController] Setting possible recipes: " + recipeData.keySet());
            menuView.setPossibleRecipes(recipeData);
            
        } catch (Exception e) {
            System.err.println("[ConfigurationController] Error initializing menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
