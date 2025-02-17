package com.softwaredesign.project.interceptor;

import com.softwaredesign.project.inventory.IObserver;
import java.util.HashMap;
import java.util.Map;

/**
 * Observer that monitors inventory and triggers the interceptor pattern
 */
public class InventoryMonitorObserver implements IObserver {
    private final Map<String, Integer> stockLevels;
    private final Dispatcher dispatcher;

    public InventoryMonitorObserver(Dispatcher dispatcher) {
        this.stockLevels = new HashMap<>();
        this.dispatcher = dispatcher;
    }

    @Override
    public void update(String ingredient, int quantity) {
        stockLevels.put(ingredient, quantity);
        
        // Create context and dispatch to interceptors
        InterceptorContext context = new InterceptorContext(new HashMap<>(stockLevels));
        dispatcher.dispatch(context);
    }
}
