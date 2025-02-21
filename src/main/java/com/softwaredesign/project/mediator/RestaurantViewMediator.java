package com.softwaredesign.project.mediator;

import com.softwaredesign.project.view.GeneralView;
import com.softwaredesign.project.controller.BaseController;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Mediator that maintains mappings between views and their controllers.
 */
public class RestaurantViewMediator {
    private static RestaurantViewMediator instance;
    private final Map<String, List<GeneralView>> viewsByType;
    private final Map<String, BaseController> controllers;
    
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
     * Register a controller
     */
    public void registerController(String type, BaseController controller) {
        System.out.println("[RestaurantViewMediator] Registering controller of type: " + type);
        controllers.put(type, controller);
    }
    
    /**
     * Register a view with its corresponding controller type
     */
    public void registerView(String type, GeneralView view) {
        System.out.println("[RestaurantViewMediator] Registering view for type: " + type);
        viewsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(view);
        
        // If we have a controller for this type, request initial update
        BaseController controller = controllers.get(type);
        if (controller != null) {
            System.out.println("[RestaurantViewMediator] Controller found for type " + type + ", requesting initial update");
            controller.updateView();
        }
    }
    
    /**
     * Unregister a view
     */
    public void unregisterView(String type, GeneralView view) {
        List<GeneralView> views = viewsByType.get(type);
        if (views != null) {
            views.remove(view);
            if (views.isEmpty()) {
                viewsByType.remove(type);
            }
        }
    }
    
    /**
     * Get all views of a specific type
     */
    public List<GeneralView> getViews(String type) {
        return viewsByType.getOrDefault(type, new ArrayList<>());
    }
    
    /**
     * Get a controller of a specific type
     */
    public BaseController getController(String type) {
        return controllers.get(type);
    }
}
