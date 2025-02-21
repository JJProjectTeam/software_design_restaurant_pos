package com.softwaredesign.project.view;

import jexer.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import java.util.Queue;
import java.util.LinkedList;

public class InventoryView extends GamePlayView {
    private TTableWidget inventoryTable;
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
        this.pendingUpdates = new LinkedList<>();
        this.mediator = RestaurantViewMediator.getInstance();
        this.isInitialized = false;
    }

    @Override
    protected void setupView() {
        super.setupView();
        System.out.println("[InventoryView] Setup view called");
    }

    @Override
    protected void addViewContent() {
        System.out.println("[InventoryView] Adding view content");
        window.addLabel("Inventory Management", 2, 6);
        window.addLabel("Ingredients:", 2, 8);
        createInventoryTable();
        
        // Now that the view is fully initialized
        isInitialized = true;
        
        // Process any pending updates
        while (!pendingUpdates.isEmpty()) {
            InventoryUpdate update = pendingUpdates.poll();
            updateIngredient(update.ingredient, update.price, update.quantity);
        }
        
        // Register with mediator when view is set up
        mediator.registerView("Inventory", this);
    }

    protected void createInventoryTable() {
        System.out.println("[InventoryView] Creating inventory table");
        inventoryTable = window.addTable(2, 10, 100, 8, 3, 10);

        inventoryTable.setColumnLabel(0, "Ingredient");
        inventoryTable.setColumnLabel(1, "Price ($)");
        inventoryTable.setColumnLabel(2, "Quantity");

        // Set column widths
        inventoryTable.setColumnWidth(0, 20); // Ingredient name needs more space
        inventoryTable.setColumnWidth(1, 20);
        inventoryTable.setColumnWidth(2, 20);
        
        System.out.println("[InventoryView] Inventory table created with " + inventoryTable.getColumnCount() + " columns");
    }

    public void onInventoryUpdate(String ingredient, double price, int quantity) {
        System.out.println("[InventoryView] Received inventory update for " + ingredient);
        
        if (!isInitialized) {
            System.out.println("[InventoryView] View not initialized yet, queueing update for " + ingredient);
            pendingUpdates.offer(new InventoryUpdate(ingredient, price, quantity));
            return;
        }
        
        updateIngredient(ingredient, price, quantity);
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
