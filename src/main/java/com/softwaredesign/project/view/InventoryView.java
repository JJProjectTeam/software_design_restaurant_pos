package com.softwaredesign.project.view;

import jexer.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class InventoryView extends GamePlayView {
    private final RestaurantApplication app;
    private TTableWidget inventoryTable;
    private static final String[] COLUMN_HEADERS = {"Ingredient", "Stock", "Price"};
    private static final int[] COLUMN_WIDTHS = {20, 10, 10};
    private static final int TABLE_Y = 3;
    private static final int TABLE_HEIGHT = 15;
    private Queue<InventoryUpdate> pendingUpdates;
    private RestaurantViewMediator mediator;
    private boolean isInitialized;
    
    private static class InventoryUpdate {
        final String ingredient;
        final double price;
        final int quantity;
        
        InventoryUpdate(String ingredient, double price, int quantity) {
            this.ingredient = ingredient;
            this.price = price;
            this.quantity = quantity;
        }
    }
    
    public InventoryView(RestaurantApplication app) {
        super(app);
        this.app = app;
        this.pendingUpdates = new LinkedList<>();
        this.mediator = RestaurantViewMediator.getInstance();
        this.isInitialized = false;
        RestaurantViewMediator.getInstance().registerView(ViewType.INVENTORY, this);
    }

    @Override
    public void setupView() {
        super.setupView();
        System.out.println("[InventoryView] Setup view called");
    }

    @Override
    protected void addViewContent() {
        System.out.println("[InventoryView] Adding view content");
        window.addLabel("Inventory Status", 2, 2);
        inventoryTable = window.addTable(2, TABLE_Y, window.getWidth() - 4, TABLE_HEIGHT, 3, 10);
        
        // Set column labels and widths
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            inventoryTable.setColumnLabel(i, COLUMN_HEADERS[i]);
            inventoryTable.setColumnWidth(i, COLUMN_WIDTHS[i]);
        }
        
        // Now that the view is fully initialized
        isInitialized = true;
        
        // Process any pending updates
        while (!pendingUpdates.isEmpty()) {
            InventoryUpdate update = pendingUpdates.poll();
            updateIngredient(update.ingredient, update.price, update.quantity);
        }
        
        // Register with mediator when view is set up
        mediator.registerView(ViewType.INVENTORY, this);
    }

    public void onInventoryUpdate(Set<String> ingredients, Map<String, Integer> stockLevels, Map<String, Double> prices) {
        if (inventoryTable == null) {
            System.err.println("[InventoryView] Table widget not initialized");
            return;
        }

        try {
            // Clear existing rows
            while (inventoryTable.getRowCount() > 0) {
                inventoryTable.deleteRow(0);
            }

            // Add new rows for each ingredient
            List<String> sortedIngredients = new ArrayList<>(ingredients);
            Collections.sort(sortedIngredients);

            for (String ingredient : sortedIngredients) {
                inventoryTable.insertRowBelow(inventoryTable.getRowCount()-1);
                int row = inventoryTable.getRowCount() - 1;

                // Update cells
                inventoryTable.setCellText(0, row, ingredient);
                inventoryTable.setCellText(1, row, String.valueOf(stockLevels.get(ingredient)));
                inventoryTable.setCellText(2, row, String.format("$%.2f", prices.get(ingredient)));
            }

            System.out.println("[InventoryView] Updated " + ingredients.size() + " ingredients");
        } catch (Exception e) {
            System.err.println("[InventoryView] Error updating inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateIngredient(String ingredient, double price, int quantity) {
        if (inventoryTable == null || window == null) {
            System.out.println("[InventoryView] ERROR: inventoryTable or window is null!");
            return;
        }
        
        try {
            // Find if row exists for this ingredient
            int rowIndex = -1;
            for (int i = 0; i < inventoryTable.getRowCount(); i++) {
                if (inventoryTable.getCellText(0, i).equals(ingredient)) {
                    rowIndex = i;
                    break;
                }
            }
            
            // If ingredient not found, add new row
            if (rowIndex == -1) {
                System.out.println("[InventoryView] Creating new row for ingredient " + ingredient);
                rowIndex = inventoryTable.getRowCount();
                inventoryTable.insertRowBelow(rowIndex);
            }

            inventoryTable.setCellText(0, rowIndex, ingredient);
            inventoryTable.setCellText(1, rowIndex, String.format("%.2f", price));
            inventoryTable.setCellText(2, rowIndex, Integer.toString(quantity));
            
            System.out.println("[InventoryView] Successfully updated ingredient " + ingredient + " in the view");
        } catch (Exception e) {
            System.out.println("[InventoryView] ERROR updating ingredient " + ingredient + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(TWindow window) {
        System.out.println("[InventoryView] Initializing view");
        super.initialize(window);
    }

    public void cleanup() {
        System.out.println("[InventoryView] Cleaning up view, unregistering from mediator");
        mediator.unregisterView("Inventory", this);
    }
}
