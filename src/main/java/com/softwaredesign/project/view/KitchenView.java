package com.softwaredesign.project.view;

import jexer.*;
import java.util.Queue;

import com.softwaredesign.project.mediator.RestaurantViewMediator;

import java.util.LinkedList;

public class KitchenView extends GamePlayView {
    private TTableWidget kitchenStations;
    private Queue<StationUpdate> pendingUpdates;
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
        this.mediator = RestaurantViewMediator.getInstance();
        this.isInitialized = false;
        this.pendingUpdates = new LinkedList<>();
        mediator.registerView(ViewType.KITCHEN, this);

    }

    @Override
    public void setupView() {
        super.setupView();
    }

    @Override
    protected void addViewContent() {
        window.addLabel("Kitchen", 2, 6);
        window.addLabel("Stations", 2, 8);
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
        kitchenStations = window.addTable(2, 3, 100, 10, 5, 10);

        kitchenStations.setColumnLabel(0, "ID");
        kitchenStations.setColumnLabel(1, "Station");
        kitchenStations.setColumnLabel(2, "Backlog");
        kitchenStations.setColumnLabel(3, "Chef");
        kitchenStations.setColumnLabel(4, "In Use");

        for (int i = 0; i < kitchenStations.getColumnCount(); i++) {
            kitchenStations.setColumnWidth(i, 10);
        }
        
    }



    public void onStationUpdate(int stationID, String station, int backlog, String chef, char inUse) {
        StationUpdate update = new StationUpdate(stationID, station, backlog, chef, inUse);
        if (!isInitialized) {
            pendingUpdates.offer(update);
        } else {
            updateStationInTable(stationID, station, backlog, chef, inUse);
        }
    }

    private void updateStationInTable(int stationID, String stationName, int backlog, String chef, char inUse) {
        if (kitchenStations == null || window == null) {
            return;
        }
        
        try {
            // Check if the row exists using station name as the label/index
            if (kitchenStations.getRowLabel(stationID) == null) {
                kitchenStations.insertRowBelow(stationID);
                kitchenStations.setRowLabel(stationID, Integer.toString(stationID));
            }

            kitchenStations.setCellText(0, stationID, Integer.toString(stationID));
            kitchenStations.setCellText(1, stationID, stationName);
            kitchenStations.setCellText(2, stationID, String.valueOf(backlog));
            kitchenStations.setCellText(3, stationID, chef);
            kitchenStations.setCellText(4, stationID, String.valueOf(inUse));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        mediator.unregisterView("Kitchen", this);
    }
}
