package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;

public class MenuConfigurationView extends ConfigurationView {
    private TTableWidget menuTable;
    private TField nameField;
    private Map<String, IngredientCounter> ingredientCounters;
    private List<String> availableIngredients;

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
        menuTable.setColumnWidth(1, 90);  
        menuTable.setColumnWidth(2, 10);


    }

    private void createInputForm() {
        window.addLabel("Add New Menu Item:", 2, 13);
        
        // Name field
        window.addLabel("Name:", 2, 15);
        nameField = window.addField(8, 15, 20, false);
        
        // Ingredients section
        window.addLabel("Ingredients:", 2, 17);
        
        // Create spinners for each ingredient
        int row = 19;
        int col = 2;
        for (String ingredient : availableIngredients) {
            window.addLabel(ingredient + ":", col, row);
            
            // Create the counter and its label
            TLabel countLabel = window.addLabel("0", col + ingredient.length() + 4, row);
            IngredientCounter counter = new IngredientCounter(countLabel);
            ingredientCounters.put(ingredient, counter);

            // Create decrement and increment buttons with their actions
            window.addButton("-", col + ingredient.length() + 2, row, new TAction() {
                @Override
                public void DO() {
                    counter.decrement();
                }
            });
            
            window.addButton("+", col + ingredient.length() + 6, row, new TAction() {
                @Override
                public void DO() {
                    counter.increment();
                }
            });
            
            // Move to next row or column based on position
            if (col > 60) {
                col = 2;
                row += 2;
            } else {
                col += 30;
            }
        }
        
        // Add button
        TButton addButton = window.addButton("Add Item", 2, row + 3, new TAction() {
            @Override
            public void DO() {
                addMenuItem();
            }
        });
    }

    private void addMenuItem() {
        try {
            String name = nameField.getText().trim();
            
            if (name.isEmpty()) {
                showError("Please enter a name for the menu item");
                return;
            }
            
            // Build ingredients list and count total ingredients
            StringBuilder ingredients = new StringBuilder();
            boolean hasIngredients = false;
            int totalIngredients = 0;
            
            for (Map.Entry<String, IngredientCounter> entry : ingredientCounters.entrySet()) {
                int quantity = entry.getValue().getValue();
                if (quantity > 0) {
                    if (hasIngredients) {
                        ingredients.append(",");  
                    }
                    ingredients.append(quantity).append("x ").append(entry.getKey());
                    hasIngredients = true;
                    totalIngredients += quantity;
                }
            }
            
            if (!hasIngredients) {
                showError("Please select at least one ingredient");
                return;
            }
            
            String ingredientsString = ingredients.toString();
            System.out.println("ingredientsString: " + ingredientsString);
            int row = menuTable.getRowCount()-1;
            System.out.println("row: " + row);
            menuTable.insertRowBelow(row);
            
            // Add to table
            menuTable.setCellText(0, row, name);
            menuTable.setCellText(1, row, ingredientsString);
            System.out.print("Cell text: " + menuTable.getCellText(1, row));
            menuTable.setCellText(2, row, String.valueOf(totalIngredients));
            
            // Clear form
            nameField.setText("");
            for (IngredientCounter counter : ingredientCounters.values()) {
                counter.reset();
            }
            
            clearError();
        } catch (Exception e) {
            showError("An error occurred while adding the menu item");
        }
    }

    @Override
    protected boolean validateConfiguration() {
        // Check if there's at least one menu item
        if (menuTable.getRowCount() <= 1) {  // Account for header row
            showError("Please add at least one menu item before proceeding");
            return false;
        }
        
        clearError();
        return true;
    }

    @Override
    protected void onNextPressed() {
        app.showView(ViewType.CHEF_CONFIGURATION);
    }

    @Override
    protected void onBackPressed() {
        app.showView(ViewType.DINING_ROOM);
    }
}


/*
 * 
 * have validation at least one menu item
 * fix forward and back
 * improve ingredients fetching and add display
 * extract magic parts for controller
 * make a configuraiton object which can be passed back and forward to connfiguraitoncontroller
 */