package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;

public class ChefConfigurationView extends ConfigurationView {
    // Local storage for chef data
    private Map<String, ChefData> chefs = new HashMap<>();
    
    // Station counts
    private int grillStationCount = 1;
    private int prepStationCount = 1;
    private int plateStationCount = 1;
    
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
    private TField removeNameField;
    private TComboBox speedCombo;
    private TComboBox strategyCombo;
    private TCheckBox grillCheckbox;
    private TCheckBox prepCheckbox;
    private TCheckBox plateCheckbox;
    private TLabel grillCountLabel;
    private TLabel prepCountLabel;
    private TLabel plateCountLabel;

    // Getters for external access
    public Map<String, ChefData> getChefs() {
        return chefs;
    }
    
    // Getters for station counts
    public int getGrillStationCount() { return grillStationCount; }
    public int getPrepStationCount() { return prepStationCount; }
    public int getPlateStationCount() { return plateStationCount; }

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
            
            // Create station count configuration
            System.out.println("[ChefConfigurationView] Creating station count configuration");
            createStationCountConfiguration();
            
            // Create input form
            System.out.println("[ChefConfigurationView] Creating input form");
            createInputForm();
            
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
            
            // Add a field and button to remove a chef
            window.addLabel("Remove Chef (enter name):", 2, 14);
            removeNameField = window.addField(30, 14, 20, false);
            window.addButton("Remove", 55, 14, new TAction() {
                public void DO() {
                    System.out.println("[ChefConfigurationView] Remove Chef button pressed");
                    removeChef();
                }
            });
            
            System.out.println("[ChefConfigurationView] createChefTable completed");
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error creating chef table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createStationCountConfiguration() {
        try {
            System.out.println("[ChefConfigurationView] createStationCountConfiguration started");
            
            window.addLabel("Station Counts:", 2, 23);
            
            // Grill station count
            window.addLabel("Grill Stations:", 10, 25);
            grillCountLabel = window.addLabel(String.valueOf(grillStationCount), 25, 25);
            
            // Add buttons to increase/decrease grill count
            window.addButton("-", 30, 25, new TAction() {
                public void DO() {
                    if (grillStationCount > 1) {
                        grillStationCount--;
                        updateStationCountLabels();
                    } else {
                        showError("Must have at least one Grill station");
                    }
                }
            });
            
            window.addButton("+", 35, 25, new TAction() {
                public void DO() {
                    if (grillStationCount < 5) {
                        grillStationCount++;
                        updateStationCountLabels();
                    } else {
                        showError("Maximum 5 Grill stations allowed");
                    }
                }
            });
            
            // Prep station count
            window.addLabel("Prep Stations:", 10, 27);
            prepCountLabel = window.addLabel(String.valueOf(prepStationCount), 25, 27);
            
            // Add buttons to increase/decrease prep count
            window.addButton("-", 30, 27, new TAction() {
                public void DO() {
                    if (prepStationCount > 1) {
                        prepStationCount--;
                        updateStationCountLabels();
                    } else {
                        showError("Must have at least one Prep station");
                    }
                }
            });
            
            window.addButton("+", 35, 27, new TAction() {
                public void DO() {
                    if (prepStationCount < 5) {
                        prepStationCount++;
                        updateStationCountLabels();
                    } else {
                        showError("Maximum 5 Prep stations allowed");
                    }
                }
            });
            
            // Plate station count
            window.addLabel("Plate Stations:", 10, 29);
            plateCountLabel = window.addLabel(String.valueOf(plateStationCount), 25, 29);
            
            // Add buttons to increase/decrease plate count
            window.addButton("-", 30, 29, new TAction() {
                public void DO() {
                    if (plateStationCount > 1) {
                        plateStationCount--;
                        updateStationCountLabels();
                    } else {
                        showError("Must have at least one Plate station");
                    }
                }
            });
            
            window.addButton("+", 35, 29, new TAction() {
                public void DO() {
                    if (plateStationCount < 5) {
                        plateStationCount++;
                        updateStationCountLabels();
                    } else {
                        showError("Maximum 5 Plate stations allowed");
                    }
                }
            });
            
            System.out.println("[ChefConfigurationView] createStationCountConfiguration completed");
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error creating station count configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStationCountLabels() {
        try {
            grillCountLabel.setLabel(String.valueOf(grillStationCount));
            prepCountLabel.setLabel(String.valueOf(prepStationCount));
            plateCountLabel.setLabel(String.valueOf(plateStationCount));
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error updating station count labels: " + e.getMessage());
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
            window.addLabel("Add New Chef:", 2, 16);
            
            // Name field
            System.out.println("[ChefConfigurationView] Adding name field");
            window.addLabel("Name:", 2, 18);
            nameField = window.addField(8, 18, 15, false);
            
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
            window.addLabel("Speed:", 30, 18);
            List<String> speeds = new ArrayList<>();
            speeds.add("1");
            speeds.add("2");
            speeds.add("3");
            System.out.println("[ChefConfigurationView] Creating speed combo box");
            speedCombo = window.addComboBox(36, 18, 8, speeds, 0, 4, localNullAction);
            
            // Strategy selection
            System.out.println("[ChefConfigurationView] Adding strategy selection");
            window.addLabel("Strategy:", 50, 18);
            List<String> strategies = new ArrayList<>();
            strategies.add("FIFO");
            strategies.add("LIFO");
            System.out.println("[ChefConfigurationView] Creating strategy combo box");
            strategyCombo = window.addComboBox(58, 18, 15, strategies, 0, 4, localNullAction);
            
            // Station checkboxes
            System.out.println("[ChefConfigurationView] Adding station checkboxes");
            window.addLabel("Stations:", 2, 20);
            grillCheckbox = window.addCheckBox(10, 20, "Grill", false);
            prepCheckbox = window.addCheckBox(30, 20, "Prep", false);
            plateCheckbox = window.addCheckBox(50, 20, "Plate", false);
            
            // Add chef button
            System.out.println("[ChefConfigurationView] Adding 'Add Chef' button");
            window.addButton("Add Chef", 80, 20, new TAction() {
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
                
                // Check if a chef with this name already exists
                if (chefs.containsKey(name)) {
                    showError("A chef with the name '" + name + "' already exists. Please use a unique name.");
                    return;
                }
                
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

    private void removeChef() {
        try {
            System.out.println("[ChefConfigurationView] removeChef started");
            
            // Get the chef name from the field
            String chefName = removeNameField.getText();
            
            // Check if a name was entered
            if (chefName == null || chefName.trim().isEmpty()) {
                showError("Please enter a chef name to remove");
                return;
            }
            
            // Check if the chef exists
            if (!chefs.containsKey(chefName)) {
                showError("Chef '" + chefName + "' not found");
                return;
            }
            
            // Don't allow removing the last chef if it would break station coverage
            if (chefs.size() <= 1) {
                showError("Cannot remove the last chef. At least one chef is required.");
                return;
            }
            
            // Check if removing this chef would break station coverage
            ChefData chefToRemove = chefs.get(chefName);
            Map<String, ChefData> tempChefs = new HashMap<>(chefs);
            tempChefs.remove(chefName);
            
            boolean hasGrill = false;
            boolean hasPrep = false;
            boolean hasPlate = false;
            
            for (ChefData chef : tempChefs.values()) {
                if (chef.stations.contains("Grill")) hasGrill = true;
                if (chef.stations.contains("Prep")) hasPrep = true;
                if (chef.stations.contains("Plate")) hasPlate = true;
            }
            
            if (!hasGrill || !hasPrep || !hasPlate) {
                showError("Cannot remove this chef as it would leave some stations uncovered. All stations must have at least one chef assigned.");
                return;
            }
            
            // Remove from local storage
            chefs.remove(chefName);
            
            // Refresh the table
            refreshChefTable();
            
            // Clear the remove field
            removeNameField.setText("");
            
            System.out.println("[ChefConfigurationView] Chef removed: " + chefName);
        } catch (Exception e) {
            System.err.println("[ChefConfigurationView] Error removing chef: " + e.getMessage());
            e.printStackTrace();
            showError("Error removing chef: " + e.getMessage());
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
            // Check if there are no chefs
            if (chefs.isEmpty()) {
                showError("You must add at least one chef!");
                return false;
            }
            
            // Check if all station types are covered
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
