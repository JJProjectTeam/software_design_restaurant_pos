package com.softwaredesign.project.view;

import com.softwaredesign.project.controller.BaseController;
import com.softwaredesign.project.controller.DiningRoomController;
import com.softwaredesign.project.orderfulfillment.Table;
import jexer.*;

import java.util.*;

public class DiningRoomView extends GamePlayView {
    private final RestaurantApplication app;    
    private TWindow window;
    private TTableWidget tableWidget;
    private Map<Integer, Integer> tableToRow;

    public DiningRoomView(RestaurantApplication app) {
        super(app);
        this.app = app;
        this.tableToRow = new HashMap<>();
    }

    @Override
    public void initialize(TWindow window) {
        this.window = window;
        setupView();
    }

    @Override
    public void cleanup() {
        window.close();
    }

    @Override
    public TWindow getWindow() {
        return window;
    }

    @Override
    public void setupView() {
        addViewContent();
    }

    @Override
    protected void addViewContent() {
        window.addLabel("Dining Room", 2, 2);
        tableWidget = window.addTable(2, 4, 60, 10);
        
        tableWidget.setColumnLabel(0, "Table");
        tableWidget.setColumnLabel(1, "Capacity");
        tableWidget.setColumnLabel(2, "Customers");
        tableWidget.setColumnLabel(3, "Status");
        tableWidget.setColumnLabel(4, "Waiter");

        tableWidget.setColumnWidth(0, 8);  // Table number
        tableWidget.setColumnWidth(1, 10); // Capacity
        tableWidget.setColumnWidth(2, 12); // Customers
        tableWidget.setColumnWidth(3, 10); // Status
        tableWidget.setColumnWidth(4, 8);  // Waiter
    }

    public void updateFromController(BaseController controller) {
        if (!(controller instanceof DiningRoomController)) {
            return;
        }
        
        DiningRoomController diningController = (DiningRoomController) controller;
        for (Table table : diningController.getSeatingPlan().getAllTables()) {
            onTableUpdate(table.getTableNumber(), table.getTableCapacity(), 
                table.getCustomers().size(), determineTableStatus(table), ' ');
        }
    }

    public void onTableUpdate(int tableNumber, int capacity, int customers, String status, char waiterId) {
        int rowIndex = tableToRow.computeIfAbsent(tableNumber, k -> {
            int newRow = tableWidget.getRowCount();
            tableWidget.insertRowBelow(newRow-1);
            tableWidget.setCellText(0, newRow, String.valueOf(tableNumber));
            tableWidget.setCellText(1, newRow, String.valueOf(capacity));
            tableWidget.setCellText(2, newRow, String.valueOf(customers));
            tableWidget.setCellText(3, newRow, status);
            tableWidget.setCellText(4, newRow, String.valueOf(waiterId));
            return newRow;
        });

        tableWidget.setCellText(rowIndex, 0, String.valueOf(tableNumber));
        tableWidget.setCellText(rowIndex, 1, String.valueOf(capacity));
        tableWidget.setCellText(rowIndex, 2, String.valueOf(customers));
        tableWidget.setCellText(rowIndex, 3, status);
        tableWidget.setCellText(rowIndex, 4, String.valueOf(waiterId));
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
}
