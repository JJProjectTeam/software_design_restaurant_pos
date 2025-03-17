package com.softwaredesign.project.view;

import java.util.*;
import jexer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiningConfigurationView extends ConfigurationView {
    private static final Logger logger = LoggerFactory.getLogger(DiningConfigurationView.class);

    // UI Components
    private TTableWidget waiterTable;
    private TField nameField;
    private TField removeNameField;
    private TLabel tableCountLabel;
    private TLabel tableCapacityLabel;
    private TLabel maxTablesLabel;
    private TLabel maxCapacityLabel; 
    private int maxTables;
    private int currentTableCount = 1;
    private int maxCapacity;
    private int currentTableCapacity = 1;
    private int minWaiters;
    private int maxWaiters; 
    private double standardPay = 10.0; // Default value

    // Public setters for configuration constants
    public void setMaxTables(int maxTables) {
        this.maxTables = maxTables;
        // Update the label if it exists
        if (maxTablesLabel != null) {
            try {
                maxTablesLabel.setLabel("(Maximum " + maxTables + " tables)");
            } catch (Exception e) {
                logger.error("[DiningConfigurationView] Error updating max tables label: " + e.getMessage());
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
                logger.error("[DiningConfigurationView] Error updating max capacity label: " + e.getMessage());
            }
        }
    }

    public void setMinWaiters(int minWaiters) {
        this.minWaiters = minWaiters;
    }

    public void setMaxWaiters(int maxWaiters) {
        this.maxWaiters = maxWaiters;
    }

    public void setStandardPay(double standardPay) {
        this.standardPay = standardPay;
    }

    // Local storage for waiter data
    private Map<String, WaiterData> waiters = new HashMap<>();

    // Inner class to hold waiter data
    public static class WaiterData {
        String name;
        double cost;
        List<Integer> assignedTables;

        WaiterData(String name, double cost) {
            this.name = name;
            this.cost = cost;
            this.assignedTables = new ArrayList<>();
        }
        
        public String getName() {
            return name;
        }
        
        public double getCost() {
            return cost;
        }
    }

    public DiningConfigurationView(RestaurantApplication app) {
        super(app);
        mediator.registerView(ViewType.DINING_CONFIGURATION, this);
        // Initialize with a default waiter
        waiters.put("Default Waiter", new WaiterData("Default Waiter", 20.0));
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
            logger.error("[DiningConfigurationView] Error setting up elements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createWaiterTable() {
        try {
            window.addLabel("Current Waiters:", 2, 4);
            waiterTable = window.addTable(2, 6, 100, 8, 4, 1);
            
            // Set column labels
            waiterTable.setColumnLabel(0, "Waiter Name");
            waiterTable.setColumnLabel(1, "Cost");
            waiterTable.setColumnLabel(2, "Tables Assigned");

            // Set column widths
            waiterTable.setColumnWidth(0, 25);
            waiterTable.setColumnWidth(1, 15);
            waiterTable.setColumnWidth(2, 20);

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
            logger.error("[DiningConfigurationView] Error creating waiter table: " + e.getMessage());
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
                addWaiterToTable(waiter.name, waiter.cost, false);
            }
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error refreshing waiter table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTableConfiguration() {
        try {
            // Table count configuration
            window.addLabel("Number of Tables:", 2, 20);
            tableCountLabel = window.addLabel(String.valueOf(currentTableCount), 25, 20);
            
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
            tableCapacityLabel = window.addLabel(String.valueOf(currentTableCapacity), 25, 22);
            
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
            
            // Update labels to show current values
            updateTableCountLabel();
            updateTableCapacityLabel();
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error creating table configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTableCountLabel() {
        try {
            tableCountLabel.setLabel(String.valueOf(currentTableCount));
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error updating table count label: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTableCapacityLabel() {
        try {
            tableCapacityLabel.setLabel(String.valueOf(currentTableCapacity));
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error updating table capacity label: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createWaiterInputForm() {
        try {
            window.addLabel("Add New Waiter:", 2, 16);
            
            // Name field
            window.addLabel("Name:", 2, 18);
            nameField = window.addField(8, 18, 20, false);
            
            // Add waiter button
            window.addButton("Add Waiter", 50, 18, new TAction() {
                public void DO() {
                    addWaiter();
                }
            });
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error creating waiter input form: " + e.getMessage());
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

            double cost = calculateCost();

            // Check if adding this waiter would cause bank balance to go negative
            if (bankBalance - cost < 0) {
                showError("Cannot hire waiter. Cost of " + String.format("%.2f", cost) + " exceeds available bankBalance of " + String.format("%.2f", bankBalance));
                return;
            }

            // Add to local storage
            waiters.put(name, new WaiterData(name, cost));
            
            // Add to table and deduct cost
            addWaiterToTable(name, cost, true);

            // Clear inputs
            nameField.setText("");
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error adding waiter: " + e.getMessage());
            e.printStackTrace();
            showError("Error adding waiter: " + e.getMessage());
        }
    }

    private void addWaiterToTable(String name, double cost, boolean deductCost) {
        try {
            int row = waiterTable.getRowCount();
            waiterTable.insertRowBelow(row-1);
            waiterTable.setCellText(0, row, name);
            waiterTable.setCellText(2, row, String.format("%.2f", cost));
            waiterTable.setCellText(3, row, "0"); // Initially no tables assigned
            
            if (deductCost) {
                setBankBalance(bankBalance - cost);
            }
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error adding waiter to table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateCost() {
        return standardPay;
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
            logger.error("[DiningConfigurationView] Error validating configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onNextPressed() {
        try {
            // Notify the mediator so the controller updates all views with the current bankBalance
            mediator.notifyBankBalanceChanged(bankBalance);
            app.showView(ViewType.MENU_CONFIGURATION);
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error navigating to next view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onBackPressed() {
        try {
            // Notify the mediator so the controller updates all views with the current bankBalance
            mediator.notifyBankBalanceChanged(bankBalance);
            app.showView(ViewType.CHEF_CONFIGURATION);
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error navigating to previous view: " + e.getMessage());
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
            double waiterCost = waiters.get(waiterName).getCost();
            
            // Remove from local storage
            waiters.remove(waiterName);
            
            // Refund the cost to the bank balance
            setBankBalance(bankBalance + waiterCost);
            
            // Refresh the table
            refreshWaiterTable();
            
            // Clear the remove field
            removeNameField.setText("");
            
            logger.info("[DiningConfigurationView] Waiter removed: " + waiterName);
        } catch (Exception e) {
            logger.error("[DiningConfigurationView] Error removing waiter: " + e.getMessage());
            e.printStackTrace();
            showError("Error removing waiter: " + e.getMessage());
        }
    }

    @Override
    public void setBankBalance(double newBalance) {
        super.setBankBalance(newBalance);
    }
}
