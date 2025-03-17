package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChefConfigurationView extends ConfigurationView {
    // Local storage for chef data
    private Map<String, ChefData> chefs = new HashMap<>();
    private Map<String, Integer> stationCounts = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ChefConfigurationView.class);
    
    // Station counts
    private int grillStationCount = 1;
    private int prepStationCount = 1;
    private int plateStationCount = 1;
    
    // Inner class to hold chef data
    public static class ChefData {
        String name;
        List<String> stations;
        int speed;
        double cost;
        String strategy;

        ChefData(String name, List<String> stations, int speed, double cost, String strategy) {
            this.name = name;
            this.stations = new ArrayList<>(stations);
            this.speed = speed;
            this.cost = cost;
            this.strategy = strategy;
        }

        public String getName() { return name; }
        public List<String> getStations() { return stations; }
        public int getSpeed() { return speed; }
        public double getCost() { return cost; }
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

    // Add these instance variables at the top of the class
    private int minChefs;
    private int maxChefs;
    private int maxStationsPerChef;
    private int minStations;
    private int maxStations;
    private int maxInstancesOfStation;
    private int minInstancesOfStation;
    private int maxSpeed = 5; // Default value
    private double standardPay = 15.0; // Default value
    private double payMultiplierBySpeed = 1.0; // Default value
    private double payMultiplierByStation = 1.0; // Default value

    public ChefConfigurationView(RestaurantApplication app) {
        super(app);
        mediator.registerView(ViewType.CHEF_CONFIGURATION, this);
        
        // Initialize with a default chef to ensure there's always at least one
        List<String> defaultStations = Arrays.asList("Grill", "Prep", "Plate");
        chefs.put("Default Chef", new ChefData("Default Chef", defaultStations, 2, 200.0, "FIFO"));
    }

    // Getters for external access
    public Map<String, ChefData> getChefs() {
        return chefs;
    }

    public Map<String, Integer> getStationCounts() {
        logger.info("[ChefConfigurationView] Getting station counts - Grill: " + grillStationCount 
            + ", Prep: " + prepStationCount + ", Plate: " + plateStationCount);
        
        Map<String, Integer> counts = new HashMap<>();
        counts.put("GRILL", grillStationCount);
        counts.put("PREP", prepStationCount);
        counts.put("PLATE", plateStationCount);
        return counts;
    }
    

    @Override
    protected void setupSpecificElements() {
        try {
            
            // Title
            window.addLabel("Chef Configuration", 2, 2);
            
            // Create chef table
            createChefTable();
            
            // Create station count configuration
            createStationCountConfiguration();
            
            // Create input form
            createInputForm();
            
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error setting up elements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createChefTable() {
        try {
            
            window.addLabel("Current Chefs:", 2, 4);
            logger.info("[ChefConfigurationView] Adding table widget");
            chefTable = window.addTable(2, 6, 130, 8, 5, 1);
            
            // Set column labels
            chefTable.setColumnLabel(0, "Chef Name");
            chefTable.setColumnLabel(1, "Stations");
            chefTable.setColumnLabel(2, "Speed");
            chefTable.setColumnLabel(3, "Cost");
            chefTable.setColumnLabel(4, "Strategy");

            // Set column widths
            chefTable.setColumnWidth(0, 20);
            chefTable.setColumnWidth(1, 40);
            chefTable.setColumnWidth(2, 10);
            chefTable.setColumnWidth(3, 15);
            chefTable.setColumnWidth(4, 20);

            // Populate from local storage
            refreshChefTable();
            
            // Add a field and button to remove a chef
            window.addLabel("Remove Chef (enter name):", 2, 14);
            removeNameField = window.addField(30, 14, 20, false);
            window.addButton("Remove", 55, 14, new TAction() {
                public void DO() {
                    logger.info("[ChefConfigurationView] Remove Chef button pressed");
                    removeChef();
                }
            });
            
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error creating chef table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createStationCountConfiguration() {
        try {
            
            window.addLabel("Station Counts:", 2, 23);
            
            // Grill station count
            window.addLabel("Grill Stations:", 10, 25);
            grillCountLabel = window.addLabel(String.valueOf(grillStationCount), 25, 25);
            
            // Add buttons to increase/decrease grill count
            window.addButton("-", 30, 25, new TAction() {
                public void DO() {
                    if (grillStationCount > minInstancesOfStation) {
                        grillStationCount--;
                        updateStationCountLabels();
                    } else {
                        showError("Must have at least " + minInstancesOfStation + " Grill station(s)");
                    }
                }
            });
            
            window.addButton("+", 35, 25, new TAction() {
                public void DO() {
                    if (grillStationCount < maxInstancesOfStation) {
                        grillStationCount++;
                        updateStationCountLabels();
                    } else {
                        showError("Maximum " + maxInstancesOfStation + " Grill stations allowed");
                    }
                }
            });
            
            // Prep station count
            window.addLabel("Prep Stations:", 10, 27);
            prepCountLabel = window.addLabel(String.valueOf(prepStationCount), 25, 27);
            
            // Add buttons to increase/decrease prep count
            window.addButton("-", 30, 27, new TAction() {
                public void DO() {
                    if (prepStationCount > minInstancesOfStation) {
                        prepStationCount--;
                        updateStationCountLabels();
                    } else {
                        showError("Must have at least " + minInstancesOfStation + " Prep station(s)");
                    }
                }
            });
            
            window.addButton("+", 35, 27, new TAction() {
                public void DO() {
                    if (prepStationCount < maxInstancesOfStation) {
                        prepStationCount++;
                        updateStationCountLabels();
                    } else {
                        showError("Maximum " + maxInstancesOfStation + " Prep stations allowed");
                    }
                }
            });
            
            // Plate station count
            window.addLabel("Plate Stations:", 10, 29);
            plateCountLabel = window.addLabel(String.valueOf(plateStationCount), 25, 29);
            
            // Add buttons to increase/decrease plate count
            window.addButton("-", 30, 29, new TAction() {
                public void DO() {
                    if (plateStationCount > minInstancesOfStation) {
                        plateStationCount--;
                        updateStationCountLabels();
                    } else {
                        showError("Must have at least " + minInstancesOfStation + " Plate station(s)");
                    }
                }
            });
            
            window.addButton("+", 35, 29, new TAction() {
                public void DO() {
                    if (plateStationCount < maxInstancesOfStation) {
                        plateStationCount++;
                        updateStationCountLabels();
                    } else {
                        showError("Maximum " + maxInstancesOfStation + " Plate stations allowed");
                    }
                }
            });
            
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error creating station count configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStationCountLabels() {
        try {
            grillCountLabel.setLabel(String.valueOf(grillStationCount));
            prepCountLabel.setLabel(String.valueOf(prepStationCount));
            plateCountLabel.setLabel(String.valueOf(plateStationCount));
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error updating station count labels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshChefTable() {
        try {
            // Clear existing table without adjusting the bank balance
            while (chefTable.getRowCount() > 1) {
                chefTable.deleteRow(1);
            }
    
            // Repopulate from local storage without deducting bank balance again
            for (var entry : chefs.entrySet()) {
                logger.info("[ChefConfigurationView] Adding chef to table: " + entry.getKey());
                var chef = entry.getValue();
                String stations = String.join(", ", chef.stations);
                addChefToTable(chef.name, stations, chef.speed, chef.cost, chef.strategy, false);
            }
            
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error refreshing chef table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createInputForm() {
        try {
            
            window.addLabel("Add New Chef:", 2, 16);
            
            // Name field
            window.addLabel("Name:", 2, 18);
            nameField = window.addField(8, 18, 15, false);
            
            // Create a local nullAction instead of using the parent's
            TAction localNullAction = new TAction() {
                public void DO() {
                    // Do nothing
                }
            };
            
            // Speed selection
            window.addLabel("Speed:", 30, 18);
            List<String> speeds = new ArrayList<>();
            for (int i = 1; i <= maxSpeed; i++) {
                speeds.add(String.valueOf(i));
            }
            speedCombo = window.addComboBox(36, 18, 8, speeds, 0, 4, localNullAction);
            
            // Strategy selection - Updated to match ConfigurationController options
            window.addLabel("Strategy:", 50, 18);
            List<String> strategies = new ArrayList<>();
            strategies.add("DYNAMIC");    // DynamicChefStrategy
            strategies.add("OLDEST");     // OldestOrderFirstStrategy
            strategies.add("LONGEST_QUEUE"); // LongestQueueFirstStrategy
            strategies.add("SIMPLE");     // SimpleChefStrategy
            strategyCombo = window.addComboBox(58, 18, 15, strategies, 0, 4, localNullAction);
            
            // Station checkboxes
            window.addLabel("Stations:", 2, 20);
            grillCheckbox = window.addCheckBox(10, 20, "Grill", false);
            prepCheckbox = window.addCheckBox(30, 20, "Prep", false);
            plateCheckbox = window.addCheckBox(50, 20, "Plate", false);
            
            // Add chef button
            window.addButton("Add Chef", 80, 20, new TAction() {
                public void DO() {
                    handleAddChef();
                }
            });
            
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error creating input form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAddChef() {
        try {
            if (validateInputs()) {
                String name = nameField.getText();
                
                // Check if a chef with this name already exists
                if (chefs.containsKey(name)) {
                    showError("A chef with the name '" + name + "' already exists. Please use a unique name.");
                    return;
                }
                
                // Check if maximum number of chefs is reached
                if (chefs.size() >= maxChefs) {
                    showError("Maximum number of chefs (" + maxChefs + ") reached!");
                    return;
                }
                
                // Get selected stations
                List<String> selectedStations = new ArrayList<>();
                if (grillCheckbox.isChecked()) selectedStations.add("Grill");
                if (prepCheckbox.isChecked()) selectedStations.add("Prep");
                if (plateCheckbox.isChecked()) selectedStations.add("Plate");
                
                // Check if maximum stations per chef is exceeded
                if (selectedStations.size() > maxStationsPerChef) {
                    showError("Maximum number of stations per chef (" + maxStationsPerChef + ") exceeded!");
                    return;
                }
                
                int speed = Integer.parseInt(speedCombo.getText());
                double cost = calculateCost(speed, selectedStations.size());
                String strategy = strategyCombo.getText();
                
                // Check if adding this chef would cause bank balance to go negative
                if (bankBalance - cost < 0) {
                    showError("Cannot hire chef. Cost of " + String.format("%.2f", cost) + " exceeds available bankBalance of " + String.format("%.2f", bankBalance));
                    return;
                }
                
                // Add to local storage
                chefs.put(name, new ChefData(name, selectedStations, speed, cost, strategy));
                
                // Add to table for display
                addChefToTable(name, String.join(", ", selectedStations), speed, cost, strategy, true);
                
                // Clear inputs
                clearInputs();
            }
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error handling add chef: " + e.getMessage());
            e.printStackTrace();
            showError("Error adding chef: " + e.getMessage());
        }
    }

    private void removeChef() {
        try {
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

            // Get the chef's cost before removing them
            double chefCost = chefs.get(chefName).getCost();

            // Remove from local storage
            chefs.remove(chefName);

            // Adjust bank balance with the saved cost
            setBankBalance(bankBalance + chefCost);
        
            // Refresh the table
            refreshChefTable();
            
            // Clear the remove field
            removeNameField.setText("");
            
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error removing chef: " + e.getMessage());
            e.printStackTrace();
            showError("Error removing chef: " + e.getMessage());
        }
    }

    // Overloaded method that adds a chef row and conditionally deducts cost from the bank balance.
    private void addChefToTable(String name, String stations, int speed, double cost, String strategy, boolean deductCost) {
        try {
            int row = chefTable.getRowCount() - 1;
            chefTable.insertRowBelow(row);
            chefTable.setCellText(0, row, name);
            chefTable.setCellText(1, row, stations);
            chefTable.setCellText(2, row, String.valueOf(speed));
            chefTable.setCellText(3, row, String.format("%.2f", cost));
            chefTable.setCellText(4, row, strategy);
            
            if (deductCost) {
                setBankBalance(bankBalance - cost);
            }
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error adding chef to table: " + e.getMessage());
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
                if (speed < 1 || speed > maxSpeed) {
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
            logger.error("[ChefConfigurationView] Error validating inputs: " + e.getMessage());
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
            logger.error("[ChefConfigurationView] Error clearing inputs: " + e.getMessage());
            e.printStackTrace();
        }
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
            logger.error("[ChefConfigurationView] Error validating station coverage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean validateConfiguration() {
        try {
            // Check if there are enough chefs
            if (chefs.size() < minChefs) {
                showError("You must add at least " + minChefs + " chef(s)!");
                return false;
            }
            
            // Check if there are too many chefs
            if (chefs.size() > maxChefs) {
                showError("Maximum number of chefs (" + maxChefs + ") exceeded!");
                return false;
            }
            
            // Check if all station types are covered
            if (!validateStationCoverage()) {
                showError("At least one chef must be assigned to each station type!");
                return false;
            }
            
            // Check total station count
            int totalStations = grillStationCount + prepStationCount + plateStationCount;
            if (totalStations < minStations) {
                showError("You must have at least " + minStations + " total stations!");
                return false;
            }
            
            if (totalStations > maxStations) {
                showError("Maximum number of total stations (" + maxStations + ") exceeded!");
                return false;
            }
            
            // Check individual station counts
            if (grillStationCount < minInstancesOfStation) {
                showError("You must have at least " + minInstancesOfStation + " Grill station(s)!");
                return false;
            }
            
            if (prepStationCount < minInstancesOfStation) {
                showError("You must have at least " + minInstancesOfStation + " Prep station(s)!");
                return false;
            }
            
            if (plateStationCount < minInstancesOfStation) {
                showError("You must have at least " + minInstancesOfStation + " Plate station(s)!");
                return false;
            }
            
            if (grillStationCount > maxInstancesOfStation) {
                showError("Maximum number of Grill stations (" + maxInstancesOfStation + ") exceeded!");
                return false;
            }
            
            if (prepStationCount > maxInstancesOfStation) {
                showError("Maximum number of Prep stations (" + maxInstancesOfStation + ") exceeded!");
                return false;
            }
            
            if (plateStationCount > maxInstancesOfStation) {
                showError("Maximum number of Plate stations (" + maxInstancesOfStation + ") exceeded!");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error validating configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onNextPressed() {
        try {
            // Notify the mediator so the controller updates all views with the current bankBalance
            mediator.notifyBankBalanceChanged(bankBalance);
            app.showView(ViewType.DINING_CONFIGURATION);
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error navigating to next view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onBackPressed() {
        try {
            // Notify the mediator so the controller updates all views with the current bankBalance
            mediator.notifyBankBalanceChanged(bankBalance);
            app.showView(ViewType.WELCOME);
        } catch (Exception e) {
            logger.error("[ChefConfigurationView] Error navigating to previous view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add these setter methods
    public void setMinChefs(int minChefs) {
        this.minChefs = minChefs;
    }

    public void setMaxChefs(int maxChefs) {
        this.maxChefs = maxChefs;
    }

    public void setMaxStationsPerChef(int maxStationsPerChef) {
        this.maxStationsPerChef = maxStationsPerChef;
    }

    public void setMinStations(int minStations) {
        this.minStations = minStations;
    }

    public void setMaxStations(int maxStations) {
        this.maxStations = maxStations;
    }

    public void setMaxInstancesOfStation(int maxInstancesOfStation) {
        this.maxInstancesOfStation = maxInstancesOfStation;
    }

    public void setMinInstancesOfStation(int minInstancesOfStation) {
        this.minInstancesOfStation = minInstancesOfStation;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
        // Update the speed combo box if it exists
        if (speedCombo != null) {
            try {
                List<String> speeds = new ArrayList<>();
                for (int i = 1; i <= maxSpeed; i++) {
                    speeds.add(String.valueOf(i));
                }
                speedCombo.setList(speeds);
            } catch (Exception e) {
                logger.error("[ChefConfigurationView] Error updating speed combo box: " + e.getMessage());
            }
        }
    }

    public void setStandardPay(double standardPay) {
        this.standardPay = standardPay;
    }

    public void setPayMultiplierBySpeed(double payMultiplierBySpeed) {
        this.payMultiplierBySpeed = payMultiplierBySpeed;
    }

    public void setPayMultiplierByStation(double payMultiplierByStation) {
        this.payMultiplierByStation = payMultiplierByStation;
    }
    @Override
    public void setBankBalance(double newBalance) {
        super.setBankBalance(newBalance);
    }
    public double calculateCost(int speed, int numberOfStations) {
        return standardPay * (speed * payMultiplierBySpeed) * (numberOfStations * payMultiplierByStation);
    }
}
