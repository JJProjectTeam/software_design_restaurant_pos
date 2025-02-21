package com.softwaredesign.project.observer;

public interface TableUpdateSubject {
    void addObserver(TableUpdateObserver observer);
    void removeObserver(TableUpdateObserver observer);
    void notifyObservers(int tableNumber, int capacity, int occupied, String status, char waiterPresent);
}
