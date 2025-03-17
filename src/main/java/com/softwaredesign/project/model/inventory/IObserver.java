package com.softwaredesign.project.model.inventory;

public interface IObserver {
    void update(String ingredient, int quantity);
}
