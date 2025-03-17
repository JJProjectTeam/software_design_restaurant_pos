package com.softwaredesign.project.controller;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.model.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.model.orderfulfillment.Table;
import com.softwaredesign.project.model.singletons.BankBalanceSingleton;
import com.softwaredesign.project.view.DiningRoomView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;

public class DiningRoomController extends BaseController {
    private SeatingPlan seatingPlan;
    private RestaurantViewMediator mediator;
    private double bankBalance;


    public DiningRoomController(SeatingPlan seatingPlan) {
        super("DiningRoom");
        this.seatingPlan = seatingPlan;
        this.mediator = RestaurantViewMediator.getInstance();
        mediator.registerController("DiningRoom", this);
        this.bankBalance = BankBalanceSingleton.getInstance().getBankBalance();
    }

    @Override
    public void updateView() {
        View view = mediator.getView(ViewType.DINING_ROOM);
        if (!(view instanceof DiningRoomView)) {
            return;
        }

        DiningRoomView diningView = (DiningRoomView) view;

        ((DiningRoomView) view).setBankBalance(BankBalanceSingleton.getInstance().getBankBalance());

        for (Table table : seatingPlan.getAllTables()) {
            int tableNumber = table.getTableNumber();
            int capacity = table.getTableCapacity();
            int customerCount = table.getCustomers().size();
            String status = determineTableStatus(table);

            diningView.onTableUpdate(
                tableNumber,
                capacity,
                customerCount,
                status
            );
        }
    }

    private String determineTableStatus(Table table) {
        if (table.getCustomers().isEmpty()) {
            return "Empty";
        } else if (table.isOrderPlaced()) {
            return "Order Placed";
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
