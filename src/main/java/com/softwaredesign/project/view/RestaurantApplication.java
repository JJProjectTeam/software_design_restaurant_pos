package com.softwaredesign.project.view;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import jexer.*;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

public class RestaurantApplication extends TApplication {
    private TWindow mainWindow;
    private Map<ViewType, View> views = new HashMap<>();

    public RestaurantApplication() throws Exception {
        super(BackendType.SWING);

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
    }

    private void initializeViews() {
        for (ViewType viewType : ViewType.values()) {
            try {
                View view = viewType.getViewClass()
                    .getDeclaredConstructor(RestaurantApplication.class)
                    .newInstance(this);
                views.put(viewType, view);  // Store with ViewType enum as key
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize view: " + viewType, e);
            }
        }
    }

    public void showView(ViewType viewType) {
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
