package com.softwaredesign.project.view;

import java.util.ArrayList;
import java.util.List;

import jexer.*;

public class ConfigurationView extends GeneralView {
    private TTableWidget chefTable;
    private TField nameField;
    private TComboBox speedCombo;
    private TComboBox stationCombo;
    private TComboBox strategyCombo;
    private TLabel warningLabel;
    private TLabel moneyLabel;
    private TLabel errorLabel; // New field for error messages

    public ConfigurationView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        // Title and money display
        window.addLabel("Configuration Settings", 2, 2);
        window.addLabel("$", window.getWidth() - 15, 2);
        moneyLabel = window.addLabel("1000", window.getWidth() - 13, 2);

        // Chef table
        createChefTable();
        
        // Input form
        createInputForm();
        
        // Warning label
        warningLabel = window.addLabel("Warning: At least one chef must be assigned to each station type!", 2, 18);
        
        // Add error label (initially empty)
        errorLabel = window.addLabel("", 2, 19);
        
        // Start game button with validation
        window.addButton("Start Game", 2, 21, new TAction() {
            public void DO() {
                if (validateStationCoverage()) {
                    app.showView(ViewType.DINING_ROOM);
                } else {
                    showError("Cannot start game: Missing chef coverage for some stations!");
                }
            }
        });
    }

    private void createChefTable() {
        chefTable = window.addTable(2, 4, 100, 8, 5, 1);
        
        // Set column labels
        chefTable.setColumnLabel(0, "Chef Name");
        chefTable.setColumnLabel(1, "Stations");
        chefTable.setColumnLabel(2, "Speed");
        chefTable.setColumnLabel(3, "Cost/Hour");
        chefTable.setColumnLabel(4, "Strategy");

        // Set column widths
        chefTable.setColumnWidth(0, 20);
        chefTable.setColumnWidth(1, 30);
        chefTable.setColumnWidth(2, 10);
        chefTable.setColumnWidth(3, 15);
        chefTable.setColumnWidth(4, 20);

        // Initialize with an empty row
        // chefTable.insertRowBelow(0);
    }

    private void createInputForm() {
        TAction somethingChanged = new TAction() {
            public void DO() {
                System.out.println("Add chef button pressed");
            }
        };

        List<String> stations = new ArrayList<String>();
        List<String> speeds = new ArrayList<String>();
        List<String> strategies = new ArrayList<String>();

        stations.add("Grill");
        stations.add("Prep");
        stations.add("Plate");

        speeds.add("1");
        speeds.add("2");
        speeds.add("3");

        strategies.add("LongestQ");
        strategies.add("OldestOrder");
        strategies.add("NewestOrder");

        // Add all labels and fields in one row (y=15)
        window.addLabel("Add New Chef:", 2, 13);
        
        // Name section
        window.addLabel("Name:", 2, 15);
        nameField = window.addField(8, 15, 15, false);
        
        // Speed section
        window.addLabel("Speed:", 25, 15);
        speedCombo = window.addComboBox(31, 15, 8, speeds, -1, 4, somethingChanged);
        
        // Station section
        window.addLabel("Station:", 41, 15);
        stationCombo = window.addComboBox(48, 15, 12, stations, -1, 4, somethingChanged);
        
        // Strategy section
        window.addLabel("Strategy:", 62, 15);
        strategyCombo = window.addComboBox(70, 15, 15, strategies, -1, 4, somethingChanged);
        
        // Add button at the end of the row
        window.addButton("Add Chef", 87, 15, new TAction() {
            public void DO() {
                if (validateInputs()) {
                    String name = nameField.getText();
                    String station = stationCombo.getText();
                    int speed = Integer.parseInt(speedCombo.getText());
                    double costPerHour = 100 * speed;
                    String strategy = strategyCombo.getText();
                    addChefToTable(name, station, speed, costPerHour, strategy);
                    refreshTable();
                    clearError();
                    clearInputs();
                }
            }
        });
    }

    private boolean validateInputs() {
        // Validate name
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showError("Name cannot be empty");
            return false;
        }

        // Validate station selection
        String station = stationCombo.getText();
        if (station == null || station.isEmpty()) {
            showError("Please select a station");
            return false;
        }

        // Validate speed selection
        String speedText = speedCombo.getText();
        if (speedText == null || speedText.isEmpty()) {
            showError("Please select a speed");
            return false;
        }
        try {
            int speed = Integer.parseInt(speedText);
            if (speed < 1 || speed > 3) {
                showError("Invalid speed value");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid speed value");
            return false;
        }

        // Validate strategy selection
        String strategy = strategyCombo.getText();
        if (strategy == null || strategy.isEmpty()) {
            showError("Please select a strategy");
            return false;
        }

        return true;
    }

    private boolean validateStationCoverage() {
        boolean hasGrill = false;
        boolean hasPrep = false;
        boolean hasPlate = false;

        // Check all rows in the table
        for (int i = 0; i < chefTable.getRowCount(); i++) {
            String station = chefTable.getCellText(1, i);
            if (station != null) {
                switch (station.trim()) {
                    case "Grill":
                        hasGrill = true;
                        break;
                    case "Prep":
                        hasPrep = true;
                        break;
                    case "Plate":
                        hasPlate = true;
                        break;
                }
            }
        }

        // Log the current coverage status
        System.out.println("[ConfigurationView] Station coverage - Grill: " + hasGrill 
            + ", Prep: " + hasPrep + ", Plate: " + hasPlate);

        return hasGrill && hasPrep && hasPlate;
    }

    private void showError(String message) {
        errorLabel.setLabel("Error: " + message);
        System.out.println("[ConfigurationView] Error: " + message);
    }

    private void clearError() {
        errorLabel.setLabel("");
    }

    private void clearInputs() {
        nameField.setText("");
        speedCombo.setText("");
        stationCombo.setText("");
        strategyCombo.setText("");
    }

    // Methods to be called by controller later
    public void updateMoney(int amount) {
        moneyLabel.setLabel(String.valueOf(amount));
    }

    public void addChefToTable(String name, String stations, int speed, double costPerHour, String strategy) {
        try {
            // Get current row count and add a new row
            int row = chefTable.getRowCount()-1;
            chefTable.insertRowAbove(row);
            
            // Set the cell values (column, row, value)
            chefTable.setCellText(0, row, name);
            chefTable.setCellText(1, row, stations);
            chefTable.setCellText(2, row, String.valueOf(speed));
            chefTable.setCellText(3,row, String.format("$%.2f", costPerHour));
            chefTable.setCellText(4, row, strategy);
            
            System.out.println("[ConfigurationView] Added chef at row " + row);
            System.out.println("[ConfigurationView] Table now has " + chefTable.getRowCount() + " rows");
            
        } catch (Exception e) {
            System.err.println("[ConfigurationView] Error adding chef to table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        window.draw();
        chefTable.draw();
        System.out.println("[ConfigurationView] Table refreshed");
    }

    public void setWarningVisible(boolean visible) {
        warningLabel.setVisible(visible);
    }

    public void updateBackEnd() {
        // This will be handled by the controller later
        System.out.println("Update back end button pressed");
    }
}
