package com.softwaredesign.project.view;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import jexer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class DiningRoomView extends GamePlayView {
    private static final Logger logger = LoggerFactory.getLogger(DiningRoomView.class);
    private TTableWidget tableWidget;
    private Queue<TableUpdate> pendingUpdates;
    private static final String[] COLUMN_HEADERS = {"Table #", "Capacity", "Customers", "Status"};
    private static final int[] COLUMN_WIDTHS = {8, 10, 10, 10};
    private static final int TABLE_Y = 3;
    private static final int TABLE_HEIGHT = 10;
    private boolean isInitialized;
    private double bankBalance;

    private static class TableUpdate {
        final int tableNumber;
        final int capacity;
        final int customers;
        final String status;

        TableUpdate(int tableNumber, int capacity, int customers, String status) {
            this.tableNumber = tableNumber; 
            this.capacity = capacity;
            this.customers = customers;
            this.status = status;
        }
    }

    public DiningRoomView(RestaurantApplication app) {
        super(app);
        this.isInitialized = false;
        this.pendingUpdates = new LinkedList<>();
        RestaurantViewMediator.getInstance().registerView(ViewType.DINING_ROOM, this);
    }

    @Override
    public void initialize(TWindow window) {
        logger.info("[DiningRoomView] Initializing view");
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
        tableWidget = window.addTable(2, TABLE_Y, window.getWidth() - 4, TABLE_HEIGHT, 4, 1);
        
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
                update.status);
        }
    }

    public void onTableUpdate(int tableNumber, int capacity, int customers, String status) {
        TableUpdate update = new TableUpdate(tableNumber, capacity, customers, status);
        if (!isInitialized) {
            logger.info("[DiningRoomView] View not yet initialized, queueing update for table: " + tableNumber);
            pendingUpdates.offer(update);
        } else {
            updateTableInWidget(tableNumber, capacity, customers, status);
        }
    }

    private void updateTableInWidget(int tableNumber, int capacity, int customers, String status) {
        if (tableWidget == null) {
            logger.error("[DiningRoomView] Table widget not initialized");
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

            logger.info("[DiningRoomView] Updated table " + tableNumber);
        } catch (Exception e) {
            logger.error("[DiningRoomView] Error updating table " + tableNumber + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setBankBalance(double newBalance) {
        super.setBankBalance(newBalance);
        logger.info("[DiningRoomView] Updated bank balance to: $" + String.format("%.2f", bankBalance));
    }
}
