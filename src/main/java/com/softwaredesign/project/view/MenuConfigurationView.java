package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;
import jexer.TScrollableWindow;

public class MenuConfigurationView extends ConfigurationView {
    private TTableWidget menuTable;
    private Map<String, List<String>> availableRecipes; // For display only
    private Map<String, TCheckBox> recipeCheckboxes;
    private Set<String> selectedRecipes; // Just store the names of selected recipes

    public MenuConfigurationView(RestaurantApplication app, Map<String, List<String>> recipes) {
        super(app);
        this.availableRecipes = recipes;
        this.recipeCheckboxes = new HashMap<>();
        this.selectedRecipes = new HashSet<>();
    }

    @Override
    protected void setupSpecificElements() {
        // Selected menu items table
        createMenuTable();
        
        // Scrollable recipe selection area
        createRecipeSelectionArea();

        showWarning("Please select at least one menu item");
    }

    private void createMenuTable() {
        window.addLabel("Selected Menu Items:", 2, 2);
        menuTable = window.addTable(2, 4, 130, 8, 2, 1);
        menuTable.setColumnLabel(0, "Item Name");
        menuTable.setColumnLabel(1, "Ingredients");
        menuTable.setColumnWidth(0, 30);
        menuTable.setColumnWidth(1, 100);
    }

    private void createRecipeSelectionArea() {
        window.addLabel("Available Recipes:", 2, 13);
        
        // Create a scrollable window
        final TScrollableWindow scrollWindow = new TScrollableWindow(window.getApplication(),
                "Recipe Selection", 2, 15, 130, 15);
        
        // Create a panel to hold the checkboxes
        TPanel panel = scrollWindow.addPanel(0, 0, 128, availableRecipes.size() * 2 + 1);
        
        int row = 0;
        for (Map.Entry<String, List<String>> recipe : availableRecipes.entrySet()) {
            final String recipeName = recipe.getKey();
            List<String> ingredients = recipe.getValue();
            
            // Create a regular checkbox
            final TCheckBox checkbox = panel.addCheckBox(2, row, recipeName, false);
            
            // Add a label for ingredients
            panel.addLabel(formatIngredientsList(ingredients), 40, row);
            
            // Store the checkbox for later reference
            recipeCheckboxes.put(recipeName, checkbox);
            
            row += 2;
        }
        
        // Set up scrollbar values
        scrollWindow.setVerticalValue(0);
        scrollWindow.setTopValue(0);
        scrollWindow.setBottomValue(Math.max(0, row - 10));
    }

    private void syncCheckboxSelections() {
        selectedRecipes.clear();
        for (Map.Entry<String, TCheckBox> entry : recipeCheckboxes.entrySet()) {
            String recipeName = entry.getKey();
            TCheckBox checkbox = entry.getValue();
            
            if (checkbox.isChecked()) {
                selectedRecipes.add(recipeName);
            }
        }
        refreshMenuTable();
    }

    private void refreshMenuTable() {
        // Clear existing table
        while (menuTable.getRowCount() > 1) {
            menuTable.deleteRow(1);
        }

        // Repopulate from selected items
        for (String recipeName : selectedRecipes) {
            addMenuItemToTable(recipeName, availableRecipes.get(recipeName));
        }
    }

    private void addMenuItemToTable(String name, List<String> ingredients) {
        int row = menuTable.getRowCount()-1;
        menuTable.insertRowBelow(row);
        menuTable.setCellText(0, row, name);
        menuTable.setCellText(1, row, formatIngredientsList(ingredients));
    }

    private String formatIngredientsList(List<String> ingredients) {
        return String.join(", ", ingredients);
    }

    // Simplified getters/setters for controller access
    public Set<String> getSelectedRecipes() {
        return new HashSet<>(selectedRecipes);
    }

    public void setSelectedRecipes(Set<String> recipes) {
        // Update the internal set
        selectedRecipes.clear();
        selectedRecipes.addAll(recipes);
        
        // Update checkboxes to match
        for (Map.Entry<String, TCheckBox> entry : recipeCheckboxes.entrySet()) {
            String recipeName = entry.getKey();
            TCheckBox checkbox = entry.getValue();
            checkbox.setChecked(recipes.contains(recipeName));
        }
        
        // Refresh the menu table
        refreshMenuTable();
    }

    @Override
    protected boolean validateConfiguration() {
        // Sync checkbox selections before validation
        syncCheckboxSelections();
        
        if (selectedRecipes.isEmpty()) {
            showError("At least one menu item must be selected");
            return false;
        }
        return true;
    }

    @Override
    protected void onNextPressed() {
        // Ensure selections are synced before proceeding
        syncCheckboxSelections();
        app.showView(ViewType.DINING_ROOM);
    }

    @Override
    protected void onBackPressed() {
        // Ensure selections are synced before going back
        syncCheckboxSelections();
        app.showView(ViewType.DINING_CONFIGURATION);
    }
}