package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;

public class DiningConfigurationView extends ConfigurationView {
    private TTableWidget waiterTable;
    private TField nameField;
    private TComboBox speedCombo;
    private TSpinner tableCountSpinner;
    private TLabel tableCountLabel;
    private int maxTables = 20;
    private int currentTableCount = 0;

    // Local storage for waiter data
    private Map<String, WaiterData> waiters = new HashMap<>();

    // Inner class to hold waiter data
    public static class WaiterData {
        String name;
        int speed;
        double costPerHour;
        List<Integer> assignedTables;

        WaiterData(String name, int speed, double costPerHour) {
            this.name = name;
            this.speed = speed;
            this.costPerHour = costPerHour;
            this.assignedTables = new ArrayList<>();
        }
        public String getName() {
            return name;
        }
        public int getSpeed() {
            return speed;
        }
        public double getCostPerHour() {
            return costPerHour;
        }
        
    }

    public DiningConfigurationView(RestaurantApplication app) {
        super(app);
    }

    // Getters for external access
    public Map<String, WaiterData> getWaiters() {
        return waiters;
    }

    public int getNumberOfTables() {
        return currentTableCount;
    }

    @Override
    protected void setupSpecificElements() {
        // Waiter table
        createWaiterTable();
        
        // Table configuration
        createTableConfiguration();
        
        // Waiter input form
        createWaiterInputForm();
    }

    private void createWaiterTable() {
        waiterTable = window.addTable(2, 4, 100, 8, 4, 1);
        
        // Set column labels
        waiterTable.setColumnLabel(0, "Waiter Name");
        waiterTable.setColumnLabel(1, "Speed");
        waiterTable.setColumnLabel(2, "Cost/Hour");
        waiterTable.setColumnLabel(3, "Tables Assigned");

        // Set column widths
        waiterTable.setColumnWidth(0, 25);
        waiterTable.setColumnWidth(1, 15);
        waiterTable.setColumnWidth(2, 20);
        waiterTable.setColumnWidth(3, 20);

        // Populate from local storage
        for (var entry : waiters.entrySet()) {
            var waiter = entry.getValue();
            addWaiterToTable(waiter.name, waiter.speed, waiter.costPerHour);
        }
    }

    private void createTableConfiguration() {
        window.addLabel("Number of Tables:", 2, 13);
        tableCountLabel = window.addLabel("0", 25, 13);

        TAction increaseTableCountAction = new TAction() {
            public void DO() {
                if (currentTableCount < maxTables) {
                    currentTableCount++;
                    updateTableCountLabel();
                }
            }
        };
        
        TAction decreaseTableCountAction = new TAction() {
            public void DO() {
                if (currentTableCount > 0) {
                    currentTableCount--;
                    updateTableCountLabel();
                }
            }
        };
        tableCountSpinner = window.addSpinner(35, 13, increaseTableCountAction, decreaseTableCountAction);
        
        window.addLabel("(Maximum " + maxTables + " tables)", 45, 13);
    }

    private void updateTableCountLabel() {
        tableCountLabel.setLabel(String.valueOf(currentTableCount));
    }

    private void createWaiterInputForm() {
        List<String> speeds = new ArrayList<>();
        speeds.add("1");
        speeds.add("2");
        speeds.add("3");

        window.addLabel("Add New Waiter:", 2, 15);
        nameField = window.addField(20, 15, 20, false);
        nameField.setText("");
        
        window.addLabel("Speed:", 45, 15);
        speedCombo = window.addComboBox(55, 15, 10, speeds, -1, 3, nullAction);
        
        window.addButton("Add Waiter", 70, 15, new TAction() {
            public void DO() {
                addWaiter();
            }
        });
    }

    private void addWaiter() {
        String name = nameField.getText();
        if (name.trim().isEmpty()) {
            showError("Please enter a waiter name");
            return;
        }

        int speed = Integer.parseInt(speedCombo.getText());
        double costPerHour = calculateCost(speed);

        // Add to local storage
        waiters.put(name, new WaiterData(name, speed, costPerHour));
        
        addWaiterToTable(name, speed, costPerHour);

        // Clear inputs
        nameField.setText("");
        speedCombo.setIndex(-1);
    }

    private void addWaiterToTable(String name, int speed, double costPerHour) {
        int row = waiterTable.getRowCount();
        waiterTable.insertRowBelow(row-1);
        waiterTable.setCellText(0, row, name);
        waiterTable.setCellText(1, row, String.valueOf(speed));
        waiterTable.setCellText(2, row, String.format("%.2f", costPerHour));
        waiterTable.setCellText(3, row, "0"); // Initially no tables assigned
    }

    private double calculateCost(int speed) {
        return 15.0 + (speed - 1) * 5.0;
    }

    @Override
    protected boolean validateConfiguration() {
        if (waiters.isEmpty()) {
            showError("At least one waiter must be added");
            return false;
        }
        if (currentTableCount == 0) {
            showError("At least one table must be added");
            return false;
        }
        return true;
    }

    @Override
    protected void onNextPressed() {
        app.showView(ViewType.MENU_CONFIGURATION);
    }

    @Override
    protected void onBackPressed() {
        app.showView(ViewType.CHEF_CONFIGURATION);
    }
}
