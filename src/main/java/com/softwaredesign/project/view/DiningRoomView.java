package com.softwaredesign.project.view;

import jexer.*;
import com.softwaredesign.project.mediator.TableUpdateMediator;
import java.util.Queue;
import java.util.LinkedList;

public class DiningRoomView extends GamePlayView {
    private TTableWidget seatingPlan;
    private boolean isInitialized = false;
    private Queue<TableUpdate> pendingUpdates = new LinkedList<>();
    
    private static class TableUpdate {
        final int tableNumber;
        final int capacity;
        final int occupied;
        final String status;
        final char waiterPresent;
        
        TableUpdate(int tableNumber, int capacity, int occupied, String status, char waiterPresent) {
            this.tableNumber = tableNumber;
            this.capacity = capacity;
            this.occupied = occupied;
            this.status = status;
            this.waiterPresent = waiterPresent;
        }
    }
    
    public DiningRoomView(RestaurantApplication app) {
        super(app);
        System.out.println("[DiningRoomView] View created");
    }

    @Override
    protected void setupView() {
        super.setupView();
        System.out.println("[DiningRoomView] Setup view called");
    }

    @Override
    protected void addViewContent() {
        System.out.println("[DiningRoomView] Adding view content");
        window.addLabel("Dining Room", 2, 6);
        window.addLabel("Tables", 2, 8);
        createSeatingPlan();
        
        // Now that the view is fully initialized, register with the mediator
        isInitialized = true;
        TableUpdateMediator.getInstance().registerView(this);
        
        // Process any pending updates
        while (!pendingUpdates.isEmpty()) {
            TableUpdate update = pendingUpdates.poll();
            updateTable(update.tableNumber, update.capacity, update.occupied, 
                       update.status, update.waiterPresent);
        }
    }

    protected void createSeatingPlan() {
        System.out.println("[DiningRoomView] Creating seating plan table");
        seatingPlan = window.addTable(2, 3, 100, 8, 5, 10);

        seatingPlan.setColumnLabel(0, "Table");
        seatingPlan.setColumnLabel(1, "Capacity");
        seatingPlan.setColumnLabel(2, "Occupied");
        seatingPlan.setColumnLabel(3, "Status");
        seatingPlan.setColumnLabel(4, "Waiter");

        for (int i = 0; i < seatingPlan.getColumnCount(); i++) {
            seatingPlan.setColumnWidth(i, 10);
        }
        System.out.println("[DiningRoomView] Seating plan table created with " + seatingPlan.getColumnCount() + " columns");
    }

    public void onTableUpdate(int tableNumber, int capacity, int occupied, String status, char waiterPresent) {
        System.out.println("[DiningRoomView] Received table update for table " + tableNumber);
        
        if (!isInitialized) {
            System.out.println("[DiningRoomView] View not initialized yet, queueing update for table " + tableNumber);
            pendingUpdates.offer(new TableUpdate(tableNumber, capacity, occupied, status, waiterPresent));
            return;
        }
        
        updateTable(tableNumber, capacity, occupied, status, waiterPresent);
    }
    
    private void updateTable(int tableNumber, int capacity, int occupied, String status, char waiterPresent) {
        if (seatingPlan == null || window == null) {
            System.out.println("[DiningRoomView] ERROR: seatingPlan or window is null!");
            return;
        }
        
        try {
            //check if the row exists
            if (seatingPlan.getRowLabel(tableNumber) == null) {
                System.out.println("[DiningRoomView] Creating new row for table " + tableNumber);
                seatingPlan.insertRowBelow(tableNumber);
                seatingPlan.setRowLabel(tableNumber, Integer.toString(tableNumber));
            }

            seatingPlan.setCellText(0, tableNumber, Integer.toString(tableNumber));
            seatingPlan.setCellText(1, tableNumber, Integer.toString(capacity));
            seatingPlan.setCellText(2, tableNumber, Integer.toString(occupied));    
            seatingPlan.setCellText(3, tableNumber, status);
            seatingPlan.setCellText(4, tableNumber, Character.toString(waiterPresent));
            System.out.println("[DiningRoomView] Successfully updated table " + tableNumber + " in the view");
        } catch (Exception e) {
            System.out.println("[DiningRoomView] ERROR updating table " + tableNumber + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
