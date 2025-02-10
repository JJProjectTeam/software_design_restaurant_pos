package com.softwaredesign.project.model.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventory implements ISubject {
    private final Map<String, Ingredient> ingredients;
    private final List<IObserver> observers;

    public Inventory() {
        this.ingredients = new HashMap<>();
        this.observers = new ArrayList<>();
    }

    @Override
    public void attach(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(IObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String ingredient, int quantity) {
        for (IObserver observer : observers) {
            observer.update(ingredient, quantity);
        }
    }

    public void addIngredient(String name, int quantity, double price) {
        ingredients.put(name, new Ingredient(name, quantity, price));
        notifyObservers(name, quantity);
    }

    public void useIngredient(String name, int amount) throws IllegalArgumentException {
        Ingredient ingredient = ingredients.get(name);
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient " + name + " not found in inventory");
        }

        if (ingredient.getQuantity() < amount) {
            throw new IllegalArgumentException("Not enough " + name + " in stock");
        }

        ingredient.setQuantity(ingredient.getQuantity() - amount);
        notifyObservers(name, ingredient.getQuantity());
    }

    public void updateInventory(Map<String, Integer> ingredientUpdates) throws IllegalArgumentException {
        for (Map.Entry<String, Integer> entry : ingredientUpdates.entrySet()) {
            String name = entry.getKey();
            int amount = entry.getValue();
            
            Ingredient ingredient = ingredients.get(name);
            if (ingredient == null) {
                throw new IllegalArgumentException("Ingredient " + name + " not found in inventory");
            }

            ingredient.setQuantity(amount);
            notifyObservers(name, amount);
        }
    }

    public int getStock(String name) {
        Ingredient ingredient = ingredients.get(name);
        return ingredient != null ? ingredient.getQuantity() : 0;
    }

    public double getPrice(String name) {
        Ingredient ingredient = ingredients.get(name);
        return ingredient != null ? ingredient.getPrice() : 0.0;
    }
}
