package com.softwaredesign.project.view;

import java.util.ArrayList;
import java.util.List;
import jexer.*;
import com.softwaredesign.project.controller.ConfigurationController;
import com.softwaredesign.project.mediator.RestaurantViewMediator;

public class ChefConfigurationView extends ConfigurationView {
    private TTableWidget chefTable;
    private TField nameField;
    private TComboBox speedCombo;
    private TComboBox strategyCombo;
    private StationDropdown stationDropdown;
    private TButton stationButton;

    public ChefConfigurationView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupSpecificElements() {
        // Chef table
        createChefTable();
        
        // Input form
        createInputForm();
        
        showWarning("Warning: At least one chef must be assigned to each station type!");
    }

    private void createChefTable() {
        chefTable = window.addTable(2, 4, 130, 8, 5, 1);
        
        // Set column labels
        chefTable.setColumnLabel(0, "Chef Name");
        chefTable.setColumnLabel(1, "Stations");
        chefTable.setColumnLabel(2, "Speed");
        chefTable.setColumnLabel(3, "Cost/Hour");
        chefTable.setColumnLabel(4, "Strategy");

        // Set column widths
        chefTable.setColumnWidth(0, 20);
        chefTable.setColumnWidth(1, 40);
        chefTable.setColumnWidth(2, 10);
        chefTable.setColumnWidth(3, 15);
        chefTable.setColumnWidth(4, 20);

        // Populate from controller if available
        ConfigurationController controller = (ConfigurationController) mediator.getController("Configuration");
        if (controller != null) {
            for (var entry : controller.getChefs().entrySet()) {
                var chef = entry.getValue();
                String stations = String.join(", ", chef.getStations());
                addChefToTable(chef.getName(), stations, chef.getSpeed(), chef.getCostPerHour(), chef.getStrategy());
            }
        }
    }

    private void createInputForm() {
        List<String> speeds = new ArrayList<>();
        List<String> strategies = new ArrayList<>();

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
        speedCombo = window.addComboBox(31, 15, 8, speeds, -1, 4, nullAction);
        
        // Station dropdown section
        window.addLabel("Stations:", 41, 15);
        stationButton = window.addButton("Select Stations", 48, 15, new TAction() {
            public void DO() {
                if (stationDropdown != null) {
                    stationDropdown.toggle();
                }
            }
        });
        stationDropdown = new StationDropdown(window, 48, 16);
        
        // Strategy section
        window.addLabel("Strategy:", 70, 15);
        strategyCombo = window.addComboBox(78, 15, 15, strategies, -1, 4, nullAction);
        
        // Add button
        window.addButton("Add Chef", 95, 15, new TAction() {
            public void DO() {
                if (validateInputs()) {
                    String name = nameField.getText();
                    List<String> selectedStations = stationDropdown.getSelectedStations();
                    
                    int speed = Integer.parseInt(speedCombo.getText());
                    int totalStations = stationDropdown.getTotalStations();
                    double costPerHour = calculateCost(speed, totalStations);
                    String strategy = strategyCombo.getText();
                    
                    String allSelectedStations = String.join(", ", selectedStations);
                    addChefToTable(name, allSelectedStations, speed, costPerHour, strategy);
                    clearInputs();
                }
            }
        });
    }

    private double calculateCost(int speed, int numberOfStations) {
        return speed * numberOfStations * 100.0;
    }

    private boolean validateInputs() {
        // Validate name
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showError("Name cannot be empty");
            return false;
        }

        // Validate at least one station is selected
        if (stationDropdown.getTotalStations() == 0) {
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
    }

    private void clearInputs() {
        nameField.setText("");
        speedCombo.setIndex(0);
        strategyCombo.setIndex(0);
        stationDropdown.resetAll();
        stationDropdown.setVisible(false);
    }

    private boolean validateStationCoverage() {
        boolean hasGrill = false;
        boolean hasPrep = false;
        boolean hasPlate = false;

        // Check all rows in the table
        for (int i = 0; i < chefTable.getRowCount(); i++) {
            String stations = chefTable.getCellText(1, i);
            if (stations != null) {
                if (stations.contains("Grill")) hasGrill = true;
                if (stations.contains("Prep")) hasPrep = true;
                if (stations.contains("Plate")) hasPlate = true;
            }
        }

        return hasGrill && hasPrep && hasPlate;
    }

    @Override
    protected boolean validateConfiguration() {
        if (!validateStationCoverage()) {
            showError("At least one chef must be assigned to each station type!");
            return false;
        }
        return true;
    }

    @Override
    protected void onNextPressed() {
        app.showView(ViewType.DINING_CONFIGURATION);
    }

    @Override
    protected void onBackPressed() {
        app.showView(ViewType.WELCOME);
    }

    private void addChefToTable(String name, String stations, int speed, double costPerHour, String strategy) {
        // Add to table UI
        int row = chefTable.getRowCount()-1;
        chefTable.insertRowBelow(row);
        chefTable.setCellText(0, row, name);
        chefTable.setCellText(1, row, stations);
        chefTable.setCellText(2, row, String.valueOf(speed));
        chefTable.setCellText(3, row, String.format("%.2f", costPerHour));
        chefTable.setCellText(4, row, strategy);

        // Add to configuration controller if available
        ConfigurationController controller = (ConfigurationController) mediator.getController("Configuration");
        if (controller != null) {
            List<String> stationList = new ArrayList<>();
            for (String station : stations.split(", ")) {
                stationList.add(station.trim());
            }
            controller.addChef(name, stationList, speed, costPerHour, strategy);
        }
    }

    @Override
    protected void onConfigurationUpdate(ConfigurationController controller) {
        // Clear existing table
        while (chefTable.getRowCount() > 1) { // Keep header row
            chefTable.deleteRow(1);
        }

        // Repopulate from controller
        for (var entry : controller.getChefs().entrySet()) {
            var chef = entry.getValue();
            int row = chefTable.getRowCount()-1;
            String stations = String.join(", ", chef.getStations());
            chefTable.insertRowBelow(row);
            chefTable.setCellText(0, row, chef.getName());
            chefTable.setCellText(1, row, stations);
            chefTable.setCellText(2, row, String.valueOf(chef.getSpeed()));
            chefTable.setCellText(3, row, String.format("%.2f", chef.getCostPerHour()));
            chefTable.setCellText(4, row, chef.getStrategy());
        }
    }

    private class StationCount {
        private String type;
        private int count;
        private TLabel countLabel;
        
        public StationCount(String type, TWindow window, int x, int y) {
            this.type = type;
            this.count = 0;
            
            window.addLabel(type + ":", x, y);
            window.addButton("-", x + type.length() + 2, y, new TAction() {
                public void DO() {
                    if (count > 0) {
                        count--;
                        updateLabel();
                    }
                }
            });
            
            countLabel = window.addLabel("0", x + type.length() + 5, y);
            
            window.addButton("+", x + type.length() + 8, y, new TAction() {
                public void DO() {
                    count++;
                    updateLabel();
                }
            });
        }
        
        private void updateLabel() {
            countLabel.setLabel(String.valueOf(count));
        }
        
        public void reset() {
            count = 0;
            updateLabel();
        }
        
        public String getType() { return type; }
        public int getCount() { return count; }
    }

    private class StationDropdown extends TWindow {
        private StationCount[] stations;
        private boolean isVisible = false;
        
        public StationDropdown(TWindow parent, int x, int y) {
            super(parent.getApplication(), "Stations", x, y, 25, 10);
            stations = new StationCount[3];
            
            stations[0] = new StationCount("Grill", this, 2, 2);
            stations[1] = new StationCount("Prep", this, 2, 4);
            stations[2] = new StationCount("Plate", this, 2, 6);
            
            this.setVisible(false);
        }
        
        public void toggle() {
            isVisible = !isVisible;
            this.setVisible(isVisible);
            if (isVisible) {
                this.activate();
            }
        }

        @Override
        public void onClose() {
            isVisible = false;
            this.setVisible(false);
        }

        public StationCount getStation(int index) {
            return stations[index];
        }
        
        public void resetAll() {
            for (StationCount station : stations) {
                station.reset();
            }
        }
        
        public int getTotalStations() {
            int total = 0;
            for (StationCount station : stations) {
                total += station.getCount();
            }
            return total;
        }
        
        public List<String> getSelectedStations() {
            List<String> selected = new ArrayList<>();
            for (StationCount station : stations) {
                for (int i = 0; i < station.getCount(); i++) {
                    selected.add(station.getType());
                }
            }
            return selected;
        }
    }
}
