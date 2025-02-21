package com.softwaredesign.project.view;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import jexer.*;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.orderfulfillment.Table;

public class RestaurantApplication extends TApplication {
    private TWindow mainWindow;
    private Map<ViewType, View> views = new HashMap<>();

    public RestaurantApplication() throws Exception {
        super(BackendType.SWING);
        System.out.println("[RestaurantApplication] Starting application initialization");

        // Create the main window first
        mainWindow = new TWindow(this, "OOPsies Bistro", 0, 0, getScreen().getWidth(), getScreen().getHeight());
        mainWindow.maximize();

        // Initialize views before showing any
        initializeViews();

        // Add Help menu
        TMenu helpMenu = addMenu("&Help");
        helpMenu.addItem(1025, "&Restart Game");

        // Show initial view last
        showView(ViewType.WELCOME);
        System.out.println("[RestaurantApplication] Application initialization complete");
    }

    

    private void initializeViews() {
        System.out.println("[RestaurantApplication] Initializing views");
        for (ViewType viewType : ViewType.values()) {
            try {
                View view = viewType.getViewClass()
                    .getDeclaredConstructor(RestaurantApplication.class)
                    .newInstance(this);
                views.put(viewType, view);
                System.out.println("[RestaurantApplication] Initialized view: " + viewType);
            } catch (Exception e) {
                System.err.println("[RestaurantApplication] Failed to initialize view: " + viewType);
                throw new RuntimeException("Failed to initialize view: " + viewType, e);
            }
        }
    }

    public void showView(ViewType viewType) {
        System.out.println("[RestaurantApplication] Showing view: " + viewType);
        // Create a new ArrayList to avoid concurrent modification
        List<TWidget> widgetsToRemove = new ArrayList<>(mainWindow.getChildren());
        
        // Remove all widgets safely
        for (TWidget widget : widgetsToRemove) {
            mainWindow.remove(widget);
        }

        // Show the selected view
        View viewToShow = views.get(viewType);
        if (viewToShow != null) {
            viewToShow.initialize(mainWindow);
            mainWindow.show();
        } else {
            throw new IllegalArgumentException("Unknown view: " + viewType);
        }
    }

    public TWindow getMainWindow() {
        return mainWindow;
    }

    public Map<ViewType, View> getViews() {
        return views;
    }

    @Override
    public boolean onMenu(TMenuEvent menu) {
        switch (menu.getId()) {
            case 1025: // MENU_RESTART
                showView(ViewType.WELCOME);
                break;
            default:
                return super.onMenu(menu);
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        RestaurantApplication app = new RestaurantApplication();
        app.run();
    }
}
