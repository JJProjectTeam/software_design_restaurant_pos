package com.softwaredesign.project.observer;

public interface TableUpdateObserver {
    void onTableUpdate(int tableNumber, int capacity, int occupied, String status, char waiterPresent);
}
