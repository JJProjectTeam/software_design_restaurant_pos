package com.softwaredesign.project.view;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import jexer.*;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.RestaurantDriver;

public class RestaurantApplication extends TApplication {
    private TWindow mainWindow;
    private Map<ViewType, View> views = new HashMap<>();
    private ViewType currentView;
    private RestaurantDriver driver;

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

        System.out.println("[RestaurantApplication] Application initialization complete");
    }

    private void initializeViews() {
        System.out.println("[RestaurantApplication] Initializing views");
        for (ViewType viewType : ViewType.values()) {
            try {
                System.out.println("[RestaurantApplication] Creating view instance for: " + viewType);
                View view = viewType.getViewClass()
                    .getDeclaredConstructor(RestaurantApplication.class)
                    .newInstance(this);
                views.put(viewType, view);
                System.out.println("[RestaurantApplication] Initialized view: " + viewType);
            } catch (Exception e) {
                System.err.println("[RestaurantApplication] ERROR: Failed to initialize view: " + viewType);
                System.err.println("[RestaurantApplication] Exception: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize view: " + viewType, e);
            }
        }
        System.out.println("[RestaurantApplication] All views initialized successfully");
    }

    public void showView(ViewType viewType) {
        if (currentView == viewType) {
            System.out.println("[RestaurantApplication] Already showing view: " + viewType);
            return;
        }

        try {
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
                
                // Force a refresh of the view through the mediator
                if (viewType == ViewType.DINING_ROOM) {
                    RestaurantViewMediator.getInstance().notifyViewUpdate("DiningRoom");
                }
                
                // Add a small delay to ensure all UI elements are properly initialized
                try {
                    Thread.sleep(100);  // 100ms delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                mainWindow.show();
                
                // Force a UI update by doing a dummy operation
                mainWindow.setTitle(mainWindow.getTitle());
                
                currentView = viewType;
            } else {
                System.err.println("[RestaurantApplication] ERROR: Unknown view: " + viewType);
                throw new IllegalArgumentException("Unknown view: " + viewType);
            }
        } catch (Exception e) {
            System.err.println("[RestaurantApplication] ERROR in showView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        // Don't show any view here - let the RestaurantDriver handle it
        // showView(ViewType.MENU_CONFIGURATION);
        
        // Start the event loop
        super.run();
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
                restartApplication();
                break;
            default:
                return super.onMenu(menu);
        }
        return true;
    }

    /**
     * Sets the driver reference for restart functionality
     */
    public void setDriver(RestaurantDriver driver) {
        this.driver = driver;
    }

    /**
     * Properly restarts the application by reinitializing all views and resetting the application state.
     */
    private void restartApplication() {
        System.out.println("[RestaurantApplication] Restarting application");
        try {
            // Use SwingUtilities.invokeLater to ensure UI updates happen on the EDT
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    // Clear the current view
                    currentView = null;
                    
                    // Clear all widgets from the main window
                    List<TWidget> widgetsToRemove = new ArrayList<>(mainWindow.getChildren());
                    for (TWidget widget : widgetsToRemove) {
                        mainWindow.remove(widget);
                    }
                    
                    // Reset the mediator
                    RestaurantViewMediator mediator = RestaurantViewMediator.getInstance();
                    mediator.reset();
                    
                    // Reinitialize all views
                    views.clear();
                    initializeViews();
                    
                    // If driver is available, use it for a complete restart
                    if (driver != null) {
                        driver.restart();
                    } else {
                        // Fallback to just showing welcome view
                        showView(ViewType.WELCOME);
                    }
                    
                    System.out.println("[RestaurantApplication] Application restart completed");
                } catch (Exception e) {
                    System.err.println("[RestaurantApplication] ERROR during restart: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("[RestaurantApplication] ERROR during restart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        RestaurantApplication app = new RestaurantApplication();
        app.run();
    }
}
