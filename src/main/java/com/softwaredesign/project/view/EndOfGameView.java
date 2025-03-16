package com.softwaredesign.project.view;

import jexer.*;
import java.util.Map;
import java.util.HashMap;
import com.softwaredesign.project.model.BankBalanceSingleton;

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
        statsTable = window.addTable(3, 5, 100, 10, 2, 10);
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
        // Clear existing rows
        while (statsTable.getRowCount() > 0) {
            statsTable.deleteRow(0);
        }

        // Add new rows for each metric
        for (Map.Entry<String, String> entry : metrics.entrySet()) {
            // Update profit label if profit metric exists
            if (metrics.containsKey("totalRevenue")) {
                profitLabel.setLabel(String.format("Total Profit: $%.2f", metrics.get("Profit")));
            } else {
                statsTable.insertRowBelow(statsTable.getRowCount());
                int row = statsTable.getRowCount() - 1;
                statsTable.setCellText(row, 0, entry.getKey());
                statsTable.setCellText(row, 1, String.format("%.2f", entry.getValue()));
                }
        }

    }

}
