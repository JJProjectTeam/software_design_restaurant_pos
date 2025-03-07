package com.softwaredesign.project.view;

import jexer.*;

public class EndOfGameView implements View {
    private final RestaurantApplication app;
    private TWindow window;
    private TTableWidget statsTable;

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
        window.addLabel("Game Over", 2, 2);
        statsTable = window.addTable(3, 5, 100, 8, 2, 3);
        
        statsTable.setColumnLabel(0, "");
        statsTable.setColumnLabel(1, "Results");

        statsTable.setCellText(0,0, "No Customers Served");
        statsTable.setCellText(0, 1, "No Meals Delivered");
        statsTable.setCellText(0, 2, "Profit ($)");

        statsTable.setShowRowLabels(false);

        statsTable.setColumnWidth(0, 20);
    }

    public void updateStats(int totalCustomers, int totalOrders, int totalRevenue) {
        while (statsTable.getRowCount() > 0) {
            statsTable.deleteRow(0);
        }

        statsTable.insertRowBelow(0);
        statsTable.insertRowBelow(1);
        statsTable.insertRowBelow(2);

        statsTable.setCellText(0, 0, "Total Customers");
        statsTable.setCellText(1, 0, "Total Orders");
        statsTable.setCellText(2, 0, "Total Revenue");

        statsTable.setCellText(0, 1, String.valueOf(totalCustomers));
        statsTable.setCellText(1, 1, String.valueOf(totalOrders));
        statsTable.setCellText(2, 1, "$" + totalRevenue);
    }
}
