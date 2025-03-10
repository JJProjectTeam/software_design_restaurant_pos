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
    private Map<Integer, TableData> tableDataMap;
    private static final String[] COLUMN_HEADERS = {"Table #", "Capacity", "Customers", "Status", "Waiter"};
    private static final int TABLE_Y = 3;
    private static final int TABLE_HEIGHT = 10;
    private static final int[] COLUMN_WIDTHS = {8, 10, 10, 10, 8};

    public DiningRoomView(RestaurantApplication app) {
        super(app);
        this.app = app;
        this.tableDataMap = new HashMap<>();
        RestaurantViewMediator.getInstance().registerView(ViewType.DINING_ROOM, this);
    }

    @Override
    public void initialize(TWindow window) {
        System.out.println("[DiningRoomView] Initializing view");
        super.initialize(window);  // This will set up the window and call setupView()
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
        tableWidget = window.addTable(2, TABLE_Y, window.getWidth() - 4, TABLE_HEIGHT, 5, 1);
        
        // Set column labels
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            tableWidget.setColumnLabel(i, COLUMN_HEADERS[i]);
            tableWidget.setColumnWidth(i, COLUMN_WIDTHS[i]);
        }
    }

    public void addTable(int tableNumber, int capacity, int customers, String status, char waiterId) {
        System.out.println("[DiningRoomView] Updating table " + tableNumber);
        
        TableData data = new TableData(tableNumber, capacity, customers, status, waiterId);
        tableDataMap.put(tableNumber, data);
    }

    public void updateAllTables() {
        if (tableWidget == null) {
            System.err.println("[DiningRoomView] Table widget not initialized");
            return;
        }

        try {
            // Get sorted table numbers
            List<Integer> tableNumbers = new ArrayList<>(tableDataMap.keySet());
            Collections.sort(tableNumbers);
            
            // Make sure we have at least one row
            if (tableWidget.getRowCount() == 0) {
                tableWidget.insertRowAbove(0);
            }
                
            // Update or add rows as needed
            for (int i = 0; i < tableNumbers.size(); i++) {
                TableData data = tableDataMap.get(tableNumbers.get(i));
                
                // Add new row if needed
                while (i >= tableWidget.getRowCount()) {
                    tableWidget.insertRowBelow(tableWidget.getRowCount() - 1);
                }
                
                // Update cells
                tableWidget.setCellText(0, i, String.valueOf(data.tableNumber));
                tableWidget.setCellText(1, i, String.valueOf(data.capacity));
                tableWidget.setCellText(2, i, String.valueOf(data.customers));
                tableWidget.setCellText(3, i, data.status);
                tableWidget.setCellText(4, i, String.valueOf(data.waiterId));
            }
            
            // Clear any extra rows
            for (int i = tableNumbers.size(); i < tableWidget.getRowCount(); i++) {
                for (int col = 0; col < COLUMN_HEADERS.length; col++) {
                    tableWidget.setCellText(col, i, "");
                }
            }

            System.out.println("[DiningRoomView] Updated " + tableDataMap.size() + " tables, cleared remaining rows");
        } catch (Exception e) {
            System.err.println("[DiningRoomView] Error updating tables: " + e.getMessage());
            e.printStackTrace();
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
}
