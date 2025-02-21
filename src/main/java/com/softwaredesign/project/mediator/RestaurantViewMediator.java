package com.softwaredesign.project.mediator;

import com.softwaredesign.project.controller.DiningRoomController;
import com.softwaredesign.project.view.DiningRoomView;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Mediator pattern implementation that handles communication between multiple controllers
 * and their corresponding views in the restaurant application.
 */
public class RestaurantViewMediator {
    private static RestaurantViewMediator instance;
    private final Map<String, List<DiningRoomView>> viewsByType;
    private final Map<String, Object> controllers;
    
    private RestaurantViewMediator() {
        this.viewsByType = new HashMap<>();
        this.controllers = new HashMap<>();
    }
    
    public static RestaurantViewMediator getInstance() {
        if (instance == null) {
            instance = new RestaurantViewMediator();
        }
        return instance;
    }
    
    /**
     * Register a controller with the mediator
     * @param type The type identifier for this controller (e.g., "DiningRoom", "Kitchen", etc.)
     * @param controller The controller instance
     */
    public void registerController(String type, Object controller) {
        System.out.println("[RestaurantViewMediator] Registering controller of type: " + type);
        controllers.put(type, controller);
    }
    
    /**
     * Register a view with the mediator
     * @param type The type identifier matching its controller (e.g., "DiningRoom", "Kitchen", etc.)
     * @param view The view instance
     */
    public void registerView(String type, DiningRoomView view) {
        System.out.println("[RestaurantViewMediator] Registering view for type: " + type);
        viewsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(view);
        
        // If we have a controller for this type, request initial updates
        Object controller = controllers.get(type);
        if (controller instanceof DiningRoomController) {
            System.out.println("[RestaurantViewMediator] Controller found for type " + type + ", requesting initial updates");
            ((DiningRoomController) controller).updateAllTableViews();
        }
    }
    
    /**
     * Unregister a view from the mediator
     * @param type The type identifier
     * @param view The view instance to remove
     */
    public void unregisterView(String type, DiningRoomView view) {
        List<DiningRoomView> views = viewsByType.get(type);
        if (views != null) {
            views.remove(view);
            if (views.isEmpty()) {
                viewsByType.remove(type);
            }
        }
    }
    
    /**
     * Notify all views of a specific type about a table update
     * @param type The type identifier
     * @param tableNumber The table number that was updated
     * @param capacity The table's capacity
     * @param occupied Number of occupied seats
     * @param status The table's status
     * @param waiterPresent The waiter assigned to the table
     */
    public void notifyViewsOfType(String type, int tableNumber, int capacity, int occupied, 
                                String status, char waiterPresent) {
        System.out.println("[RestaurantViewMediator] Notifying views of type " + type + 
                         " about update for table " + tableNumber);
        
        List<DiningRoomView> views = viewsByType.get(type);
        if (views != null) {
            for (DiningRoomView view : views) {
                view.onTableUpdate(tableNumber, capacity, occupied, status, waiterPresent);
            }
        }
    }
    
    /**
     * Get a controller of a specific type
     * @param type The type identifier
     * @return The controller instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getController(String type) {
        return (T) controllers.get(type);
    }
}
