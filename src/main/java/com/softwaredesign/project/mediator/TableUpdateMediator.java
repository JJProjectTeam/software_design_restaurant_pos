package com.softwaredesign.project.mediator;

import com.softwaredesign.project.controller.DiningRoomController;
import com.softwaredesign.project.view.DiningRoomView;
import java.util.ArrayList;
import java.util.List;

/**
 * Mediator pattern implementation that handles communication between the DiningRoomController
 * and DiningRoomView. This decouples the components and centralizes their interaction logic.
 */
public class TableUpdateMediator {
    private static TableUpdateMediator instance;
    private DiningRoomController controller;
    private List<DiningRoomView> views;
    
    private TableUpdateMediator() {
        this.views = new ArrayList<>();
    }
    
    public static TableUpdateMediator getInstance() {
        if (instance == null) {
            instance = new TableUpdateMediator();
        }
        return instance;
    }
    
    public void setController(DiningRoomController controller) {
        System.out.println("[TableUpdateMediator] Setting controller");
        this.controller = controller;
    }
    
    public void registerView(DiningRoomView view) {
        System.out.println("[TableUpdateMediator] Registering view");
        if (!views.contains(view)) {
            views.add(view);
            if (controller != null) {
                System.out.println("[TableUpdateMediator] View registered, requesting initial table updates");
                controller.updateAllTableViews();
            }
        }
    }
    
    public void unregisterView(DiningRoomView view) {
        views.remove(view);
    }
    
    public void notifyTableUpdate(int tableNumber, int capacity, int occupied, String status, char waiterPresent) {
        System.out.println("[TableUpdateMediator] Notifying views of update for table " + tableNumber);
        for (DiningRoomView view : views) {
            view.onTableUpdate(tableNumber, capacity, occupied, status, waiterPresent);
        }
    }
}
