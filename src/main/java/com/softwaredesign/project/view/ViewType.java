package com.softwaredesign.project.view;

public enum ViewType {
    WELCOME(WelcomeView.class),
    CONFIGURATION(ConfigurationView.class),
    KITCHEN(KitchenView.class),
    DINING_ROOM(DiningRoomView.class),
    END_OF_GAME(EndOfGameView.class),
    INVENTORY(InventoryView.class);

    private final Class<? extends View> viewClass;

    ViewType(Class<? extends View> viewClass) {
        this.viewClass = viewClass;
    }

    public Class<? extends View> getViewClass() {
        return viewClass;
    }
}