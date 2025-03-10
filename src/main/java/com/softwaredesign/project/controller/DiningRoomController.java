package com.softwaredesign.project.controller;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.view.DiningRoomView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;
import com.softwaredesign.project.customer.DineInCustomer;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.softwaredesign.project.menu.Menu;

public class DiningRoomController extends BaseController {
    private SeatingPlan seatingPlan;
    private Map<Integer, Character> tableToWaiter;
    private RestaurantViewMediator mediator;

    public DiningRoomController(SeatingPlan seatingPlan) {
        super("DiningRoom");
        this.seatingPlan = seatingPlan;
        this.tableToWaiter = new HashMap<>();
        this.mediator = RestaurantViewMediator.getInstance();
        mediator.registerController("DiningRoom", this);
    }
    @Override
    public void updateView() {
        View view = mediator.getView(ViewType.DINING_ROOM);
        if (!(view instanceof DiningRoomView)) {
            return;
        }

        DiningRoomView diningView = (DiningRoomView) view;
        
        for (Table table : seatingPlan.getAllTables()) {
                diningView.addTable(table.getTableNumber(), table.getTableCapacity(), table.getCustomers(), );

        }

        diningView.updateAllTables();
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

    public SeatingPlan getSeatingPlan() {
        return seatingPlan;
    }
}
