package com.softwaredesign.project.model.customer;

import com.softwaredesign.project.model.menu.Menu;
import com.softwaredesign.project.model.order.Recipe;
public abstract class Customer {
    public abstract Recipe selectRecipeFromMenu(Menu menu);

    public abstract void requestRecipeModification(Menu menu);
}
