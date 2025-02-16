package com.softwaredesign.project;

import com.softwaredesign.project.Kitchen.Kitchen;
import com.softwaredesign.project.Order.BurgerRecipe;
import com.softwaredesign.project.Order.KebabRecipe;
import com.softwaredesign.project.Order.Meal;
import com.softwaredesign.project.Order.Order;
import com.softwaredesign.project.Order.OrderManager;
import com.softwaredesign.project.inventory.Ingredient;

import java.util.List;

public class Driver {
    public static void main(String[] args) {
        // 1. Create OrderManager and Kitchen
        OrderManager orderManager = new OrderManager();
        Kitchen kitchen = new Kitchen(orderManager);

        // Test empty kitchen first
        System.out.println("Testing empty kitchen:");
        List<Meal> emptyMeals = kitchen.prepareRecipes();
        if (emptyMeals == null) {
            System.out.println("No meals to prepare - kitchen is empty\n");
        }

        // 2. Create ingredients for modifications
        Ingredient cheese = new Ingredient("Cheese");
        Ingredient sauce = new Ingredient("Garlic Sauce");

        // 3. Create and populate order
        Order order = new Order();
        BurgerRecipe burger = new BurgerRecipe();
        KebabRecipe kebab = new KebabRecipe();

        order.addRecipes(burger);
        order.addRecipes(kebab);

        // 4. Add modifications
        order.addModification(burger, cheese, true); // Extra cheese
        order.addModification(kebab, sauce, true); // Extra sauce

        // 5. Submit order to manager
        orderManager.addOrder(order);

        // 6. Process order and prepare meals
        System.out.println("Order Processing Started");
        System.out.println("------------------------");
        List<Meal> preparedMeals = kitchen.prepareRecipes();
        
        // 7. Display prepared meals and their ingredients
        if (preparedMeals != null) {
            System.out.println("\nPrepared Meals:");
            System.out.println("---------------");
            for (Meal meal : preparedMeals) {
                System.out.println(meal);
            }
        }

        System.out.println("\nOrder Processing Completed");
    }
}