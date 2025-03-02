package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;
import com.softwaredesign.project.controller.ConfigurationController;

public class MenuConfigurationView extends ConfigurationView {
    private TTableWidget menuTable;
    private TField nameField;
    private Map<String, IngredientCounter> ingredientCounters;
    private List<String> availableIngredients;

    public MenuConfigurationView(RestaurantApplication app) {
        super(app);
        this.ingredientCounters = new HashMap<>();
        initializeAvailableIngredients();
    }

    private void initializeAvailableIngredients() {
        // For now, hardcode available ingredients
        availableIngredients = Arrays.asList(
            "Beef Patty",
            "Chicken Breast",
            "Bun",
            "Lettuce",
            "Tomato",
            "Cheese",
            "Onion",
            "Bacon",
            "Mayo",
            "Ketchup"
        );
    }

    @Override
    protected void setupSpecificElements() {
        // Menu items table
        createMenuTable();
        
        // Input form for new menu items
        createInputForm();

        // Show initial warning
        showWarning("At least one menu item must be added before proceeding");
    }

    private void createMenuTable() {
        window.addLabel("Menu Items:", 2, 2);
        menuTable = window.addTable(2, 4, 130, 8, 3, 1);
        menuTable.setColumnLabel(0, "Item Name");
        menuTable.setColumnLabel(1, "Ingredients");
        menuTable.setColumnLabel(2, "Price");
        menuTable.setColumnWidth(0, 20);
        menuTable.setColumnWidth(1, 80);  
        menuTable.setColumnWidth(2, 10);

        // Populate from controller if available
        ConfigurationController controller = (ConfigurationController) mediator.getController("Configuration");
        if (controller != null) {
            for (var entry : controller.getMenuItems().entrySet()) {
                var item = entry.getValue();
                addMenuItemToTable(item.getName(), item.getIngredients(), item.getPrice());
            }
        }
    }

    private void createInputForm() {
        window.addLabel("Add New Menu Item:", 2, 13);
        
        // Name field
        window.addLabel("Name:", 2, 15);
        nameField = window.addField(8, 15, 20, false);

        // Ingredient counters
        int row = 17;
        int col = 2;
        for (String ingredient : availableIngredients) {
            window.addLabel(ingredient + ":", col, row);
            TLabel countLabel = window.addLabel("Amount: 0", col + 15, row);
            
            TAction incrementAction = new TAction() {
                public void DO() {
                    IngredientCounter counter = ingredientCounters.get(ingredient);
                    if (counter != null) {
                        counter.increment();
                    }
                }
            };
            
            TAction decrementAction = new TAction() {
                public void DO() {
                    IngredientCounter counter = ingredientCounters.get(ingredient);
                    if (counter != null) {
                        counter.decrement();
                    }
                }
            };
            
            window.addSpinner(col + 10, row, incrementAction, decrementAction);
            ingredientCounters.put(ingredient, new IngredientCounter(countLabel));
            
            row += 2;
            if (row > 25) {
                row = 17;
                col += 40;
            }
        }
        
        // Add button
        window.addButton("Add Item", 2, row + 2, new TAction() {
            public void DO() {
                addMenuItem();
            }
        });
    }

    private void addMenuItem() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter a menu item name");
            return;
        }

        // Collect ingredients and their amounts
        Map<String, Integer> ingredients = new HashMap<>();
        for (Map.Entry<String, IngredientCounter> entry : ingredientCounters.entrySet()) {
            int count = entry.getValue().getValue();
            if (count > 0) {
                ingredients.put(entry.getKey(), count);
            }
        }

        if (ingredients.isEmpty()) {
            showError("Please add at least one ingredient");
            return;
        }

        // Calculate price based on ingredients
        double price = calculatePrice(ingredients);

        // Add to table and controller
        addMenuItemToTable(name, ingredients, price);

        // Clear inputs
        nameField.setText("");
        for (IngredientCounter counter : ingredientCounters.values()) {
            counter.reset();
        }
    }

    private void addMenuItemToTable(String name, Map<String, Integer> ingredients, double price) {
        // Add to table UI
        int row = menuTable.getRowCount()-1;
        menuTable.insertRowBelow(row);
        menuTable.setCellText(0, row, name);
        menuTable.setCellText(1, row, formatIngredients(ingredients));
        menuTable.setCellText(2, row, String.format("%.2f", price));

        // Add to configuration controller if available
        ConfigurationController controller = (ConfigurationController) mediator.getController("Configuration");
        if (controller != null) {
            controller.addMenuItem(name, ingredients, price);
        }
    }

    private double calculatePrice(Map<String, Integer> ingredients) {
        // For now, assume a fixed price per ingredient
        double price = 0;
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            price += entry.getValue() * 1.0; // Replace with actual price calculation
        }
        return price;
    }

    private String formatIngredients(Map<String, Integer> ingredients) {
        StringBuilder formatted = new StringBuilder();
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            if (formatted.length() > 0) {
                formatted.append(", ");
            }
            formatted.append(entry.getValue()).append("x ").append(entry.getKey());
        }
        return formatted.toString();
    }

    // Method to handle updates from the controller
    @Override
    protected void onConfigurationUpdate(ConfigurationController controller) {
        // Clear existing table
        while (menuTable.getRowCount() > 1) { // Keep header row
            menuTable.deleteRow(1);
        }

        // Repopulate from controller
        for (var entry : controller.getMenuItems().entrySet()) {
            var item = entry.getValue();
            int row = menuTable.getRowCount()-1;
            menuTable.insertRowBelow(row);
            menuTable.setCellText(0, row, item.getName());
            menuTable.setCellText(1, row, formatIngredients(item.getIngredients()));
            menuTable.setCellText(2, row, String.format("%.2f", item.getPrice()));
        }
    }

    @Override
    protected boolean validateConfiguration() {
        int menuItemCount = menuTable.getRowCount() - 1;  // Subtract header row
        if (menuItemCount == 0) {
            showError("At least one menu item must be added");
            return false;
        }
        return true;
    }

    @Override
    protected void onNextPressed() {
        app.showView(ViewType.DINING_ROOM);
    }

    @Override
    protected void onBackPressed() {
        app.showView(ViewType.DINING_CONFIGURATION);
    }

    private class IngredientCounter {
        TLabel countLabel;
        int count;
        
        IngredientCounter(TLabel label) {
            this.countLabel = label;
            this.count = 0;
        }
        
        void increment() {
            if (count < 10) {
                count++;
                updateLabel();
            }
        }
        
        void decrement() {
            if (count > 0) {
                count--;
                updateLabel();
            }
        }
        
        void updateLabel() {
            countLabel.setLabel("Amount: " + String.valueOf(count));
        }
        
        int getValue() {
            return count;
        }
        
        void reset() {
            count = 0;
            updateLabel();
        }
    }
}