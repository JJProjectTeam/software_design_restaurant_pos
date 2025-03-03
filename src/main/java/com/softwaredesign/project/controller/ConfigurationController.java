package com.softwaredesign.project.controller;

import java.util.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.view.*;
import com.softwaredesign.project.kitchen.*;
import com.softwaredesign.project.order.*;
import com.softwaredesign.project.inventory.*;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;

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
    private int QUANTITY = 10;
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
        this.stationManager = new StationManager();
        this.orderManager = new OrderManager(collectionPoint, stationManager);
        this.kitchen = new Kitchen(orderManager, inventory, collectionPoint);
        this.menu = new Menu(inventory);
    }

    // Methods to read from views and create restaurant entities
    public void createRestaurantComponents() {
        ChefConfigurationView chefView = (ChefConfigurationView) mediator.getView("ChefConfiguration");
        DiningConfigurationView diningView = (DiningConfigurationView) mediator.getView("DiningConfiguration");
        MenuConfigurationView menuView = (MenuConfigurationView) mediator.getView("MenuConfiguration");

        createChefs(chefView.getChefs());
        createWaitersAndTables(diningView.getWaiters(), diningView.getNumberOfTables());
        createMenuItems(menuView.getMenuItems());
    }

    private void createChefs(Map<String, ChefConfigurationView.ChefData> chefData) {
        for (var entry : chefData.entrySet()) {
            var data = entry.getValue();
            
            // Create chef strategy based on selection
            ChefStrategy strategy = createChefStrategy(data.getStrategy());
            
            // Create chef stations
            List<Station> stations = new ArrayList<>();
            for (String stationType : data.getStations()) {
                Station station = new Station(StationType.valueOf(stationType.toUpperCase()));
                stations.add(station);
                stationManager.addStation(station);
            }
            
            Chef chef = new Chef(data.getCostPerHour(), data.getSpeed(), strategy, stationManager);
            for (Station station : stations) {
                chef.assignToStation(station.getType());
            }
        }
    }

    private ChefStrategy createChefStrategy(String strategyName) {
        return switch (strategyName.toUpperCase()) {
            case "FIFO", "OLDEST" -> new OldestOrderFirstStrategy();
            case "LIFO", "NEWEST" -> new NewestOrderFirstStrategy(); // You'll need to create this class
            default -> new OldestOrderFirstStrategy(); // Default to FIFO/Oldest
        };
    }

    private void createWaitersAndTables(Map<String, DiningConfigurationView.WaiterData> waiterData, int tableCount) {
        // Create tables
        tables.clear();
        //TODO this should be creating a seating plan!!
        for (int i = 0; i < tableCount; i++) {
            tables.add(new Table(i + 1));
        }

        // Create waiters
        waiters.clear();
        for (var entry : waiterData.entrySet()) {
            var data = entry.getValue();
            Waiter waiter = new Waiter(data.getCostPerHour(), data.getSpeed(), orderManager, menu);
            
            // TODO This will be done by seating plan
            for (Integer tableNum : data.assignedTables) {
                if (tableNum > 0 && tableNum <= tables.size()) {
                    waiter.assignTable(tables.get(tableNum - 1));
                }
            }
            
            waiters.add(waiter);
        }
    }

    private void createMenuItems(Map<String, MenuConfigurationView.MenuItem> menuData) {
        for (var entry : menuData.entrySet()) {
            var data = entry.getValue();
            
            // TODO way of doing recipe has to change!!! If we want users to be able to create then they shouldnt be concrete classes!!
            //it has to be able to create a recipe with a list of ingredients
            //not require one of predefined concrete recipe
            Recipe recipe = new Recipe(data.getName(), inventory);
            menu.addMenuItem(data.getName(), recipe, data.getPrice);
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
