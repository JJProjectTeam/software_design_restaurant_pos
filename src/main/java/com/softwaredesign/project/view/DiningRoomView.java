package com.softwaredesign.project.view;

import com.softwaredesign.project.controller.BaseController;
import com.softwaredesign.project.controller.DiningRoomController;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.orderfulfillment.Table;
import jexer.*;

import java.util.*;

public class DiningRoomView extends GamePlayView {
    private final RestaurantApplication app;    
    private TTableWidget tableWidget;
    private List<TableUpdate> pendingUpdates;
    private Map<Integer, TableData> tableDataMap;  // Store table data by table number
    private static final String[] COLUMN_HEADERS = {"Table #", "Capacity", "Customers", "Status", "Waiter"};
    private static final int TABLE_Y = 3;
    private static final int TABLE_HEIGHT = 10;
    private static final int[] COLUMN_WIDTHS = {8, 10, 10, 10, 8};

    public DiningRoomView(RestaurantApplication app) {
        super(app);
        this.app = app;
        this.pendingUpdates = new ArrayList<>();
        this.tableDataMap = new HashMap<>();
        RestaurantViewMediator.getInstance().registerView(ViewType.DINING_ROOM, this);
    }

    @Override
    public void initialize(TWindow window) {
        System.out.println("[DiningRoomView] Initializing view");
        super.initialize(window);  // This will set up the window and call setupView()
        
        // Process any pending updates
        System.out.println("[DiningRoomView] Processing " + pendingUpdates.size() + " pending updates");
        for (TableUpdate update : pendingUpdates) {
            processTableUpdate(update);
        }
        pendingUpdates.clear();
    }

    @Override
    public void cleanup() {
        if (window != null) {
            window.close();
        }
    }

    @Override
    protected void addViewContent() {
        window.addLabel("Dining Room", 2, 2);
        
        // Create table widget
        tableWidget = window.addTable(2, TABLE_Y, window.getWidth() - 4, TABLE_HEIGHT, 5, 10);
        
        // Set column labels
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            tableWidget.setColumnLabel(i, COLUMN_HEADERS[i]);
            tableWidget.setColumnWidth(i, COLUMN_WIDTHS[i]);
        }
        
        // Add initial empty row
        tableWidget.insertRowBelow(0);
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            tableWidget.setCellText(i, 1, "");
        }

        // Populate table with stored data
        for (TableData data : tableDataMap.values()) {
            updateTableRow(data);
        }
    }

    public void updateFromController(BaseController controller) {
        if (!(controller instanceof DiningRoomController)) {
            return;
        }
        
        DiningRoomController diningController = (DiningRoomController) controller;
        // Clear existing rows except header
        while (tableWidget.getRowCount() > 1) {
            tableWidget.deleteRow(tableWidget.getRowCount() - 1);
        }
        
        // Add all tables
        for (Table table : diningController.getSeatingPlan().getAllTables()) {
            onTableUpdate(table.getTableNumber(), table.getTableCapacity(), 
                table.getCustomers().size(), determineTableStatus(table), ' ');
        }
    }

    public void onTableUpdate(int tableNumber, int capacity, int customers, String status, char waiterId) {
        TableData data = new TableData(tableNumber, capacity, customers, status, waiterId);
        tableDataMap.put(tableNumber, data);  // Store the data
        
        if (tableWidget == null) {
            System.out.println("[DiningRoomView] Table widget not ready, queueing update for table " + tableNumber);
            pendingUpdates.add(new TableUpdate(tableNumber, capacity, customers, status, waiterId));
            return;
        }
        updateTableRow(data);
    }

    private void processTableUpdate(TableUpdate update) {
        System.out.println("[DiningRoomView] Processing update for table " + update.tableNumber);
        try {
            // Find if row already exists for this table
            int targetRow = update.tableNumber - 1; // Convert table number to 0-based index
            
            // If we need to add new rows to reach the target position
            while (tableWidget.getRowCount() <= targetRow) {
                tableWidget.insertRowBelow(tableWidget.getRowCount() - 1);
                // Initialize the new row with empty cells
                for (int i = 0; i < COLUMN_HEADERS.length; i++) {
                    tableWidget.setCellText(i, tableWidget.getRowCount() - 1, "");
                }
            }
            
            // Update the cells in the target row
            tableWidget.setCellText(0, targetRow, String.valueOf(update.tableNumber));
            tableWidget.setCellText(1, targetRow, String.valueOf(update.capacity));
            tableWidget.setCellText(2, targetRow, String.valueOf(update.customers));
            tableWidget.setCellText(3, targetRow, update.status);
            tableWidget.setCellText(4, targetRow, String.valueOf(update.waiterId));
            
            System.out.println("[DiningRoomView] Successfully updated table " + update.tableNumber);
        } catch (Exception e) {
            System.err.println("[DiningRoomView] Error updating table " + update.tableNumber + ": " + e.getMessage());
        }
    }

    private void updateTableRow(TableData data) {
        System.out.println("[DiningRoomView] Processing update for table " + data.tableNumber);
        try {
            int targetRow = data.tableNumber - 1;
            
            // If we need to add new rows to reach the target position
            while (tableWidget.getRowCount() <= targetRow) {
                tableWidget.insertRowBelow(tableWidget.getRowCount() - 1);
                // Initialize the new row with empty cells
                for (int i = 0; i < COLUMN_HEADERS.length; i++) {
                    tableWidget.setCellText(i, tableWidget.getRowCount() - 1, "");
                }
            }
            
            // Update the cells in the target row
            tableWidget.setCellText(0, targetRow, String.valueOf(data.tableNumber));
            tableWidget.setCellText(1, targetRow, String.valueOf(data.capacity));
            tableWidget.setCellText(2, targetRow, String.valueOf(data.customers));
            tableWidget.setCellText(3, targetRow, data.status);
            tableWidget.setCellText(4, targetRow, String.valueOf(data.waiterId));
            
            System.out.println("[DiningRoomView] Successfully updated table " + data.tableNumber);
        } catch (Exception e) {
            System.err.println("[DiningRoomView] Error updating table " + data.tableNumber + ": " + e.getMessage());
        }
    }

    private String determineTableStatus(Table table) {
        if (table.getCustomers().isEmpty()) {
            return "Empty";
        } else if (table.isEveryoneReadyToOrder()) {
            return "Ready";
        } else {
            return "Browsing";
        }
    }

    private static class TableData {
        final int tableNumber;
        final int capacity;
        final int customers;
        final String status;
        final char waiterId;

        TableData(int tableNumber, int capacity, int customers, String status, char waiterId) {
            this.tableNumber = tableNumber;
            this.capacity = capacity;
            this.customers = customers;
            this.status = status;
            this.waiterId = waiterId;
        }
    }

    private static class TableUpdate extends TableData {
        TableUpdate(int tableNumber, int capacity, int customers, String status, char waiterId) {
            super(tableNumber, capacity, customers, status, waiterId);
        }
    }
}
