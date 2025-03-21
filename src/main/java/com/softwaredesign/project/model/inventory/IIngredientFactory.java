package com.softwaredesign.project.model.inventory;

import java.util.Dictionary;

public interface IIngredientFactory {
    public Ingredient makeIngredient(String name);
    public Ingredient[] listIngredients();
    public Dictionary<String, Integer> listStock();
}
