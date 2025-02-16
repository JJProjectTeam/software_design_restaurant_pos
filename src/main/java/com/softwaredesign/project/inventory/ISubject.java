package com.softwaredesign.project.inventory;

public interface ISubject {
    void attach(IObserver observer);
    void detach(IObserver observer);
    void notifyObservers(String ingredient, int quantity);
}
