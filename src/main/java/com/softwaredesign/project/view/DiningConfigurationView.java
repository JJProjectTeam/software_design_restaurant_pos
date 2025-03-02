package com.softwaredesign.project.view;

import java.util.ArrayList;
import java.util.List;
import jexer.*;
import com.softwaredesign.project.controller.ConfigurationController;

public class DiningConfigurationView extends ConfigurationView {
    private TTableWidget waiterTable;
    private TField nameField;
    private TComboBox speedCombo;
    private TSpinner tableCountSpinner;
    private TLabel tableCountLabel;
    private int maxTables = 20;
    private int currentTableCount = 0;

    public DiningConfigurationView(RestaurantApplication app) {
        super(app);
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

        // Populate from controller if available
        ConfigurationController controller = (ConfigurationController) mediator.getController("Configuration");
        if (controller != null) {
            for (var entry : controller.getWaiters().entrySet()) {
                var waiter = entry.getValue();
                addWaiterToTable(waiter.getName(), waiter.getSpeed(), waiter.getCostPerHour());
            }
            currentTableCount = controller.getNumberOfTables();
            updateTableCountLabel();
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
        
        // Update controller if available
        ConfigurationController controller = (ConfigurationController) mediator.getController("Configuration");
        if (controller != null) {
            controller.setNumberOfTables(currentTableCount);
        }
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

        addWaiterToTable(name, speed, costPerHour);

        // Clear inputs
        nameField.setText("");
        speedCombo.setIndex(-1);
    }

    private void addWaiterToTable(String name, int speed, double costPerHour) {
        // Add to table UI
        int row = waiterTable.getRowCount();
        waiterTable.insertRowBelow(row-1);
        waiterTable.setCellText(0, row, name);
        waiterTable.setCellText(1, row, String.valueOf(speed));
        waiterTable.setCellText(2, row, String.valueOf(costPerHour));
        waiterTable.setCellText(3, row, "");

        // Add to configuration controller if available
        ConfigurationController controller = (ConfigurationController) mediator.getController("Configuration");
        if (controller != null) {
            controller.addWaiter(name, speed, costPerHour);
        }
    }

    private double calculateCost(int speed) {
        // Base cost of 15 per hour, increases with speed
        return 15.0 + (speed - 1) * 5.0;
    }

    // Method to handle updates from the controller
    @Override
    protected void onConfigurationUpdate(ConfigurationController controller) {
        // Clear existing table
        while (waiterTable.getRowCount() > 1) { // Keep header row
            waiterTable.deleteRow(1);
        }

        // Repopulate from controller
        for (var entry : controller.getWaiters().entrySet()) {
            var waiter = entry.getValue();
            int row = waiterTable.getRowCount()-1;
            waiterTable.insertRowBelow(row);
            waiterTable.setCellText(0, row, waiter.getName());
            waiterTable.setCellText(1, row, String.valueOf(waiter.getSpeed()));
            waiterTable.setCellText(2, row, String.format("%.2f", waiter.getCostPerHour()));
            waiterTable.setCellText(3, row, String.valueOf(waiter.getAssignedTables().size()));
        }
            
        // Update table count
        tableCountLabel.setLabel(String.valueOf(controller.getNumberOfTables()));
    }

    @Override
    protected boolean validateConfiguration() {
        int waiterCount = waiterTable.getRowCount() - 1;  // Subtract header row
        if (waiterCount == 0) {
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
