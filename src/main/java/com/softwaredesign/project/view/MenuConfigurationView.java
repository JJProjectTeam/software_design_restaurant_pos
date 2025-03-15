package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;

public class MenuConfigurationView extends ConfigurationView {
    // UI Components
    private Map<String, List<String>> availableRecipes; 
    private Map<String, Boolean> selectedRecipeStates; 
    private Set<String> selectedRecipes; // Just store the names of selected recipes
    private TList recipeList; 
    private int minRecipes;
    private int maxRecipes;

    public MenuConfigurationView(RestaurantApplication app) {
        super(app);
        System.out.println("[MenuConfigurationView] Registering with mediator");
        mediator.registerView(ViewType.MENU_CONFIGURATION, this);
        System.out.println("[MenuConfigurationView] Registration complete");
        // Initialize with default recipes
        this.availableRecipes = new HashMap<>();
        this.selectedRecipeStates = new HashMap<>();
        this.selectedRecipes = new HashSet<>();
        
        // Initialize all recipes as unselected
        for (String recipe : availableRecipes.keySet()) {
            selectedRecipeStates.put(recipe, false);
        }
    }
    public void setAvailableRecipes(Map<String, List<String>> recipes) {
        // Initialize all recipes as unselected
        for (String recipe : availableRecipes.keySet()) {
            selectedRecipeStates.put(recipe, false);
        }
    }

    public void setPossibleRecipes(Map<String, List<String>> recipes) {
        System.out.println("[MenuConfigurationView] Setting possible recipes: " + recipes.keySet());
        this.availableRecipes = new HashMap<>(recipes);
        this.selectedRecipeStates = new HashMap<>();
        
        // Initialize selection states for NEW recipes
        for (String recipe : recipes.keySet()) { // Change this line to use recipes instead of availableRecipes
            selectedRecipeStates.put(recipe, false);
        }
        
        // Update the display if already initialized
        if (window != null) {
            System.out.println("[MenuConfigurationView] Window exists, updating display");
            updateListDisplay();
        } else {
            System.out.println("[MenuConfigurationView] Window not yet initialized");
        }
    }

    @Override
    protected void setupSpecificElements() {
        try {
            // Title
            window.addLabel("Menu Configuration", 2, 2);
            
            // Create recipe selection area
            window.addLabel("Available Recipes:", 2, 4);
            window.addLabel("(Select the items you want to include in your menu)", 2, 5);
            
            System.out.println("[MenuConfigurationView] Setting up with " + availableRecipes.size() + " recipes");
            createRecipeSelectionArea();
            
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error setting up elements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createRecipeSelectionArea() {
        try {
            // Create list items with checkbox-like display
            List<String> displayItems = new ArrayList<>();
            for (Map.Entry<String, List<String>> recipe : availableRecipes.entrySet()) {
                String recipeName = recipe.getKey();
                List<String> ingredients = recipe.getValue();
                boolean isSelected = selectedRecipeStates.get(recipeName);
                
                // Format: [X] or [ ] Recipe - Ingredients: list
                String checkboxDisplay = isSelected ? "[X]" : "[ ]";
                displayItems.add(checkboxDisplay + " " + recipeName + " - Ingredients: " + formatIngredientsList(ingredients));
            }
            
            // Create action for when list item is selected
            TAction listAction = new TAction() {
                public void DO() {
                    if (recipeList != null && recipeList.getSelectedIndex() >= 0) {
                        toggleRecipeSelection(recipeList.getSelectedIndex());
                    }
                }
            };
            
            // Add the scrollable list
            recipeList = window.addList(displayItems, 2, 7, window.getWidth() - 4, window.getHeight() - 15, listAction);
            
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error creating recipe selection area: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void toggleRecipeSelection(int index) {
        try {
            // Get the recipe name at this index
            String recipeName = (String) availableRecipes.keySet().toArray()[index];
            
            // Toggle selection state
            boolean currentState = selectedRecipeStates.get(recipeName);
            selectedRecipeStates.put(recipeName, !currentState);
            
            // Update the display
            updateListDisplay();
            
            // Sync with selectedRecipes set
            syncSelections();
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error toggling recipe selection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateListDisplay() {
        try {
            // Remember the current selection
            int currentSelection = recipeList.getSelectedIndex();
            
            // Create a new list of items
            List<String> updatedItems = new ArrayList<>();
            
            for (Map.Entry<String, List<String>> recipe : availableRecipes.entrySet()) {
                String recipeName = recipe.getKey();
                List<String> ingredients = recipe.getValue();
                boolean isSelected = selectedRecipeStates.get(recipeName);
                
                // Format: [X] or [ ] Recipe - Ingredients: list
                String checkboxDisplay = isSelected ? "[X]" : "[ ]";
                updatedItems.add(checkboxDisplay + " " + recipeName + " - Ingredients: " + formatIngredientsList(ingredients));
            }
            
            // Replace the list with a new one
            TAction listAction = new TAction() {
                public void DO() {
                    if (recipeList != null && recipeList.getSelectedIndex() >= 0) {
                        toggleRecipeSelection(recipeList.getSelectedIndex());
                    }
                }
            };
            
            // Remove the old list
            if (recipeList != null) {
                window.remove(recipeList);
            }
            
            // Add the new list
            recipeList = window.addList(updatedItems, 2, 7, window.getWidth() - 4, window.getHeight() - 15, listAction);
            
            // Restore selection if possible
            if (currentSelection >= 0 && currentSelection < updatedItems.size()) {
                recipeList.setSelectedIndex(currentSelection);
            }
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error updating list display: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void syncSelections() {
        try {
            selectedRecipes.clear();
            for (Map.Entry<String, Boolean> entry : selectedRecipeStates.entrySet()) {
                if (entry.getValue()) {
                    selectedRecipes.add(entry.getKey());
                }
            }
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error syncing selections: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatIngredientsList(List<String> ingredients) {
        return String.join(", ", ingredients);
    }

    // Getters/setters for controller access
    public Set<String> getSelectedRecipes() {
        syncSelections(); // Ensure we have the latest selections
        return new HashSet<>(selectedRecipes);
    }

    public void setSelectedRecipes(Set<String> recipes) {
        try {
            // Update the internal set
            selectedRecipes.clear();
            selectedRecipes.addAll(recipes);
            
            // Update selection states
            for (String recipeName : selectedRecipeStates.keySet()) {
                selectedRecipeStates.put(recipeName, recipes.contains(recipeName));
            }
            
            // Update the display
            updateListDisplay();
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error setting selected recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setMinRecipes(int minRecipes) {
        this.minRecipes = minRecipes;
    }

    public void setMaxRecipes(int maxRecipes) {
        this.maxRecipes = maxRecipes;
    }

    @Override
    protected boolean validateConfiguration() {
        try {
            if (selectedRecipes.size() < minRecipes) {
                showError("At least " + minRecipes + " recipe(s) must be selected");
                return false;
            }
            if (selectedRecipes.size() > maxRecipes) {
                showError("Maximum number of recipes (" + maxRecipes + ") exceeded");
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
            syncSelections();
            mediator.notifyConfigurationComplete();
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
            syncSelections();
            app.showView(ViewType.DINING_CONFIGURATION);
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error navigating to previous view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void setupNavigationButtons() {
        System.out.println("[MenuConfigurationView] setupNavigationButtons started");
        try {
            window.addButton("Start Game", window.getWidth() - 15, window.getHeight() - 4, new TAction() {
                public void DO() {
                    System.out.println("[MenuConfigurationView] Start Game button pressed");
                    if (validateConfiguration()) {
                        onNextPressed();
                    }
                }
            });

            window.addButton("Back", 2, window.getHeight() - 4, new TAction() {
                public void DO() {
                    System.out.println("[MenuConfigurationView] Back button pressed");
                    onBackPressed();
                }
            });
            System.out.println("[MenuConfigurationView] setupNavigationButtons completed");
        } catch (Exception e) {
            System.err.println("[MenuConfigurationView] Error in setupNavigationButtons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void setBankBalance(double newBalance) {
        super.setBankBalance(newBalance);
    }
}