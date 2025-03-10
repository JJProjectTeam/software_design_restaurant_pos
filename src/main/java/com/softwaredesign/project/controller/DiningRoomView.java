package com.softwaredesign.project.view;oller;

import com.softwaredesign.project.controller.BaseController;iator;
import com.softwaredesign.project.controller.DiningRoomController;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.orderfulfillment.Table;
import jexer.*;waredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;
import java.util.*;design.project.customer.DineInCustomer;

public class DiningRoomView extends GamePlayView {
    private final RestaurantApplication app;    
    private TTableWidget tableWidget;
    private Map<Integer, TableData> tableDataMap;
    private static final String[] COLUMN_HEADERS = {"Table #", "Capacity", "Customers", "Status", "Waiter"};
    private static final int TABLE_Y = 3;
    private static final int TABLE_HEIGHT = 10;ontroller {
    private static final int[] COLUMN_WIDTHS = {8, 10, 10, 10, 8};
    private Map<Integer, Character> tableToWaiter;
    public DiningRoomView(RestaurantApplication app) {
        super(app);
        this.app = app;ntroller(Menu menu, SeatingPlan seatingPlan) {
        this.tableDataMap = new HashMap<>();
        RestaurantViewMediator.getInstance().registerView(ViewType.DINING_ROOM, this);
    }   this.tableToWaiter = new HashMap<>();
        this.mediator = RestaurantViewMediator.getInstance();
    @Override
    public void initialize(TWindow window) {
        System.out.println("[DiningRoomView] Initializing view");
        super.initialize(window);  // This will set up the window and call setupView()
    }
    public void assignWaiterToTable(int tableNumber, char waiterId) {
    @OverrideToWaiter.put(tableNumber, waiterId);
    public void cleanup() {pdate(seatingPlan.getTable(tableNumber));
        if (window != null) {
            window.close();
        }c void addCustomerToTable(int tableNumber, DineInCustomer customer) {
    }   Table table = seatingPlan.getTable(tableNumber);
        table.addCustomer(customer);
    @OverrideyViewsOfTableUpdate(table);
    protected void addViewContent() {
        window.addLabel("Dining Room", 2, 2);
        tableWidget = window.addTable(2, TABLE_Y, window.getWidth() - 4, TABLE_HEIGHT, 5, 1);
        // First update our internal state
        // Set column labelsble.getTableNumber();
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {umber, ' ');
            tableWidget.setColumnLabel(i, COLUMN_HEADERS[i]);
            tableWidget.setColumnWidth(i, COLUMN_WIDTHS[i]);
        }ist<View> views = mediator.getViews("DiningRoom");
    }   for (View view : views) {
            if (view instanceof DiningRoomView) {
    public void updateAllTables(Map<Integer, TableData> newTableData) {
        System.out.println("[DiningRoomView] Updating all tables");
        this.tableDataMap = new HashMap<>(newTableData);            tableNumber,
        updateTableDisplay();
    }e(),
            determineTableStatus(table),
    private void updateTableDisplay() {terId
        if (tableWidget == null) {           );
            System.err.println("[DiningRoomView] Table widget not initialized");            }
            return;
        }

        try {
            // Get sorted table numbersc void updateView() {
            List<Integer> tableNumbers = new ArrayList<>(tableDataMap.keySet());        View view = mediator.getView(ViewType.DINING_ROOM);
            Collections.sort(tableNumbers);(view instanceof DiningRoomView)) {
            
            // Update or add rows as needed
            for (int i = 0; i < tableNumbers.size(); i++) {
                TableData data = tableDataMap.get(tableNumbers.get(i));ngRoomView diningView = (DiningRoomView) view;
                ta> tableUpdates = new HashMap<>();
                // Add new row if needed
                if (i >= tableWidget.getRowCount()) {
                    tableWidget.insertRow(i);eUpdates.put(table.getTableNumber(), 
                }Data(
                
                // Update cells
                tableWidget.setCellText(0, i, String.valueOf(data.tableNumber));   table.getCustomers().size(),
                tableWidget.setCellText(1, i, String.valueOf(data.capacity));    determineTableStatus(table),
                tableWidget.setCellText(2, i, String.valueOf(data.customers));iter() ? table.getWaiter().getId() : ' '
                tableWidget.setCellText(3, i, data.status);
                tableWidget.setCellText(4, i, String.valueOf(data.waiterId));
            }
            
            // Clear any extra rows
            for (int i = tableNumbers.size(); i < tableWidget.getRowCount(); i++) {
                for (int col = 0; col < COLUMN_HEADERS.length; col++) {
                    tableWidget.setCellText(col, i, "");
                }
            }

            System.out.println("[DiningRoomView] Updated " + tableDataMap.size() + " tables");g determineTableStatus(Table table) {
        } catch (Exception e) {able.getCustomers().isEmpty()) {
            System.err.println("[DiningRoomView] Error updating tables: " + e.getMessage());            return "Empty";
            e.printStackTrace();
        }
    }

    private static class TableData {
        final int tableNumber;
        final int capacity;
        final int customers;n() {
        final String status;
        final char waiterId;

        TableData(int tableNumber, int capacity, int customers, String status, char waiterId) {            this.tableNumber = tableNumber;            this.capacity = capacity;            this.customers = customers;            this.status = status;            this.waiterId = waiterId;        }    }}