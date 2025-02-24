package com.softwaredesign.project.view;

import jexer.TTableWidget;

public class EndOfGameView extends GeneralView{
    private TTableWidget statsTable;

    public EndOfGameView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        window.addLabel("Game Over", 2, 2);
        statsTable = window.addTable(3, 5, 100, 8, 2, 3);
        
        // Set row label width to accommodate longest label
        
        statsTable.setColumnLabel(0, "");
        statsTable.setColumnLabel(1, "Results");


        statsTable.setCellText(0,0, "No Customers Served");
        statsTable.setCellText(0, 1, "No Meals Delivered");
        statsTable.setCellText(0, 2, "Profit ($)");

        statsTable.setShowRowLabels(false);

        statsTable.setColumnWidth(0, 20);
    }
    

}
