package com.softwaredesign.project.model;

import java.util.Dictionary;
import java.util.Hashtable;

public class Inventory {
    private Dictionary<Ingredient, Integer> stock;

    public Inventory() {
        this.stock = new Hashtable<>();
    }

    public void addIngredient(Ingredient ingredient, int quantity) {
        stock.put(ingredient, quantity);
    }

    public void removeIngredient(Ingredient ingredient) {
        stock.remove(ingredient);
    }

    public int checkStock(Ingredient ingredient) {
        return stock.get(ingredient);
    }

    // not sure if I want to have a single adjust stock, or to have two one for adding and one for removing, I went with two for now
    // due to SRP and clear intent, why do I want something else to remember to put a negative or positive quantity?
    public void addStock(Ingredient ingredient, int quantity) {
        stock.put(ingredient, stock.get(ingredient) + quantity);
    }

    public void removeStock(Ingredient ingredient, int quantity) {
        stock.put(ingredient, stock.get(ingredient) - quantity);
    }



}
