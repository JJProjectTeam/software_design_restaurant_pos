package com.softwaredesign.project.view;

import jexer.TWindow;

public interface View {
    void initialize(TWindow window);
    TWindow getWindow();
}
