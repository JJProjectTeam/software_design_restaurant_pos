package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;

public class DiningConfigurationView extends ConfigurationView {


    // UI Components
    private TTableWidget waiterTable;
    private TField nameField;
    private TField removeNameField;
    private TComboBox speedCombo;
    private TLabel tableCountLabel;
    private TLabel tableCapacityLabel;
    private TLabel maxTablesLabel;
    private TLabel maxCapacityLabel; 
    private int maxTables;
    private int currentTableCount;
    private int maxCapacity;
    private int currentTableCapacity;
    private int minWaiters;
    private int maxWaiters; 
    private int maxSpeed = 5; // Default value
    private double standardPayPerHour = 10.0; // Default value
    private double payMultiplierBySpeed = 1.0; // Default value

    // Public setters for configuration constants
    public void setMaxTables(int maxTables) {
        this.maxTables = maxTables;
        // Update the label if it exists
        if (maxTablesLabel != null) {
            try {
                maxTablesLabel.setLabel("(Maximum " + maxTables + " tables)");
            } catch (Exception e) {
                System.err.println("[DiningConfigurationView] Error updating max tables label: " + e.getMessage());
            }
        }
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        // Update the label if it exists
        if (maxCapacityLabel != null) {
            try {
                maxCapacityLabel.setLabel("(Maximum " + maxCapacity + " seats per table)");
            } catch (Exception e) {
                System.err.println("[DiningConfigurationView] Error updating max capacity label: " + e.getMessage());
            }
        }
    }

    public void setMinWaiters(int minWaiters) {
        this.minWaiters = minWaiters;
    }

    public void setMaxWaiters(int maxWaiters) {
        this.maxWaiters = maxWaiters;
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
                System.err.println("[DiningConfigurationView] Error updating speed combo box: " + e.getMessage());
            }
        }
    }

    public void setStandardPayPerHour(double standardPayPerHour) {
        this.standardPayPerHour = standardPayPerHour;
    }

    public void setPayMultiplierBySpeed(double payMultiplierBySpeed) {
        this.payMultiplierBySpeed = payMultiplierBySpeed;
    }

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
        mediator.registerView(ViewType.DINING_CONFIGURATION, this);
        // Initialize with a default waiter
        waiters.put("Default Waiter", new WaiterData("Default Waiter", 2, 20.0));
    }

    // Getters for external access
    public Map<String, WaiterData> getWaiters() {
        return waiters;
    }

    public int getNumberOfTables() {
        return currentTableCount;
    }
    
    public int getTableCapacity() {
        return currentTableCapacity;
    }

    @Override
    protected void setupSpecificElements() {
        try {
            // Title
            window.addLabel("Dining Room Configuration", 2, 2);
            
            // Create waiter table
            createWaiterTable();
            
            // Create table configuration
            createTableConfiguration();
            
            // Create waiter input form
            createWaiterInputForm();
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error setting up elements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createWaiterTable() {
        try {
            window.addLabel("Current Waiters:", 2, 4);
            waiterTable = window.addTable(2, 6, 100, 8, 4, 1);
            
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
            refreshWaiterTable();
            
            // Add a field and button to remove a waiter
            window.addLabel("Remove Waiter (enter name):", 2, 14);
            removeNameField = window.addField(30, 14, 20, false);
            window.addButton("Remove", 55, 14, new TAction() {
                public void DO() {
                    removeWaiter();
                }
            });
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error creating waiter table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshWaiterTable() {
        try {
            // Clear existing table
            while (waiterTable.getRowCount() > 1) {
                waiterTable.deleteRow(1);
            }

            // Populate from local storage without deducting costs again
            for (var entry : waiters.entrySet()) {
                var waiter = entry.getValue();
                addWaiterToTable(waiter.name, waiter.speed, waiter.costPerHour, false);
            }
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error refreshing waiter table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTableConfiguration() {
        try {
            // Table count configuration
            window.addLabel("Number of Tables:", 2, 20);
            tableCountLabel = window.addLabel("0", 25, 20);
            
            // Add buttons to increase/decrease table count
            window.addButton("-", 30, 20, new TAction() {
                public void DO() {
                    if (currentTableCount > 0) {
                        currentTableCount--;
                        updateTableCountLabel();
                    }
                }
            });
            
            window.addButton("+", 35, 20, new TAction() {
                public void DO() {
                    if (currentTableCount < maxTables) {
                        currentTableCount++;
                        updateTableCountLabel();
                    }
                }
            });
            
            maxTablesLabel = window.addLabel("(Maximum " + maxTables + " tables)", 40, 20);
            
            // Table capacity configuration
            window.addLabel("Table Capacity:", 2, 22);
            tableCapacityLabel = window.addLabel("0", 25, 22);
            
            // Add buttons to increase/decrease table capacity
            window.addButton("-", 30, 22, new TAction() {
                public void DO() {
                    if (currentTableCapacity > 0) {
                        currentTableCapacity--;
                        updateTableCapacityLabel();
                    }
                }
            });
            
            window.addButton("+", 35, 22, new TAction() {
                public void DO() {
                    if (currentTableCapacity < maxCapacity) {
                        currentTableCapacity++;
                        updateTableCapacityLabel();
                    }
                }
            });
            
            maxCapacityLabel = window.addLabel("(Maximum " + maxCapacity + " seats per table)", 40, 22);
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error creating table configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTableCountLabel() {
        try {
            tableCountLabel.setLabel(String.valueOf(currentTableCount));
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error updating table count label: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTableCapacityLabel() {
        try {
            tableCapacityLabel.setLabel(String.valueOf(currentTableCapacity));
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error updating table capacity label: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createWaiterInputForm() {
        try {
            window.addLabel("Add New Waiter:", 2, 16);
            
            // Name field
            window.addLabel("Name:", 2, 18);
            nameField = window.addField(8, 18, 20, false);
            
            // Speed selection
            window.addLabel("Speed:", 30, 18);
            List<String> speeds = new ArrayList<>();
            for (int i = 1; i <= maxSpeed; i++) {
                speeds.add(String.valueOf(i));
            }
            speedCombo = window.addComboBox(36, 18, 10, speeds, 0, 3, nullAction);
            
            // Add waiter button
            window.addButton("Add Waiter", 50, 18, new TAction() {
                public void DO() {
                    addWaiter();
                }
            });
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error creating waiter input form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addWaiter() {
        try {
            String name = nameField.getText();
            if (name.trim().isEmpty()) {
                showError("Please enter a waiter name");
                return;
            }
            
            // Check if a waiter with this name already exists
            if (waiters.containsKey(name)) {
                showError("A waiter with the name '" + name + "' already exists. Please use a unique name.");
                return;
            }

            int speed = Integer.parseInt(speedCombo.getText());
            double costPerHour = calculateCost(speed);

            // Check if adding this waiter would cause bank balance to go negative
            if (bankBalance - costPerHour < 0) {
                showError("Cannot hire waiter. Cost of " + String.format("%.2f", costPerHour) + " exceeds available budget of " + String.format("%.2f", bankBalance));
                return;
            }

            // Add to local storage
            waiters.put(name, new WaiterData(name, speed, costPerHour));
            
            // Add to table and deduct cost
            addWaiterToTable(name, speed, costPerHour, true);

            // Clear inputs
            nameField.setText("");
            speedCombo.setIndex(0);
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error adding waiter: " + e.getMessage());
            e.printStackTrace();
            showError("Error adding waiter: " + e.getMessage());
        }
    }

    private void addWaiterToTable(String name, int speed, double costPerHour, boolean deductCost) {
        try {
            int row = waiterTable.getRowCount();
            waiterTable.insertRowBelow(row-1);
            waiterTable.setCellText(0, row, name);
            waiterTable.setCellText(1, row, String.valueOf(speed));
            waiterTable.setCellText(2, row, String.format("%.2f", costPerHour));
            waiterTable.setCellText(3, row, "0"); // Initially no tables assigned
            
            if (deductCost) {
                setBankBalance(bankBalance - costPerHour);
            }
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error adding waiter to table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateCost(int speed) {
        return standardPayPerHour * (1 + (speed - 1) * payMultiplierBySpeed);
    }

    @Override
    protected boolean validateConfiguration() {
        try {
            if (waiters.size() < minWaiters) {
                showError("At least " + minWaiters + " waiter(s) must be added");
                return false;
            }
            if (waiters.size() > maxWaiters) {
                showError("Maximum number of waiters (" + maxWaiters + ") exceeded");
                return false;
            }
            if (currentTableCount == 0) {
                showError("At least one table must be added");
                return false;
            }
            if (currentTableCapacity == 0) {
                showError("Table capacity must be set (at least 1)");
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error validating configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onNextPressed() {
        try {
            // Notify the mediator so the controller updates all views with the current budget
            mediator.notifyBudgetChanged(bankBalance);
            app.showView(ViewType.MENU_CONFIGURATION);
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error navigating to next view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onBackPressed() {
        try {
            // Notify the mediator so the controller updates all views with the current budget
            mediator.notifyBudgetChanged(bankBalance);
            app.showView(ViewType.CHEF_CONFIGURATION);
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error navigating to previous view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeWaiter() {
        try {
            // Get the waiter name from the field
            String waiterName = removeNameField.getText();
            
            // Check if a name was entered
            if (waiterName == null || waiterName.trim().isEmpty()) {
                showError("Please enter a waiter name to remove");
                return;
            }
            
            // Check if the waiter exists
            if (!waiters.containsKey(waiterName)) {
                showError("Waiter '" + waiterName + "' not found");
                return;
            }
            
            // Don't allow removing waiters below minimum
            if (waiters.size() <= minWaiters) {
                showError("Cannot remove waiter. At least " + minWaiters + " waiter(s) required.");
                return;
            }

            // Get the waiter's cost before removing them
            double waiterCost = waiters.get(waiterName).getCostPerHour();
            
            // Remove from local storage
            waiters.remove(waiterName);
            
            // Refund the cost to the bank balance
            setBankBalance(bankBalance + waiterCost);
            
            // Refresh the table
            refreshWaiterTable();
            
            // Clear the remove field
            removeNameField.setText("");
            
            System.out.println("[DiningConfigurationView] Waiter removed: " + waiterName);
        } catch (Exception e) {
            System.err.println("[DiningConfigurationView] Error removing waiter: " + e.getMessage());
            e.printStackTrace();
            showError("Error removing waiter: " + e.getMessage());
        }
    }

    @Override
    public void setBankBalance(double newBalance) {
        super.setBankBalance(newBalance);
    }
}
