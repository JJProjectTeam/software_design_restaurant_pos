package com.softwaredesign.project.mediator;

import com.softwaredesign.project.controller.BaseController;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;
import com.softwaredesign.project.view.ConfigurableView;
import com.softwaredesign.project.controller.ConfigurationController;
import com.softwaredesign.project.model.BankBalanceSingleton;

import java.util.*;

/**
 * Mediator that maintains mappings between views and their controllers.
 */
public class RestaurantViewMediator {
    private static RestaurantViewMediator instance;
    private final Map<ViewType, List<View>> registeredViews;
    private final Map<String, BaseController> controllers;
    public BankBalanceSingleton bankBalanceSingleton;
    
    private RestaurantViewMediator() {
        registeredViews = new HashMap<>();
        controllers = new HashMap<>();
    }
    
    public static RestaurantViewMediator getInstance() {
        if (instance == null) {
            instance = new RestaurantViewMediator();
        }
        return instance;
    }
    
    /**
     * Register a view with its corresponding controller type
     */
    public void registerView(ViewType type, View view) {
        System.out.println("[RestaurantViewMediator] Registering view for type: " + type);
        registeredViews.computeIfAbsent(type, k -> new ArrayList<>()).add(view);
    }
    
    /**
     * Unregister a view
     */
    public void unregisterView(String type, View view) {
        System.out.println("[RestaurantViewMediator] Unregistering view for type: " + type);
        List<View> views = registeredViews.get(type);
        if (views != null) {
            views.remove(view);
            if (views.isEmpty()) {
                registeredViews.remove(type);
            }
        }
    }
    
    /**
     * Register a controller
     */
    public void registerController(String type, BaseController controller) {
        System.out.println("[RestaurantViewMediator] Registering controller of type: " + type);
        controllers.put(type, controller);
    }
    
    /**
     * Get a controller of a specific type
     */
    public BaseController getController(String type) {
        return controllers.get(type);
    }
    
    /**
     * Get all views of a specific type
     */
    public List<View> getViews(String type) {
        return registeredViews.getOrDefault(type, new ArrayList<>());
    }

    /**
     * Notify all views of a specific type to update
     */
    public void notifyViewUpdate(String type) {
        System.out.println("[RestaurantViewMediator] Notifying views of type " + type + " to update");
        BaseController controller = controllers.get(type);
        if (controller != null) {
            List<View> views = registeredViews.get(type);
            if (views != null) {
                for (View view : views) {
                    if (view instanceof ConfigurableView) {
                        ((ConfigurableView) view).onUpdate(controller);
                    }
                }
            }
        }
    }
    public View getView(ViewType type) {
        return registeredViews.get(type).get(0);
    }

    public void notifyConfigurationComplete(){
        getController("Configuration").onUserInput();
    }

    public void notifyBankBalanceChanged(double newBalance) {
        ConfigurationController configController = (ConfigurationController) getController("Configuration");
        BankBalanceSingleton.getInstance().setBankBalance(newBalance);
        if (configController != null) {
            configController.updateBankBalance(newBalance);
        }
    }

    /**
     * Reset the mediator by clearing all registered controllers and views
     */
    public void reset() {
        System.out.println("[RestaurantViewMediator] Resetting mediator - clearing all controllers and views");
        controllers.clear();
        registeredViews.clear();
    }
}
