package com.softwaredesign.project.inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.softwaredesign.project.kitchen.StationType;

public class IngredientStore {
    private String name;
    private int quantity;
    private double price;
    private Set<StationType> stationTypes;

    public IngredientStore(String name, int quantity, double price, StationType... stationTypes) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.stationTypes = new HashSet<>(Arrays.asList(stationTypes));
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Set<StationType> getStationTypes() {
        return Collections.unmodifiableSet(stationTypes);
    }
}
