package com.softwaredesign.project.view;

import jexer.*;
import com.softwaredesign.project.controller.DiningRoomController;

public class DiningRoomView extends GamePlayView {
    TTableWidget seatingPlan;
    private DiningRoomController controller;
    
    public DiningRoomView(RestaurantApplication app) {
        super(app);
    }

    public void setController(DiningRoomController controller) {
        this.controller = controller;
    }

    @Override
    protected void setupView() {
        super.setupView();
    }

    @Override
    protected void addViewContent() {
        window.addLabel("Dining Room", 2, 6);
        window.addLabel("Tables", 2, 8);
        createSeatingPlan();
        
        // If we have a controller, update with its data
        if (controller != null) {
            controller.updateAllTableViews();
        }
    }

    protected void createSeatingPlan() {
        seatingPlan = window.addTable(2, 3, 100, 8, 5, 10);

        seatingPlan.setColumnLabel(0, "Table");
        seatingPlan.setColumnLabel(1, "Capacity");
        seatingPlan.setColumnLabel(2, "Occupied");
        seatingPlan.setColumnLabel(3, "Status");
        seatingPlan.setColumnLabel(4, "Waiter");

        for (int i = 0; i < seatingPlan.getColumnCount(); i++) {
            seatingPlan.setColumnWidth(i, 10);
        }
    }

    public void AddOrUpdateRow(int tableNumber, int capacity, int occupied, String status, char waiterPresent) {
        if (seatingPlan == null) return;
        
        //check if the row exists
        if (seatingPlan.getRowLabel(tableNumber) == null) {
            seatingPlan.insertRowBelow(tableNumber);
            seatingPlan.setRowLabel(tableNumber, Integer.toString(tableNumber));
        }
        seatingPlan.setCellText(0, tableNumber, Integer.toString(tableNumber));
        seatingPlan.setCellText(1, tableNumber, Integer.toString(capacity));
        seatingPlan.setCellText(2, tableNumber, Integer.toString(occupied));    
        seatingPlan.setCellText(3, tableNumber, status);
        seatingPlan.setCellText(4, tableNumber, Character.toString(waiterPresent));
    }
}
