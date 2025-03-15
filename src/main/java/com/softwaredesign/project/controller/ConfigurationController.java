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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Paths;
import java.nio.file.Files;

public class ConfigurationController extends BaseController {
    private Kitchen kitchen;
    private OrderManager orderManager;
    private Inventory inventory;
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
            // Read and parse config file
            String configPath = "src/main/config.json";
            String jsonContent = Files.readString(Paths.get(configPath));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode config = mapper.readTree(jsonContent);
            JsonNode stations = config.path("inventory").path("stations");

            System.out.println("[ConfigurationController] Loading ingredients from config file...");

            // Iterate through each station type
            for (StationType stationType : StationType.values()) {
                JsonNode stationNode = stations.path(stationType.toString());
                if (stationNode.isMissingNode()) {
                    System.out.println("[ConfigurationController] No ingredients found for station: " + stationType);
                    continue;
                }

                // Iterate through ingredients in this station
                stationNode.fields().forEachRemaining(entry -> {
                    String ingredientName = entry.getKey();
                    JsonNode ingredientData = entry.getValue();
                    int stock = ingredientData.path("stock").asInt();
                    double price = ingredientData.path("price").asDouble();

                    try {
                        inventory.addIngredient(ingredientName, stock, price, stationType);
                        System.out.println("[ConfigurationController] Added ingredient: " + ingredientName + 
                            " (Stock: " + stock + ", Price: $" + price + ", Station: " + stationType + ")");
                    } catch (Exception e) {
                        System.err.println("[ConfigurationController] Error adding ingredient " + 
                            ingredientName + ": " + e.getMessage());
                    }
                });
            }

            // Initialize other components
            this.collectionPoint = new CollectionPoint();
            this.stationManager = new StationManager(collectionPoint);
            this.orderManager = new OrderManager(collectionPoint, stationManager);
            this.kitchen = new Kitchen(orderManager, collectionPoint, stationManager);
            this.menu = new Menu(inventory);

            possibleRecipes.add(new BurgerRecipe(inventory));
            possibleRecipes.add(new KebabRecipe(inventory));

            // Configure views with constants from config
            configureViews(config);

        } catch (Exception e) {
            System.err.println("[ConfigurationController] Error setting up base components: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to setup base components", e);
        }
    }

    private void configureViews(JsonNode config) {
        try {
            // Get chef configuration view
            ChefConfigurationView chefView = (ChefConfigurationView) mediator.getView(ViewType.CHEF_CONFIGURATION);
            if (chefView != null) {
                // Set chef constants
                JsonNode chefRules = config.path("staffRules").path("chefs");
                if (!chefRules.isMissingNode()) {
                    int minChefs = chefRules.path("min").asInt(1);
                    int maxChefs = chefRules.path("max").asInt(20);
                    int maxStationsPerChef = chefRules.path("maxStationsPerChef").asInt(10);
                    int maxSpeed = chefRules.path("maxSpeed").asInt(5);
                    double standardPayPerHour = chefRules.path("standardPayPerHour").asDouble(15.0);
                    double payMultiplierBySpeed = chefRules.path("payMultiplierBySpeed").asDouble(1.0);
                    double payMultiplierByStation = chefRules.path("payMultiplierByStation").asDouble(1.0);
                    
                    chefView.setMinChefs(minChefs);
                    chefView.setMaxChefs(maxChefs);
                    chefView.setMaxStationsPerChef(maxStationsPerChef);
                    chefView.setMaxSpeed(maxSpeed);
                    chefView.setStandardPayPerHour(standardPayPerHour);
                    chefView.setPayMultiplierBySpeed(payMultiplierBySpeed);
                    chefView.setPayMultiplierByStation(payMultiplierByStation);
                    
                    System.out.println("[ConfigurationController] Set chef constants - Min Chefs: " + 
                        minChefs + ", Max Chefs: " + maxChefs + ", Max Stations Per Chef: " + maxStationsPerChef +
                        ", Max Speed: " + maxSpeed + ", Standard Pay: " + standardPayPerHour +
                        ", Speed Multiplier: " + payMultiplierBySpeed + ", Station Multiplier: " + payMultiplierByStation);
                }
                
                // Set kitchen constants
                JsonNode kitchenRules = config.path("kitchenRules");
                if (!kitchenRules.isMissingNode()) {
                    int minStations = kitchenRules.path("minStations").asInt(3);
                    int maxStations = kitchenRules.path("maxStations").asInt(20);
                    int maxInstancesOfStation = kitchenRules.path("maxInstancesOfStation").asInt(10);
                    int minInstancesOfStation = kitchenRules.path("minInstancesOfStation").asInt(1);
                    
                    chefView.setMinStations(minStations);
                    chefView.setMaxStations(maxStations);
                    chefView.setMaxInstancesOfStation(maxInstancesOfStation);
                    chefView.setMinInstancesOfStation(minInstancesOfStation);
                    
                    System.out.println("[ConfigurationController] Set kitchen constants - Min Stations: " + 
                        minStations + ", Max Stations: " + maxStations + 
                        ", Min Instances: " + minInstancesOfStation + 
                        ", Max Instances: " + maxInstancesOfStation);
                }
            } else {
                System.err.println("[ConfigurationController] Chef configuration view not found");
            }
            
            // Get dining configuration view
            DiningConfigurationView diningView = (DiningConfigurationView) mediator.getView(ViewType.DINING_CONFIGURATION);
            if (diningView == null) {
                System.err.println("[ConfigurationController] Dining configuration view not found");
                return;
            }

            // Set dining room constants
            JsonNode diningRoomRules = config.path("diningRoomRules");
            if (!diningRoomRules.isMissingNode()) {
                int maxTables = diningRoomRules.path("maxTables").asInt(20); // Default to 20 if not found
                int maxCapacity = diningRoomRules.path("maxCapacity").asInt(10); // Default to 10 if not found
                
                diningView.setMaxTables(maxTables);
                diningView.setMaxCapacity(maxCapacity);
                
                System.out.println("[ConfigurationController] Set dining room constants - Max Tables: " + 
                    maxTables + ", Max Capacity: " + maxCapacity);
            }

            // Set waiter constants
            JsonNode waiterRules = config.path("staffRules").path("waiters");
            if (!waiterRules.isMissingNode()) {
                int minWaiters = waiterRules.path("min").asInt(1); // Default to 1 if not found
                int maxWaiters = waiterRules.path("max").asInt(10); // Default to 10 if not found
                int maxSpeed = waiterRules.path("maxSpeed").asInt(5);
                double standardPayPerHour = waiterRules.path("standardPayPerHour").asDouble(10.0);
                double payMultiplierBySpeed = waiterRules.path("payMultiplierBySpeed").asDouble(1.0);
                
                diningView.setMinWaiters(minWaiters);
                diningView.setMaxWaiters(maxWaiters);
                diningView.setMaxSpeed(maxSpeed);
                diningView.setStandardPayPerHour(standardPayPerHour);
                diningView.setPayMultiplierBySpeed(payMultiplierBySpeed);
                
                System.out.println("[ConfigurationController] Set waiter constants - Min Waiters: " + 
                    minWaiters + ", Max Waiters: " + maxWaiters + ", Max Speed: " + maxSpeed +
                    ", Standard Pay: " + standardPayPerHour + ", Speed Multiplier: " + payMultiplierBySpeed);
            }
            
            // Set menu configuration constants
            MenuConfigurationView menuView = (MenuConfigurationView) mediator.getView(ViewType.MENU_CONFIGURATION);
            if (menuView != null) {
                JsonNode menuRules = config.path("menuRules");
                if (!menuRules.isMissingNode()) {
                    int minRecipes = menuRules.path("minRecipes").asInt(1); // Default to 1 if not found
                    int maxRecipes = menuRules.path("maxRecipes").asInt(10); // Default to 10 if not found
                    
                    menuView.setMinRecipes(minRecipes);
                    menuView.setMaxRecipes(maxRecipes);
                    
                    System.out.println("[ConfigurationController] Set menu constants - Min Recipes: " + 
                        minRecipes + ", Max Recipes: " + maxRecipes);
                }
            } else {
                System.err.println("[ConfigurationController] Menu configuration view not found");
            }
            
        } catch (Exception e) {
            System.err.println("[ConfigurationController] Error configuring views: " + e.getMessage());
            e.printStackTrace();
        }
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
        
        // Create stations based on configuration FIRST
        createStations(grillStationCount, prepStationCount, plateStationCount);
        
        // Create chefs AFTER stations exist
        createChefs(chefView.getChefs());
        
        // Create waiters and tables
        createWaitersAndTables(diningView.getWaiters(), diningView.getNumberOfTables(), diningView.getTableCapacity());
        
        // Create menu items
        createMenuItems(menuView.getSelectedRecipes());
        
        // Set configuration as complete
        configurationComplete = true;
        System.out.println("[ConfigurationController] Configuration complete");
        
        // Notify mediator that configuration is complete
        // mediator.notifyConfigurationComplete();
    }
    
    private void createDefaultConfiguration() {        
        // Define default stations
        List<String> defaultStations = Arrays.asList("Grill", "Prep", "Plate");
        
        // Create default stations FIRST
        for (String stationType : defaultStations) {
            Station station = new Station(StationType.valueOf(stationType.toUpperCase()), collectionPoint);
            station.setKitchen(kitchen);
            stationManager.addStation(station);
        }
        
        // Create default chef AFTER stations exist
        ChefStrategy strategy = new SimpleChefStrategy();
        Chef chef = new Chef(200.0, 2, strategy, stationManager);
        
        // Assign chef to stations
        for (String stationType : defaultStations) {
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
        
        for (ChefConfigurationView.ChefData data : chefData.values()) {
            try {
                ChefStrategy strategy = createChefStrategy(data.getStrategy());
                
                Chef chef = new Chef(
                    data.getName(),
                    data.getCostPerHour(),
                    data.getSpeed(),
                    strategy, 
                    stationManager  // Pass stationManager to chef
                );
                
                // Find available stations that match chef's qualifications
                for (String stationType : data.getStations()) {
                    StationType type = StationType.valueOf(stationType.toUpperCase());
                    // Find an unassigned station of this type
                    for (Station station : stationManager.getAllStations()) {
                        if (station.getType() == type && !station.hasChef()) {
                            chef.assignToStation(type);
                            System.out.println("[ConfigurationController] Assigned chef to " + type + " station");
                            break;
                        }
                    }
                }
                
                chefs.add(chef);
                System.out.println("[ConfigurationController] Created chef: " + chef.getName());
                
            } catch (Exception e) {
                System.err.println("[ConfigurationController] Error creating chef: " + e.getMessage());
                throw new RuntimeException("Failed to create chef: " + data.getName(), e);
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
        System.out.println("[ConfigurationController] Creating stations - Grill: " + grillCount + 
            ", Prep: " + prepCount + ", Plate: " + plateCount);
        
        // Clear existing stations
        stationManager.clearStations();
        
        int stationId = 0; // Keep track of total station count
        
        // Create stations
        stationId = createStationsOfType(StationType.GRILL, grillCount, stationId);
        stationId = createStationsOfType(StationType.PREP, prepCount, stationId);
        stationId = createStationsOfType(StationType.PLATE, plateCount, stationId);
        
        // Verify creation
        Map<StationType, Long> counts = stationManager.getAllStations().stream()
            .collect(Collectors.groupingBy(Station::getType, Collectors.counting()));
        
        System.out.println("[ConfigurationController] Station counts after creation:");
        counts.forEach((type, count) -> System.out.println("  " + type + ": " + count));
    }

    private int createStationsOfType(StationType type, int count, int startId) {
        System.out.println("[ConfigurationController] Creating " + count + " " + type + " stations starting at ID " + startId);
        for (int i = 0; i < count; i++) {
            Station station = new Station(type, collectionPoint);
            station.setKitchen(kitchen);
            stationManager.addStation(station);
            System.out.println("[ConfigurationController] Created " + type + " station " + startId);
            startId++;
        }
        return startId;
    }

    // Getters for restaurant components
    public Kitchen getKitchen() { return kitchen; }
    public OrderManager getOrderManager() { return orderManager; }
    public Inventory getInventory() { return inventory; }
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
