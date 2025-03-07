package com.softwaredesign.project.view;

import com.softwaredesign.project.controller.BaseController;

public interface ConfigurableView extends View {
    /**
     * Handle updates from controllers via the mediator pattern
     */
    void onUpdate(BaseController controller);
}
