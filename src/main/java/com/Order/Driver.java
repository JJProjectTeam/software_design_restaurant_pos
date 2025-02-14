package com.Order;

import com.softwaredesign.project.model.Ingredient;
import java.util.Queue;

public class Driver {
    public static void main(String[] args) {
        // 1. Create some ingredients with their required stations

        Ingredient cheese = new Ingredient("Cheese", 0.75);


        Ingredient sauce = new Ingredient("Garlic Sauce", 0.50);

        // 2. Create the OrderManager
        OrderManager orderManager = new OrderManager();

        // 3. Create an order
        Order order = new Order();

        // 4. Add recipes to the order
        BurgerRecipe burger = new BurgerRecipe();
        KebabRecipe kebab = new KebabRecipe();

        order.addRecipe(burger);
        order.addRecipe(kebab);

        // 5. Add some modifications
        order.addModification(burger, cheese, true); // Extra cheese
        order.addModification(kebab, sauce, true); // Extra sauce

        // 6. Submit order to manager
        orderManager.addOrder(order);

        // 7. Demonstrate the station flow for each recipe
        System.out.println("Order Processing Started");
        System.out.println("------------------------");

        for (Recipe recipe : order.getRecipes()) {
            System.out.println("\nProcessing " + recipe.getName());
            System.out.println("Required stations in order:");

            Queue<Station> stations = recipe.getStationsToVisit();
            while (!stations.isEmpty()) {
                Station currentStation = stations.poll();
                System.out.println("- Processing at: " + currentStation);
                // In a real implementation, you might have actual processing here
            }
        }

        System.out.println("\nOrder Processing Completed");
    }
}