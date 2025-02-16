package com.softwaredesign.project.inventory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.softwaredesign.project.Order.Station;

public class IngredientStore {
    private String name;
    private int quantity;
    private double price;
    private Set<Station> stations;

    public IngredientStore(String name, int quantity, double price, Station... stations) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.stations = new HashSet<>(Arrays.asList(stations));
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

    public Set<Station> getStations() {
        return stations;
    }
}
