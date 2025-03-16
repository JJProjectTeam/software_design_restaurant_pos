package com.softwaredesign.project.view;

import jexer.TWindow;

public interface View {
    /**
     * Initialize the view with a window and set up view-specific elements
     */
    void initialize(TWindow window);
    
    /**
     * Clean up resources when view is closed
     */
    void cleanup();
    
    /**
     * Get the window associated with this view
     */
    TWindow getWindow();
    
    /**
     * Set up the view's specific elements. Called by initialize()
     */
    void setupView();


}
