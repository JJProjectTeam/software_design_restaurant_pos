package com.softwaredesign.project.view;

import jexer.*;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;

import com.softwaredesign.project.mediator.RestaurantViewMediator;

import java.util.LinkedList;

public class KitchenView extends GamePlayView {
    private TTableWidget kitchenStations;
    private Queue<StationUpdate> pendingUpdates;
    private Map<Integer, StationUpdate> stationDataMap;  // Add this to store station data
    private static final String[] COLUMN_HEADERS = {"ID", "Station", "Backlog", "Chef", "In Use"};
    private static final int[] COLUMN_WIDTHS = {5, 10, 8, 15, 7};
    private RestaurantViewMediator mediator;
    private boolean isInitialized;

    private static class StationUpdate {
        final int stationID;
        final String station;
        final int backlog;
        final String chef;
        final char inUse;

        StationUpdate(int stationID, String station, int backlog, String chef, char inUse) {
            this.stationID = stationID;
            this.station = station;
            this.backlog = backlog;
            this.chef = chef;
            this.inUse = inUse;
        }
    }

    public KitchenView(RestaurantApplication app) {
        super(app);
        this.isInitialized = false;
        this.pendingUpdates = new LinkedList<>();
        this.stationDataMap = new HashMap<>();  // Initialize the map
        RestaurantViewMediator.getInstance().registerView(ViewType.KITCHEN, this);
    }

    @Override
    public void setupView() {
        super.setupView();
    }

    @Override
    protected void addViewContent() {
        window.addLabel("Stations", 2, 2);
        createKitchenStationsTable();
        
        // Now that the view is fully initialized
        isInitialized = true;
        
        // Process any pending updates
        while (!pendingUpdates.isEmpty()) {
            StationUpdate update = pendingUpdates.poll();
            updateStationInTable(update.stationID, update.station, update.backlog, update.chef, update.inUse);
        }
    }

    protected void createKitchenStationsTable() {
        System.out.println("[KitchenView] Creating kitchen table...");
        kitchenStations = window.addTable(2, 3, window.getWidth() - 4, 15, 5, 10);

        // Set column labels and widths
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            kitchenStations.setColumnLabel(i, COLUMN_HEADERS[i]);
            kitchenStations.setColumnWidth(i, COLUMN_WIDTHS[i]);
        }
    }

    public void onStationUpdate(int stationID, String station, int backlog, String chef, char inUse) {
        StationUpdate update = new StationUpdate(stationID, station, backlog, chef, inUse);
        
        // Store the update in the map regardless of initialization status
        synchronized (stationDataMap) {
            stationDataMap.put(stationID, update);
        }
        
        if (!isInitialized) {
            System.out.println("[KitchenView] View not yet initialized, queueing update for station: " + station);
            synchronized (pendingUpdates) {
                pendingUpdates.offer(update);
            }
        } else {
            updateStationInTable(stationID, station, backlog, chef, inUse);
        }
    }

    private void updateStationInTable(int stationID, String stationName, int backlog, String chef, char inUse) {
        if (kitchenStations == null) {
            System.out.println("[KitchenView] ERROR: kitchenStations is null!");
            return;
        }
        
        try {
            // Synchronize on the table widget to prevent concurrent modification
            synchronized (kitchenStations) {
                // Find or create row for this station
                int targetRow = stationID;
                
                // Add rows if needed
                while (kitchenStations.getRowCount() <= targetRow) {
                    kitchenStations.insertRowBelow(kitchenStations.getRowCount() - 1);
                }

                kitchenStations.setCellText(0, targetRow, Integer.toString(stationID));
                kitchenStations.setCellText(1, targetRow, stationName);
                kitchenStations.setCellText(2, targetRow, String.valueOf(backlog));
                kitchenStations.setCellText(3, targetRow, chef);
                kitchenStations.setCellText(4, targetRow, String.valueOf(inUse));
            }

            System.out.println("[KitchenView] Successfully updated station " + stationID + " in the view");
        } catch (Exception e) {
            System.out.println("[KitchenView] ERROR updating station " + stationID + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        System.out.println("[KitchenView] Cleaning up view, unregistering from mediator");
        mediator.unregisterView("Kitchen", this);
    }
}
