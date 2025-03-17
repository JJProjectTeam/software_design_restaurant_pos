package com.softwaredesign.project.view;

import jexer.*;
import java.util.Map;
import com.softwaredesign.project.mediator.RestaurantViewMediator;

public class EndOfGameView implements View {
    private final RestaurantApplication app;
    private TWindow window;
    private TTableWidget statsTable;
    private TLabel profitLabel;

    public EndOfGameView(RestaurantApplication app) {
        if (app == null) {
            throw new IllegalArgumentException("RestaurantApplication cannot be null");
        }
        this.app = app;
        // Register this view with the mediator so that the EndOfGameController can retrieve it
        RestaurantViewMediator.getInstance().registerView(ViewType.END_OF_GAME, this);
    }

    @Override
    public void initialize(TWindow window) {
        this.window = window;
        setupView();
    }

    @Override
    public void cleanup() {
        window.close();
    }

    @Override
    public TWindow getWindow() {
        return window;
    }

    @Override
    public void setupView() {
        // Title at the top
        window.addLabel("=== End of Game Statistics ===", 2, 2);

        // Create table for metrics
        statsTable = window.addTable(3, 5, 100, 10, 2, 1);
        statsTable.setColumnLabel(0, "Metric");
        statsTable.setColumnLabel(1, "Value");
        statsTable.setColumnWidth(0, 25);
        statsTable.setColumnWidth(1, 15);

        // Profit label at the bottom
        profitLabel = window.addLabel("Total Profit: $0", 3, 17);

        // Add restart button
        TButton restartButton = window.addButton("Play Again", 3, 19, new TAction() {
            @Override
            public void DO() {
                // Get the driver from the app and call restartGame directly
                app.restartApplication();
            }
        });
    }

    /**
     * Updates the statistics table with the provided metrics
     * @param metrics Map of metric names to their values
     */
    public void updateStats(Map<String, String> metrics) {

        
        // Update profit label if 'totalRevenue' exists
        if (metrics.containsKey("totalRevenue")) {
            try {
                double totalRevenue = Double.parseDouble(metrics.get("totalRevenue"));
                profitLabel.setLabel(String.format("Total Profit: $%.2f", totalRevenue));
            } catch (NumberFormatException e) {
                profitLabel.setLabel("Total Profit: " + metrics.get("totalRevenue"));
            }
        } else {
            profitLabel.setLabel("Total Profit: $0.00");
        }
        
        int counter = 0;
        // For each stat (except totalRevenue), add a row using a safe method for row insertion
        for (Map.Entry<String, String> entry : metrics.entrySet()) {
            if (entry.getKey().equals("totalRevenue")) continue;
            // If table is empty, insert a row at index 0
            if (statsTable.getRowCount() == 0) {
                statsTable.insertRowBelow(0);
            } else {
                // Otherwise, ensure that there is a row available at index 'counter'
                while (statsTable.getRowCount() <= counter) {
                    // Use getRowCount() if > 0, else default to 0
                    int index = statsTable.getRowCount() > 0 ? statsTable.getRowCount()-1 : 0;
                    statsTable.insertRowBelow(index);
                }
            }
            // Use KitchenView ordering: first argument column, second row
            statsTable.setCellText(0, counter, entry.getKey());
            try {
                double value = Double.parseDouble(entry.getValue());
                statsTable.setCellText(1, counter, String.format("%.2f", value));
            } catch (NumberFormatException e) {
                statsTable.setCellText(1, counter, entry.getValue());
            }
            counter++;
        }
    }

}
