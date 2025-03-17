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
import com.softwaredesign.project.staff.chefstrategies.DynamicChefStrategy;
import com.softwaredesign.project.staff.staffspeeds.BaseSpeed;
import com.softwaredesign.project.staff.staffspeeds.CaffeineAddictDecorator;
import com.softwaredesign.project.staff.staffspeeds.StimulantAddictDecorator;
import com.softwaredesign.project.staff.staffspeeds.ISpeedComponent;
import com.softwaredesign.project.staff.staffspeeds.LethargicDecorator;

import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Paths;
import java.nio.file.Files;
import com.softwaredesign.project.model.BankBalanceSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
    private static final String CONFIG_ERROR_MSG = "[ConfigurationController] Error reading config file: ";

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
    private InventoryStockTracker inventoryStockTracker;

    private double bankBalance;
    private double chefStandardPay;
    private double chefPayMultiplierBySpeed;
    private double chefPayMultiplierByStation;
    private double waiterStandardPay;

    private List<Recipe> possibleRecipes;

    //constructor registers with mediator
    public ConfigurationController() {
        super("Configuration");
        this.inventory = new Inventory(); // Initialize with concrete Inventory class
        this.mediator = RestaurantViewMediator.getInstance();
        this.possibleRecipes = new ArrayList<>(); 
        
        // Order matters here
        setupBaseComponents();
        mediator.registerController("Configuration", this);
        
        // Only initialize menu after everything else is set up
        initializeMenuConfiguration();
        
    }

    //this should set up the components
    private void setupBaseComponents() {        
        try {
            JsonNode config = loadConfiguration();
            JsonNode stations = config.path("inventory").path("stations");

            logger.info("[ConfigurationController] Loading ingredients from config file...");

            bankBalance = config.path("initialBankBalance").asDouble(1000.0);

            // Iterate through each station type
            for (StationType stationType : StationType.values()) {
                JsonNode stationNode = stations.path(stationType.toString());
                if (stationNode.isMissingNode()) {
                    logger.info("[ConfigurationController] No ingredients found for station: {}", stationType);
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
                        logger.info("[ConfigurationController] Added ingredient: {} (Stock: {}, Price: ${}, Station: {})",
                            ingredientName, stock, price, stationType);
                    } catch (Exception e) {
                        logger.error("[ConfigurationController] Error adding ingredient {}: {}", 
                            ingredientName, e.getMessage());
                    }
                });
            }
            // Load pay-related configuration
            JsonNode payConfig = config.path("staffRules");
            
            // Load chef pay details
            JsonNode chefPay = payConfig.path("chefs");
            if (!chefPay.isMissingNode()) {
                this.chefStandardPay = chefPay.path("standardPay").asDouble(15.0);
                this.chefPayMultiplierBySpeed = chefPay.path("payMultiplierBySpeed").asDouble(1.0); 
                this.chefPayMultiplierByStation = chefPay.path("payMultiplierByStation").asDouble(1.0);
                logger.info("[ConfigurationController] Loaded chef pay config - Standard Pay: ${}, Speed Multiplier: {}, Station Multiplier: {}", 
                    chefStandardPay, chefPayMultiplierBySpeed, chefPayMultiplierByStation);
            }

            // Load waiter pay details
            JsonNode waiterPay = payConfig.path("waiters");
            if (!waiterPay.isMissingNode()) {
                this.waiterStandardPay = waiterPay.path("standardPay").asDouble(10.0);
                logger.info("[ConfigurationController] Loaded waiter pay config - Standard Pay: ${}", 
                    waiterStandardPay);
            }

            // Initialize other components
            this.collectionPoint = new CollectionPoint();
            this.stationManager = new StationManager(collectionPoint);
            this.orderManager = new OrderManager(collectionPoint, stationManager);
            this.kitchen = new Kitchen(orderManager, collectionPoint, stationManager);
            this.menu = new Menu(inventory);

            // create an set up inventory stock tracker and attach it to the inventory
            this.inventoryStockTracker = new InventoryStockTracker();
            this.inventory.attach(inventoryStockTracker);


            possibleRecipes.add(new BurgerRecipe(inventory));
            possibleRecipes.add(new KebabRecipe(inventory));

            // Configure views with constants from config
            configureViews(config);

        } catch (Exception e) {
            logger.error("[ConfigurationController] Error setting up base components: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to setup base components", e);
        }
    }
    // abstracted load configuration
    private JsonNode loadConfiguration() {
        try {
            String configPath = "src/main/config.json";
            String jsonContent = Files.readString(Paths.get(configPath));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonContent);
        } catch (Exception e) {
            logger.error(CONFIG_ERROR_MSG + "{}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration", e);
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
                    chefView.setStandardPay(chefStandardPay);
                    chefView.setPayMultiplierBySpeed(chefPayMultiplierBySpeed);
                    chefView.setPayMultiplierByStation(chefPayMultiplierByStation);
                    //todo - also for waiters
                    
                    logger.info("[ConfigurationController] Set chef constants - Min Chefs: {}, Max Chefs: {}, Max Stations Per Chef: {}, Max Speed: {}, Standard Pay: {}, Speed Multiplier: {}, Station Multiplier: {}", 
                        minChefs, maxChefs, maxStationsPerChef, maxSpeed, chefStandardPay, chefPayMultiplierBySpeed, chefPayMultiplierByStation);
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
                    
                    logger.info("[ConfigurationController] Set kitchen constants - Min Stations: {}, Max Stations: {}, Min Instances: {}, Max Instances: {}", 
                        minStations, maxStations, minInstancesOfStation, maxInstancesOfStation);
                }
            } else {
                logger.error("[ConfigurationController] Chef configuration view not found");
            }
            
            // Get dining configuration view
            DiningConfigurationView diningView = (DiningConfigurationView) mediator.getView(ViewType.DINING_CONFIGURATION);
            if (diningView == null) {
                logger.error("[ConfigurationController] Dining configuration view not found");
                return;
            }

            // Set dining room constants
            JsonNode diningRoomRules = config.path("diningRoomRules");
            if (!diningRoomRules.isMissingNode()) {
                int maxTables = diningRoomRules.path("maxTables").asInt(20); // Default to 20 if not found
                int maxCapacity = diningRoomRules.path("maxCapacity").asInt(10); // Default to 10 if not found
                
                diningView.setMaxTables(maxTables);
                diningView.setMaxCapacity(maxCapacity);
                
                logger.info("[ConfigurationController] Set dining room constants - Max Tables: {}, Max Capacity: {}", 
                    maxTables, maxCapacity);
            }

            // Set waiter constants
            JsonNode waiterRules = config.path("staffRules").path("waiters");
            if (!waiterRules.isMissingNode()) {
                int minWaiters = waiterRules.path("min").asInt(1); // Default to 1 if not found
                int maxWaiters = waiterRules.path("max").asInt(10); // Default to 10 if not found
                double standardPayPerHour = waiterRules.path("standardPayPerHour").asDouble(10.0);
                diningView.setMinWaiters(minWaiters);
                diningView.setMaxWaiters(maxWaiters);
                diningView.setStandardPayPerHour(standardPayPerHour);
                
                logger.info("[ConfigurationController] Set waiter constants - Min Waiters: {}, Max Waiters: {}, Max Speed: {}, Standard Pay: {}, Speed Multiplier: {}", 
                    minWaiters, maxWaiters, standardPayPerHour);
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
                    
                    logger.info("[ConfigurationController] Set menu constants - Min Recipes: {}, Max Recipes: {}", 
                        minRecipes, maxRecipes);
                }
            } else {
                logger.error("[ConfigurationController] Menu configuration view not found");
            }

            updateBankBalance(bankBalance);
            
        } catch (Exception e) {
            logger.error("[ConfigurationController] Error configuring views: {}", e.getMessage());
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

        logger.info("[ConfigurationController] Final configuration bank balance set to: ${}", String.format("%.2f", bankBalance));
        
        // Set configuration as complete
        configurationComplete = true;
        logger.info("[ConfigurationController] Configuration complete");
        
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
        Chef chef = new Chef(200.0, new BaseSpeed(), strategy, stationManager);
        
        // Assign chef to stations
        for (String stationType : defaultStations) {
            chef.assignToStation(StationType.valueOf(stationType.toUpperCase()));
        }
        
        // Create default tables
        seatingPlan = new SeatingPlan(4, 40, 5, menu);
        

        // Create default waiter
        Waiter waiter = new Waiter(20.0, orderManager, menu, inventoryStockTracker);
        
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
        JsonNode chefRules;
        try {
            logger.info("[ConfigurationController] Creating chefs from configuration");
            chefs = new ArrayList<>();
            JsonNode config = loadConfiguration();
            chefRules = config.path("staffRules").path("chefs");
        } catch (Exception e) {
            logger.error("[ConfigurationController] Error reading config file: {}", e.getMessage());
            e.printStackTrace();
            return;
        }
        double chanceOfCaffieneAddict = chefRules.path("chanceOfCaffieneAddict").asDouble(0.3);
        double chanceOfLethargic = chefRules.path("chanceOfLethargic").asDouble(0.3);
        double chanceOfStimulantAddict = chefRules.path("chanceOfStimulantAddict").asDouble(0.25);
        
        for (ChefConfigurationView.ChefData data : chefData.values()) {
            try {
                // Create strategy based on configuration
                ChefStrategy strategy = createChefStrategy(data.getStrategy());
                ISpeedComponent speedComponent = new BaseSpeed(data.getSpeed());
                
                // Apply speed modifiers in a consistent order to maintain SOLID principles
                if (Math.random() < chanceOfLethargic) {
                    speedComponent = new LethargicDecorator(speedComponent);
                }
                if (Math.random() < chanceOfCaffieneAddict) {
                    speedComponent = new CaffeineAddictDecorator(speedComponent);
                }
                if (Math.random() < chanceOfStimulantAddict) {
                    speedComponent = new StimulantAddictDecorator(speedComponent);
                }

                Chef chef = new Chef(
                    data.getName(),
                    data.getCost(),
                    speedComponent,
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
                            logger.info("[ConfigurationController] Assigned chef to {}", type);
                            break;
                        }
                    }
                }
                
                chefs.add(chef);
                logger.info("[ConfigurationController] Created chef: {}", chef.getName());
                
            } catch (Exception e) {
                logger.error("[ConfigurationController] Error creating chef: {}", e.getMessage());
                throw new RuntimeException("Failed to create chef: " + data.getName(), e);
            }
        }
    }

    private ChefStrategy createChefStrategy(String strategyName) {
        logger.info("[ConfigurationController] Creating chef strategy: {}", strategyName);
        return switch (strategyName.toUpperCase()) {
            case "DYNAMIC" -> new DynamicChefStrategy(stationManager);
            case "OLDEST" -> new OldestOrderFirstStrategy();
            case "LONGEST_QUEUE" -> new LongestQueueFirstStrategy();
            case "SIMPLE" -> new SimpleChefStrategy();
            default -> {
                logger.warn("[ConfigurationController] Unknown strategy '{}', defaulting to SimpleChefStrategy", strategyName);
                yield new SimpleChefStrategy();
            }
        };
    }

    private void createWaitersAndTables(Map<String, DiningConfigurationView.WaiterData> waiterData, int tableCount, int tableCapacity) {
        // Initialize waiters list first
        this.waiters = new ArrayList<>();

        // Create tables
        logger.info("[ConfigurationController] Creating tables - Count: {}, Capacity: {}", tableCount, tableCapacity);
        seatingPlan = new SeatingPlan(tableCount, 40, tableCapacity, menu);
        logger.info("[ConfigurationController] Created seating plan with {} tables", seatingPlan.getAllTables().size());

        // Get all tables from seating plan
        List<Table> allTables = seatingPlan.getAllTables();
        
        // Calculate table distribution
        int tablesPerWaiter = allTables.size() / waiterData.size();
        int extraTables = allTables.size() % waiterData.size();
        int tableIndex = 0;

        logger.info("[ConfigurationController] Distributing {} tables among {} waiters", allTables.size(), waiterData.size());
        JsonNode waiterRules;
        try{
            JsonNode config = loadConfiguration();
            waiterRules = config.path("staffRules").path("waiters");
        } catch (Exception e) {
            logger.error("[ConfigurationController] Error reading config file: {}", e.getMessage());
            e.printStackTrace();
            return;
        }
        
        for (var entry : waiterData.entrySet()) {
            var data = entry.getValue();

            Waiter waiter = new Waiter(data.getCostPerHour(), orderManager, menu, inventoryStockTracker);
            
            // Calculate tables for this waiter
            int tablesToAssign = tablesPerWaiter + (waiters.size() < extraTables ? 1 : 0);
            logger.info("[ConfigurationController] Assigning {} tables to waiter", tablesToAssign);
            
            // Assign tables
            for (int i = 0; i < tablesToAssign && tableIndex < allTables.size(); i++) {
                waiter.assignTable(allTables.get(tableIndex));
                tableIndex++;
            }
            
            waiters.add(waiter);
            logger.info("[ConfigurationController] Added waiter with {} tables", tablesToAssign);
        }
        
        logger.info("[ConfigurationController] Created {} waiters", waiters.size());
    }

    //This allows us to map the selected recipes to actual instances, without duplicating listing what recipes are available
    private void createMenuItems(Set<String> selectedRecipes) {
        // First, create a new Menu with the inventory service
        this.menu = new Menu(inventory);
        
        // Create a map of recipe names to their corresponding Recipe objects
        Map<String, Recipe> recipeMap = possibleRecipes.stream()
            .collect(Collectors.toMap(
                recipe -> recipe.getName().toLowerCase(),
                recipe -> recipe
            ));
        
        // For each selected recipe, look up and create a new instance
        for (String recipeName : selectedRecipes) {
            try {
                Recipe templateRecipe = recipeMap.get(recipeName.toLowerCase());
                if (templateRecipe != null) {
                    // Create a new instance of the same type of recipe
                    Recipe newRecipe = templateRecipe.getClass()
                        .getConstructor(InventoryService.class)
                        .newInstance(inventory);
                    
                    // TODO: Uncomment when menu implementation is ready
                    // menu.addRecipe(newRecipe);
                    
                    logger.info("[ConfigurationController] Created recipe: {}", recipeName);
                } else {
                    logger.error("[ConfigurationController] Unknown recipe: {} - skipping", recipeName);
                }
            } catch (Exception e) {
                logger.error("[ConfigurationController] Error creating menu item: {} - {}", 
                    recipeName, e.getMessage());
            }
        }
    }

    /**
     * Create stations based on the configuration
     */
    private void createStations(int grillCount, int prepCount, int plateCount) {
        logger.info("[ConfigurationController] Creating stations - Grill: {}, Prep: {}, Plate: {}", 
            grillCount, prepCount, plateCount);
        
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
        
        logger.info("[ConfigurationController] Station counts after creation:");
        counts.forEach((type, count) -> logger.info("  {}: {}", type, count));
    }

    private int createStationsOfType(StationType type, int count, int startId) {
        logger.info("[ConfigurationController] Creating {} {} stations starting at ID {}", count, type, startId);
        for (int i = 0; i < count; i++) {
            Station station = new Station(type, collectionPoint);
            station.setKitchen(kitchen);
            stationManager.addStation(station);
            logger.info("[ConfigurationController] Created {} station {}", type, startId);
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
            logger.error("[ConfigurationController] Error initializing menu: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateBankBalance(double newBalance) {
        try {
            BankBalanceSingleton.getInstance().setBankBalance(newBalance);
            this.bankBalance = newBalance;
            // Update all configuration views
            ChefConfigurationView chefView = (ChefConfigurationView) mediator.getView(ViewType.CHEF_CONFIGURATION);
            DiningConfigurationView diningView = (DiningConfigurationView) mediator.getView(ViewType.DINING_CONFIGURATION);
            MenuConfigurationView menuView = (MenuConfigurationView) mediator.getView(ViewType.MENU_CONFIGURATION);

            if (chefView != null) chefView.setBankBalance(newBalance);
            if (diningView != null) diningView.setBankBalance(newBalance);
            if (menuView != null) menuView.setBankBalance(newBalance);

            logger.info("[ConfigurationController] Updated bank balance to: ${}", newBalance);
        } catch (Exception e) {
            logger.error("[ConfigurationController] Error updating bank balance: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateChefCost(int speed, int numberOfStations) {
        return chefStandardPay * (speed * chefPayMultiplierBySpeed) * 
               (numberOfStations * chefPayMultiplierByStation);
    }

    private double calculateWaiterCost(int speed) {
        return waiterStandardPay;
    }
}
