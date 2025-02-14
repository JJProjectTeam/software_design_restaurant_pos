package com.softwaredesign.project.model.customer;

import java.time.LocalDateTime;

import com.softwaredesign.project.model.placeholders.Recipe;
import com.softwaredesign.project.model.exceptions.RecipeValidationException;
import com.softwaredesign.project.model.menu.Menu;

public class DineInCustomer extends Customer {
    private boolean isBrowsing;
    private Recipe selectedRecipe;

    public DineInCustomer() {
        this.isBrowsing = true;
    }

    @Override
    public Recipe getOrder(Menu menu) {
        if (isBrowsing) {
            throw new IllegalStateException("Customer is still browsing or hasn't selected a recipe");
        }

        Recipe selectedRecipe = null;
        boolean validOrder = false;

        while (!validOrder) {
            selectedRecipe = menu.getRandomRecipe();
            try {
                // RecipeValidator.getInstance().validateRecipe(selectedRecipe);
                validOrder = true;
            } catch (RecipeValidationException e) {
                // If validation fails, loop will continue and customer will pick again
                System.out.println("Sorry, that item is unavailable. Selecting something else...");
            }
        }

        return selectedRecipe;
    }

    public void selectRecipe(Recipe recipe) {
        this.selectedRecipe = recipe;
    }

    public void finishBrowsing() {
        this.isBrowsing = false;
    }

    public boolean isDoneBrowsing() {
        return !isBrowsing;
    }
}
