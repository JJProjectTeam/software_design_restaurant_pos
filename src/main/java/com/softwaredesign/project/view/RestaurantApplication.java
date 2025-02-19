package com.softwaredesign.project.view;

import jexer.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantApplication extends TApplication {
    private TWindow mainWindow;

    public RestaurantApplication() throws Exception {
        super(BackendType.SWING);
        mainWindow = new TWindow(this, "OOPsies Bistro", 0, 0, getScreen().getWidth(), getScreen().getHeight());
        mainWindow.maximize();
        showView(new WelcomeView(this));
    }

    public void showView(View view) {
        // Create a new list to (hopefully) avoid concurrent modification
        List<TWidget> widgetsToRemove = new ArrayList<>(mainWindow.getChildren());
        
        
        for (TWidget widget : widgetsToRemove) {
            mainWindow.remove(widget);
        }
        
        // Initialize the new view
        view.initialize(mainWindow);
        mainWindow.show();
    }

    public static void main(String[] args) throws Exception {
        RestaurantApplication app = new RestaurantApplication();
        app.run();
    }
}