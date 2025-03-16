package com.softwaredesign.project.controller;

import java.util.HashMap;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.model.BankBalanceSingleton;
import com.softwaredesign.project.model.StatisticsSingleton;
import com.softwaredesign.project.view.EndOfGameView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;

public class EndOfGameController extends BaseController {
    public EndOfGameController() {
        super("EndOfGame");
    }

    @Override
    public void updateView() {
        RestaurantViewMediator mediator = RestaurantViewMediator.getInstance();
        View view = mediator.getView(ViewType.END_OF_GAME);
        if (!(view instanceof EndOfGameView)) {
            return;
        }
        EndOfGameView endView = (EndOfGameView) view;
        endView.updateStats(StatisticsSingleton.getInstance().getAllStatsFormatted());
    }
}