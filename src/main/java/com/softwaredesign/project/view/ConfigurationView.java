package com.softwaredesign.project.view;

import jexer.*;

public class ConfigurationView extends GeneralView {
    public ConfigurationView() throws Exception {
        super();
    }

    @Override
    protected void initializeUI(TWindow window) {
        window.addLabel("Configuration Settings", 2, 2);
        
        window.addLabel("Select ingredient to configure:", 2, 3);
    }


}
