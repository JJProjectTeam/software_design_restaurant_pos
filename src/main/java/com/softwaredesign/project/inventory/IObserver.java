package com.softwaredesign.project.inventory;

public interface IObserver {
    void update(String ingredient, int quantity);
}
