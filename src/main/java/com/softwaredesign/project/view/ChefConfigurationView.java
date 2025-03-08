package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;

public class ChefConfigurationView extends ConfigurationView {
    // Local storage for chef data
    private Map<String, ChefData> chefs = new HashMap<>();
    
    // Inner class to hold chef data
    public static class ChefData {
        String name;
        List<String> stations;
        int speed;
        double costPerHour;
        String strategy;

        ChefData(String name, List<String> stations, int speed, double costPerHour, String strategy) {
            this.name = name;
            this.stations = new ArrayList<>(stations);
            this.speed = speed;
            this.costPerHour = costPerHour;
            this.strategy = strategy;
        }

        public String getName() { return name; }
        public List<String> getStations() { return stations; }
        public int getSpeed() { return speed; }
        public double getCostPerHour() { return costPerHour; }
        public String getStrategy() { return strategy; }
    }

    // UI Components
    private TTableWidget chefTable;
    private TField nameField;
    private TComboBox speedCombo;
    private TComboBox strategyCombo;
    private TCheckBox grillCheckbox;
    private TCheckBox prepCheckbox;
    private TCheckBox plateCheckbox;

    // Getters for external access
    public Map<String, ChefData> getChefs() {
        return chefs;
    }

    public ChefConfigurationView(RestaurantApplication app) {
        super(app);
        
        System.out.println("[ChefConfigurationView] Constructor called");
        
        // Initialize with a default chef to ensure there's always at least one
        List<String> defaultStations = Arrays.asList("Grill", "Prep", "Plate");
        chefs.put("Default Chef", new ChefData("Default Chef", defaultStations, 2, 200.0, "FIFO"));
        
        System.out.println("[ChefConfigurationView] Constructor completed");
    }

    @Override
    protected void setupSpecificElements() {
        try {
            System.out.println("[ChefConfigurationView] setupSpecificElements started");
            
            // Title
            System.out.println("[ChefConfigurationView] Adding title label");
            window.addLabel("Chef Configuration", 2, 2);
            
            // Create chef table
            System.out.println("[ChefConfigurationView] Creating chef table");
            createChefTable();
            
            // Create input form
            System.out.println("[ChefConfigurationView] Creating input form");
            createInputForm();
            
            // Add warning - with a small delay to ensure UI is ready
            System.out.println("[ChefConfigurationView] Adding warning");
            try {
                // Make sure warning label is initialized before using it
            //     if (warningLabel != null) {
            //         showWarning("At least one chef must be assigned to each station type!");
            //     } else {
            //         System.err.println("[ChefConfigurationView] Warning label is null, cannot show warning");
            //     }
            } catch (Exception e) {
                System.err.println("[ChefConfigurationView] Error showing warning: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("[ChefConfigurationView] setupSpecificElements completed");
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error setting up elements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createChefTable() {
        try {
            System.out.println("[ChefConfigurationView] createChefTable started");
            
            window.addLabel("Current Chefs:", 2, 4);
            System.out.println("[ChefConfigurationView] Adding table widget");
            chefTable = window.addTable(2, 6, 130, 8, 5, 1);
            
            // Set column labels
            System.out.println("[ChefConfigurationView] Setting column labels");
            chefTable.setColumnLabel(0, "Chef Name");
            chefTable.setColumnLabel(1, "Stations");
            chefTable.setColumnLabel(2, "Speed");
            chefTable.setColumnLabel(3, "Cost/Hour");
            chefTable.setColumnLabel(4, "Strategy");

            // Set column widths
            System.out.println("[ChefConfigurationView] Setting column widths");
            chefTable.setColumnWidth(0, 20);
            chefTable.setColumnWidth(1, 40);
            chefTable.setColumnWidth(2, 10);
            chefTable.setColumnWidth(3, 15);
            chefTable.setColumnWidth(4, 20);

            // Populate from local storage
            System.out.println("[ChefConfigurationView] Refreshing chef table");
            refreshChefTable();
            
            System.out.println("[ChefConfigurationView] createChefTable completed");
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error creating chef table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshChefTable() {
        try {
            System.out.println("[ChefConfigurationView] refreshChefTable started");
            
            // Clear existing table
            System.out.println("[ChefConfigurationView] Clearing existing table rows");
            while (chefTable.getRowCount() > 1) {
                chefTable.deleteRow(1);
            }

            // Repopulate from local storage
            System.out.println("[ChefConfigurationView] Repopulating table from storage, chef count: " + chefs.size());
            for (var entry : chefs.entrySet()) {
                System.out.println("[ChefConfigurationView] Adding chef to table: " + entry.getKey());
                var chef = entry.getValue();
                String stations = String.join(", ", chef.stations);
                addChefToTable(chef.name, stations, chef.speed, chef.costPerHour, chef.strategy);
            }
            
            System.out.println("[ChefConfigurationView] refreshChefTable completed");
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error refreshing chef table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createInputForm() {
        try {
            System.out.println("[ChefConfigurationView] createInputForm started");
            
            System.out.println("[ChefConfigurationView] Adding 'Add New Chef' label");
            window.addLabel("Add New Chef:", 2, 15);
            
            // Name field
            System.out.println("[ChefConfigurationView] Adding name field");
            window.addLabel("Name:", 2, 17);
            nameField = window.addField(8, 17, 15, false);
            
            // Create a local nullAction instead of using the parent's
            System.out.println("[ChefConfigurationView] Creating local nullAction");
            TAction localNullAction = new TAction() {
                public void DO() {
                    System.out.println("[ChefConfigurationView] localNullAction DO method called");
                    // Do nothing
                }
            };
            
            // Speed selection
            System.out.println("[ChefConfigurationView] Adding speed selection");
            window.addLabel("Speed:", 30, 17);
            List<String> speeds = new ArrayList<>();
            speeds.add("1");
            speeds.add("2");
            speeds.add("3");
            System.out.println("[ChefConfigurationView] Creating speed combo box");
            speedCombo = window.addComboBox(36, 17, 8, speeds, 0, 4, localNullAction);
            
            // Strategy selection
            System.out.println("[ChefConfigurationView] Adding strategy selection");
            window.addLabel("Strategy:", 50, 17);
            List<String> strategies = new ArrayList<>();
            strategies.add("FIFO");
            strategies.add("LIFO");
            System.out.println("[ChefConfigurationView] Creating strategy combo box");
            strategyCombo = window.addComboBox(58, 17, 15, strategies, 0, 4, localNullAction);
            
            // Station checkboxes
            System.out.println("[ChefConfigurationView] Adding station checkboxes");
            window.addLabel("Stations:", 2, 19);
            grillCheckbox = window.addCheckBox(10, 19, "Grill", false);
            prepCheckbox = window.addCheckBox(30, 19, "Prep", false);
            plateCheckbox = window.addCheckBox(50, 19, "Plate", false);
            
            // Add chef button
            System.out.println("[ChefConfigurationView] Adding 'Add Chef' button");
            window.addButton("Add Chef", 80, 19, new TAction() {
                public void DO() {
                    System.out.println("[ChefConfigurationView] Add Chef button pressed");
                    handleAddChef();
                }
            });
            
            System.out.println("[ChefConfigurationView] createInputForm completed");
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error creating input form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAddChef() {
        try {
            System.out.println("[ChefConfigurationView] handleAddChef started");
            
            if (validateInputs()) {
                String name = nameField.getText();
                System.out.println("[ChefConfigurationView] Adding chef with name: " + name);
                
                // Get selected stations
                List<String> selectedStations = new ArrayList<>();
                if (grillCheckbox.isChecked()) selectedStations.add("Grill");
                if (prepCheckbox.isChecked()) selectedStations.add("Prep");
                if (plateCheckbox.isChecked()) selectedStations.add("Plate");
                
                int speed = Integer.parseInt(speedCombo.getText());
                double costPerHour = calculateCost(speed, selectedStations.size());
                String strategy = strategyCombo.getText();
                
                System.out.println("[ChefConfigurationView] Chef details - Stations: " + selectedStations + 
                                   ", Speed: " + speed + ", Cost: " + costPerHour + ", Strategy: " + strategy);
                
                // Add to local storage
                chefs.put(name, new ChefData(name, selectedStations, speed, costPerHour, strategy));
                
                // Add to table for display
                addChefToTable(name, String.join(", ", selectedStations), speed, costPerHour, strategy);
                
                // Clear inputs
                clearInputs();
            }
            
            System.out.println("[ChefConfigurationView] handleAddChef completed");
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error handling add chef: " + e.getMessage());
            e.printStackTrace();
            showError("Error adding chef: " + e.getMessage());
        }
    }

    private void addChefToTable(String name, String stations, int speed, double costPerHour, String strategy) {
        try {
            System.out.println("[ChefConfigurationView] addChefToTable started for chef: " + name);
            
            // Add to table UI
            int row = chefTable.getRowCount()-1;
            System.out.println("[ChefConfigurationView] Inserting row at position: " + row);
            chefTable.insertRowBelow(row);
            chefTable.setCellText(0, row, name);
            chefTable.setCellText(1, row, stations);
            chefTable.setCellText(2, row, String.valueOf(speed));
            chefTable.setCellText(3, row, String.format("%.2f", costPerHour));
            chefTable.setCellText(4, row, strategy);
            
            System.out.println("[ChefConfigurationView] addChefToTable completed");
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error adding chef to table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        try {
            // Validate name
            String name = nameField.getText();
            if (name == null || name.trim().isEmpty()) {
                showError("Name cannot be empty");
                return false;
            }

            // Validate at least one station is selected
            if (!grillCheckbox.isChecked() && !prepCheckbox.isChecked() && !plateCheckbox.isChecked()) {
                showError("Please select at least one station");
                return false;
            }

            // Speed validation
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

            // Strategy validation
            String strategy = strategyCombo.getText();
            if (strategy == null || strategy.isEmpty()) {
                showError("Please select a strategy");
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error validating inputs: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void clearInputs() {
        try {
            nameField.setText("");
            speedCombo.setIndex(0);
            strategyCombo.setIndex(0);
            grillCheckbox.setChecked(false);
            prepCheckbox.setChecked(false);
            plateCheckbox.setChecked(false);
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error clearing inputs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateCost(int speed, int numberOfStations) {
        return speed * numberOfStations * 100.0;
    }

    private boolean validateStationCoverage() {
        try {
            boolean hasGrill = false;
            boolean hasPrep = false;
            boolean hasPlate = false;

            for (ChefData chef : chefs.values()) {
                if (chef.stations.contains("Grill")) hasGrill = true;
                if (chef.stations.contains("Prep")) hasPrep = true;
                if (chef.stations.contains("Plate")) hasPlate = true;
            }

            return hasGrill && hasPrep && hasPlate;
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error validating station coverage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean validateConfiguration() {
        try {
            if (!validateStationCoverage()) {
                showError("At least one chef must be assigned to each station type!");
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error validating configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onNextPressed() {
        try {
            app.showView(ViewType.DINING_CONFIGURATION);
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error navigating to next view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onBackPressed() {
        try {
            app.showView(ViewType.WELCOME);
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error navigating to previous view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
