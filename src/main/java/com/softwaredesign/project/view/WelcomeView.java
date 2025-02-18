package com.softwaredesign.project.view;

import jexer.*;

public class WelcomeView extends GeneralView {
    public WelcomeView() throws Exception {
        super();
    }
    
    @Override
    protected void initializeUI(TWindow window) {
        window.addLabel("Welcome to OOPsies Bistro", 2, 2);
        window.addButton("Configure Game", 2, 4, new TAction() {
            public void DO() {
                try {
                    window.close();  // TODO, i think this may be the wrong way of closing
                    ConfigurationView newView = new ConfigurationView();
                    (new Thread(newView)).start();  // Start the new view in a new thread
                } catch (Exception e) {
                    messageBox("Error", "Failed to open configuration");
                }
            }
        });
    }


    public static void main(String[] args) {
        try {
            WelcomeView app = new WelcomeView();
            (new Thread(app)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
