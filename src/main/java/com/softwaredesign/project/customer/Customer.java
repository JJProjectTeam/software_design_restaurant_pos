package com.softwaredesign.project.customer;

import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.placeholders.Recipe;

public abstract class Customer {
    public abstract Recipe selectRecipeFromMenu(Menu menu);

    public abstract void requestRecipeModification(Menu menu);
}
