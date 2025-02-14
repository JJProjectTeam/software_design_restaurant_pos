package com.softwaredesign.project.model.customer;

import com.softwaredesign.project.model.placeholders.Recipe;
import com.softwaredesign.project.model.menu.Menu;

public abstract class Customer {
    public abstract Recipe getOrder(Menu menu);
}
