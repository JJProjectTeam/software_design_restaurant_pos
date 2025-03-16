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
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryView extends GamePlayView {
    private static final Logger logger = LoggerFactory.getLogger(InventoryView.class);
    private final RestaurantApplication app;
    private TTableWidget inventoryTable;
    private Queue<InventoryUpdate> pendingUpdates;
    private Map<String, Integer> ingredientRowMap;  // Track row indices for ingredients
    private int nextRowIndex = 0;
    private static final String[] COLUMN_HEADERS = {"Ingredient", "Stock", "Price"};
    private static final int[] COLUMN_WIDTHS = {20, 10, 10};
    private static final int TABLE_Y = 3;
    private static final int TABLE_HEIGHT = 15;
    private boolean isInitialized;
    private double bankBalance;

    private static class InventoryUpdate {
        final String ingredient;
        final int stock;
        final double price;

        InventoryUpdate(String ingredient, int stock, double price) {
            this.ingredient = ingredient;
            this.stock = stock;
            this.price = price;
        }
    }

    public InventoryView(RestaurantApplication app) {
        super(app);
        this.app = app;
        this.pendingUpdates = new LinkedList<>();
        this.ingredientRowMap = new HashMap<>();
        this.isInitialized = false;
        RestaurantViewMediator.getInstance().registerView(ViewType.INVENTORY, this);
    }

    @Override
    public void setupView() {
        super.setupView();
        logger.info("[InventoryView] Setup view called");
    }

    @Override
    protected void addViewContent() {
        logger.info("[InventoryView] Adding view content");
        window.addLabel("Inventory Status", 2, 2);
        createInventoryTable();
        
        isInitialized = true;
        
        // Process any pending updates
        while (!pendingUpdates.isEmpty()) {
            InventoryUpdate update = pendingUpdates.poll();
            updateIngredientInTable(update.ingredient, update.stock, update.price);
        }
        
    }

    protected void createInventoryTable() {
        logger.info("[InventoryView] Creating inventory table...");
        inventoryTable = window.addTable(2, TABLE_Y, window.getWidth() - 4, TABLE_HEIGHT, COLUMN_HEADERS.length, 1);
        
        // Set column labels and widths
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            inventoryTable.setColumnLabel(i, COLUMN_HEADERS[i]);
            inventoryTable.setColumnWidth(i, COLUMN_WIDTHS[i]);
        }
        
        // Ensure we start with one row
        inventoryTable.insertRowAbove(0);
    }

    public void onIngredientUpdate(String ingredient, int stock, double price) {
        InventoryUpdate update = new InventoryUpdate(ingredient, stock, price);
        if (!isInitialized) {
            logger.info("[InventoryView] View not yet initialized, queueing update for ingredient: " + ingredient);
            pendingUpdates.offer(update);
        } else {
            updateIngredientInTable(ingredient, stock, price);
        }
    }

    private void updateIngredientInTable(String ingredient, int stock, double price) {
        if (inventoryTable == null) {
            logger.error("[InventoryView] Inventory table not initialized");
            return;
        }

        try {
            int rowIndex;
            if (ingredientRowMap.containsKey(ingredient)) {
                rowIndex = ingredientRowMap.get(ingredient);
            } else {
                // Make sure we have at least one row
                if (inventoryTable.getRowCount() == 0) {
                    inventoryTable.insertRowAbove(0);
                }
                
                // Add new row if needed
                while (nextRowIndex >= inventoryTable.getRowCount()) {
                    inventoryTable.insertRowBelow(inventoryTable.getRowCount() - 1);
                    logger.info("[InventoryView] Added row " + (inventoryTable.getRowCount() - 1) + " to table");
                }
                
                rowIndex = nextRowIndex++;
                ingredientRowMap.put(ingredient, rowIndex);
                logger.info("[InventoryView] Created new row " + rowIndex + " for ingredient " + ingredient);
            }

            // Check if the row index is valid before setting cell text
            if (rowIndex >= inventoryTable.getRowCount()) {
                logger.info("[InventoryView] Row index " + rowIndex + " is out of bounds, adding more rows");
                while (rowIndex >= inventoryTable.getRowCount()) {
                    inventoryTable.insertRowBelow(inventoryTable.getRowCount() - 1);
                }
            }

            // Update cells
            inventoryTable.setCellText(0, rowIndex, ingredient);
            inventoryTable.setCellText(1, rowIndex, String.valueOf(stock));
            inventoryTable.setCellText(2, rowIndex, String.format("$%.2f", price));

        } catch (Exception e) {
            logger.error("[InventoryView] Error updating ingredient " + ingredient + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(TWindow window) {
        logger.info("[InventoryView] Initializing view");
        // Reset the row index and map when reinitializing
        this.nextRowIndex = 0;
        this.ingredientRowMap.clear();
        this.isInitialized = false;
        super.initialize(window);
    }

    @Override
    public void cleanup() {
        logger.info("[InventoryView] Cleaning up view");
        if (window != null) {
            window.close();
        }
    }

    @Override
    public void setBankBalance(double newBalance) {
        super.setBankBalance(newBalance);
        logger.info("[InventoryView] Updated bank balance to: $" + String.format("%.2f", bankBalance));
    }
}
