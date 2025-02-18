package com.softwaredesign.project.view;

import jexer.*;

public abstract class GeneralView extends TApplication {
    protected TWindow mainWindow;
    protected final int SIDE_BUTTON_WIDTH = 20;
    protected final int SIDE_BUTTON_HEIGHT = 3;
    protected final int SIDE_BUTTON_X = 2;
    
    public GeneralView() throws Exception {
        super(BackendType.SWING);
        setupMainWindow();
    }
    
    private void setupMainWindow() {
        int screenWidth = getScreen().getWidth();
        int screenHeight = getScreen().getHeight();
        mainWindow = new TWindow(this, "OOPsies Bistro", 0, 0, screenWidth, screenHeight);
        
        // TODO this restart button doesnt work!!
        mainWindow.addButton("Restart", screenWidth - 12, 1, new TAction() {
            public void DO() {
                restartToWelcome();
            }
        });
        
        initializeUI(mainWindow);
        mainWindow.maximize();
        mainWindow.show();
    }
    
    protected void restartToWelcome() {
        try {
            mainWindow.close();
            WelcomeView welcome = new WelcomeView();
            (new Thread(welcome)).start();
        } catch (Exception e) {
            messageBox("Error", "Failed to restart: " + e.getMessage());
        }
    }
    
    protected abstract void initializeUI(TWindow window);
}
