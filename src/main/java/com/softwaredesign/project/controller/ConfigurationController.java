package com.softwaredesign.project.controller;

import java.util.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;

public class ConfigurationController extends BaseController {
    private Map<String, ChefConfig> chefs;
    private Map<String, WaiterConfig> waiters;
    private Map<String, MenuItem> menuItems;
    private int numberOfTables;
    private RestaurantViewMediator mediator;

    public ConfigurationController() {
        super("Configuration");
        this.chefs = new HashMap<>();
        this.waiters = new HashMap<>();
        this.menuItems = new HashMap<>();
        this.numberOfTables = 0;
        this.mediator = RestaurantViewMediator.getInstance();
        
        // Register with mediator
        mediator.registerController("Configuration", this);
    }

    // Chef configuration methods
    public void addChef(String name, List<String> stations, int speed, double costPerHour, String strategy) {
        chefs.put(name, new ChefConfig(name, stations, speed, costPerHour, strategy));
        updateView();
    }

    public Map<String, ChefConfig> getChefs() {
        return Collections.unmodifiableMap(chefs);
    }

    // Waiter configuration methods
    public void addWaiter(String name, int speed, double costPerHour) {
        waiters.put(name, new WaiterConfig(name, speed, costPerHour));
        updateView();
    }

    public Map<String, WaiterConfig> getWaiters() {
        return Collections.unmodifiableMap(waiters);
    }

    // Menu configuration methods
    public void addMenuItem(String name, Map<String, Integer> ingredients, double price) {
        menuItems.put(name, new MenuItem(name, ingredients, price));
        updateView();
    }

    public Map<String, MenuItem> getMenuItems() {
        return Collections.unmodifiableMap(menuItems);
    }

    // Table configuration methods
    public void setNumberOfTables(int tables) {
        this.numberOfTables = tables;
        updateView();
    }

    public int getNumberOfTables() {
        return numberOfTables;
    }

    // Configuration validation
    public boolean isValid() {
        return !chefs.isEmpty() && !waiters.isEmpty() && !menuItems.isEmpty() && numberOfTables > 0;
    }

    @Override
    public void updateView() {
        // The mediator will handle notifying all registered views
        mediator.notifyViewUpdate("Configuration");
    }

    // Configuration data classes
    public static class ChefConfig {
        private final String name;
        private final List<String> stations;
        private final int speed;
        private final double costPerHour;
        private final String strategy;

        public ChefConfig(String name, List<String> stations, int speed, double costPerHour, String strategy) {
            this.name = name;
            this.stations = new ArrayList<>(stations);
            this.speed = speed;
            this.costPerHour = costPerHour;
            this.strategy = strategy;
        }

        public String getName() { return name; }
        public List<String> getStations() { return Collections.unmodifiableList(stations); }
        public int getSpeed() { return speed; }
        public double getCostPerHour() { return costPerHour; }
        public String getStrategy() { return strategy; }
    }

    public static class WaiterConfig {
        private final String name;
        private final int speed;
        private final double costPerHour;
        private final List<Integer> assignedTables;

        public WaiterConfig(String name, int speed, double costPerHour) {
            this.name = name;
            this.speed = speed;
            this.costPerHour = costPerHour;
            this.assignedTables = new ArrayList<>();
        }

        public String getName() { return name; }
        public int getSpeed() { return speed; }
        public double getCostPerHour() { return costPerHour; }
        public List<Integer> getAssignedTables() { return Collections.unmodifiableList(assignedTables); }
    }

    public static class MenuItem {
        private final String name;
        private final Map<String, Integer> ingredients;
        private final double price;

        public MenuItem(String name, Map<String, Integer> ingredients, double price) {
            this.name = name;
            this.ingredients = new HashMap<>(ingredients);
            this.price = price;
        }

        public String getName() { return name; }
        public Map<String, Integer> getIngredients() { return Collections.unmodifiableMap(ingredients); }
        public double getPrice() { return price; }
    }
}
