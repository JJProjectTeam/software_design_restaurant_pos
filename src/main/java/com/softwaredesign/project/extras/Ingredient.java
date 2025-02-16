package com.softwaredesign.project.extras;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.softwaredesign.project.Order.Station;

public class Ingredient {
    private String name;
    private double price;
    private Set<Station> stations;

    public Ingredient(String name, double price, Station... stations) {
        this.name = name;
        this.price = price;
        this.stations = new HashSet<>(Arrays.asList(stations));
    }

    public String getName() {
        return name;
    }

    public Set<Station> getStations() {
        return stations;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name;
    }
}
