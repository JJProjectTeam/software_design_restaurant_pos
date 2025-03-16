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
    private Queue<TableUpdate> pendingUpdates;
    private static final String[] COLUMN_HEADERS = {"Table #", "Capacity", "Customers", "Status", "Waiter"};
    private static final int[] COLUMN_WIDTHS = {8, 10, 10, 10, 8};
    private static final int TABLE_Y = 3;
    private static final int TABLE_HEIGHT = 10;
    private boolean isInitialized;
    private double bankBalance;

    private static class TableUpdate {
        final int tableNumber;
        final int capacity;
        final int customers;
        final String status;
        final char waiterId;

        TableUpdate(int tableNumber, int capacity, int customers, String status, char waiterId) {
            this.tableNumber = tableNumber;
            this.capacity = capacity;
            this.customers = customers;
            this.status = status;
            this.waiterId = waiterId;
        }
    }

    public DiningRoomView(RestaurantApplication app) {
        super(app);
        this.app = app;
        this.isInitialized = false;
        this.pendingUpdates = new LinkedList<>();
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
        
        // Set column labels and widths
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            tableWidget.setColumnLabel(i, COLUMN_HEADERS[i]);
            tableWidget.setColumnWidth(i, COLUMN_WIDTHS[i]);
        }

        isInitialized = true;
        
        // Process any pending updates
        while (!pendingUpdates.isEmpty()) {
            TableUpdate update = pendingUpdates.poll();
            updateTableInWidget(update.tableNumber, update.capacity, update.customers, 
                update.status, update.waiterId);
        }
    }

    public void onTableUpdate(int tableNumber, int capacity, int customers, String status, char waiterId) {
        TableUpdate update = new TableUpdate(tableNumber, capacity, customers, status, waiterId);
        if (!isInitialized) {
            System.out.println("[DiningRoomView] View not yet initialized, queueing update for table: " + tableNumber);
            pendingUpdates.offer(update);
        } else {
            updateTableInWidget(tableNumber, capacity, customers, status, waiterId);
        }
    }

    private void updateTableInWidget(int tableNumber, int capacity, int customers, String status, char waiterId) {
        if (tableWidget == null) {
            System.err.println("[DiningRoomView] Table widget not initialized");
            return;
        }

        try {
            // Make sure we have enough rows
            if (tableWidget.getRowCount() == 0) {
                tableWidget.insertRowAbove(0);
            }
            while (tableNumber >= tableWidget.getRowCount()) {
                tableWidget.insertRowBelow(tableWidget.getRowCount() - 1);
            }
            
            // Update cells
            tableWidget.setCellText(0, tableNumber - 1, String.valueOf(tableNumber));
            tableWidget.setCellText(1, tableNumber - 1, String.valueOf(capacity));
            tableWidget.setCellText(2, tableNumber - 1, String.valueOf(customers));
            tableWidget.setCellText(3, tableNumber - 1, status);
            tableWidget.setCellText(4, tableNumber - 1, String.valueOf(waiterId));

            System.out.println("[DiningRoomView] Updated table " + tableNumber);
        } catch (Exception e) {
            System.err.println("[DiningRoomView] Error updating table " + tableNumber + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setBankBalance(double newBalance) {
        super.setBankBalance(newBalance);
        System.out.println("[DiningRoomView] Updated bank balance to: $" + String.format("%.2f", bankBalance));
    }
}
