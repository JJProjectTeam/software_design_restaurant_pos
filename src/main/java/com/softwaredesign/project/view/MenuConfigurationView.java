package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;

public class MenuConfigurationView extends ConfigurationView {
    // UI Components
    private Map<String, List<String>> availableRecipes; // For display only
    private Map<String, TCheckBox> recipeCheckboxes;
    private Set<String> selectedRecipes; // Just store the names of selected recipes

    public MenuConfigurationView(RestaurantApplication app) {
        super(app);
        // Initialize with default recipes
        this.availableRecipes = new HashMap<>();
        // Add some default recipes
        List<String> burgerIngredients = Arrays.asList("Beef Patty", "Bun", "Lettuce", "Tomato", "Cheese");
        List<String> kebabIngredients = Arrays.asList("Lamb", "Pita Bread", "Onion", "Tomato", "Tzatziki");
        this.availableRecipes.put("Burger", burgerIngredients);
        this.availableRecipes.put("Kebab", kebabIngredients);
        
        this.recipeCheckboxes = new HashMap<>();
        this.selectedRecipes = new HashSet<>();
    }

    @Override
    protected void setupSpecificElements() {
        try {
            // Title
            window.addLabel("Menu Configuration", 2, 2);
            
            // Create recipe selection area
            //TODO this probably needs to be SCROLLABLE for when there are a lot of recipes
            createRecipeSelectionArea();
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error setting up elements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createRecipeSelectionArea() {
        try {
            window.addLabel("Available Recipes:", 2, 4);
            window.addLabel("(Select the items you want to include in your menu)", 2, 5);
            
            // Create checkboxes directly in the main window
            int y = 7;
            for (Map.Entry<String, List<String>> recipe : availableRecipes.entrySet()) {
                final String recipeName = recipe.getKey();
                List<String> ingredients = recipe.getValue();
                
                // Create a regular checkbox
                final TCheckBox checkbox = window.addCheckBox(2, y, recipeName, false);
                
                // Add a label for ingredients
                window.addLabel("Ingredients: " + formatIngredientsList(ingredients), 30, y);
                
                // Store the checkbox for later reference
                recipeCheckboxes.put(recipeName, checkbox);
                
                y += 2;
            }
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error creating recipe selection area: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void syncCheckboxSelections() {
        try {
            selectedRecipes.clear();
            for (Map.Entry<String, TCheckBox> entry : recipeCheckboxes.entrySet()) {
                String recipeName = entry.getKey();
                TCheckBox checkbox = entry.getValue();
                
                if (checkbox.isChecked()) {
                    selectedRecipes.add(recipeName);
                }
            }
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error syncing checkbox selections: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatIngredientsList(List<String> ingredients) {
        return String.join(", ", ingredients);
    }

    // Getters/setters for controller access
    public Set<String> getSelectedRecipes() {
        syncCheckboxSelections(); // Ensure we have the latest selections
        return new HashSet<>(selectedRecipes);
    }

    public void setSelectedRecipes(Set<String> recipes) {
        try {
            // Update the internal set
            selectedRecipes.clear();
            selectedRecipes.addAll(recipes);
            
            // Update checkboxes to match
            for (Map.Entry<String, TCheckBox> entry : recipeCheckboxes.entrySet()) {
                String recipeName = entry.getKey();
                TCheckBox checkbox = entry.getValue();
                checkbox.setChecked(recipes.contains(recipeName));
            }
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error setting selected recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected boolean validateConfiguration() {
        try {
            // Sync checkbox selections before validation
            syncCheckboxSelections();
            
            if (selectedRecipes.isEmpty()) {
                showError("At least one menu item must be selected");
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error validating configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onNextPressed() {
        try {
            // Ensure selections are synced before proceeding
            syncCheckboxSelections();
            app.showView(ViewType.DINING_ROOM);
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error navigating to next view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onBackPressed() {
        try {
            // Ensure selections are synced before going back
            syncCheckboxSelections();
            app.showView(ViewType.DINING_CONFIGURATION);
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error navigating to previous view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}