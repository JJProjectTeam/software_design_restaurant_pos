package com.softwaredesign.project;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryAlert;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.order.*;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.order.CollectionPoint;
import com.softwaredesign.project.kitchen.StationManager;

import java.util.List;

public class Main {
    private static InventoryService setupInventory() {
        // Create new inventory instance
        Inventory inventory = new Inventory();

        // Create and attach the alert with threshold of 5
        InventoryAlert alert = new InventoryAlert(5);
        inventory.attach(alert);

        // Add initial ingredients to inventory
        System.out.println("Initializing Inventory:");
        System.out.println("----------------------");
        inventory.addIngredient("Tomatoes", 10, 2.50);
        inventory.addIngredient("Garlic", 8, 1.00);
        inventory.addIngredient("Onions", 15, 1.50);
        inventory.addIngredient("Beef Patty", 20, 5.00);
        inventory.addIngredient("Bun", 20, 5.00);
        inventory.addIngredient("Chicken", 20, 4.00);
        inventory.addIngredient("Lettuce", 25, 1.00);
        inventory.addIngredient("Cheese", 30, 2.00);
        inventory.addIngredient("Garlic Sauce", 40, 1.00);
        inventory.addIngredient("Tomato", 30, 2.00);
        inventory.addIngredient("Kebab Meat", 50, 5.00);
        inventory.addIngredient("Pita Bread", 40, 5.00);

        System.out.println();
        return inventory;
    }

    private static void processOrders(InventoryService inventory) {
        // Create CollectionPoint first
        CollectionPoint collectionPoint = new CollectionPoint();

        // Create OrderManager with CollectionPoint
        StationManager stationManager = new StationManager();
        OrderManager orderManager = new OrderManager(collectionPoint, stationManager);

        // Create Kitchen with same CollectionPoint
        Kitchen kitchen = new Kitchen(orderManager, inventory, collectionPoint);

        // Test empty kitchen first
        System.out.println("Testing empty kitchen:");
        kitchen.prepareRecipes(); // No return value needed anymore

        // Create ingredients for modifications
        Ingredient cheese = new Ingredient("Cheese", inventory);
        Ingredient sauce = new Ingredient("Garlic Sauce", inventory);

        // Create and populate orders
        System.out.println("Creating and processing orders:");
        System.out.println("------------------------------");

        // Order 1: Burger with extra cheese
        String orderId1 = orderManager.generateOrderId();
        Order order1 = new Order(orderId1);
        BurgerRecipe burger = new BurgerRecipe(inventory);
        order1.addRecipes(burger);
        order1.addModification(burger, cheese, true);
        orderManager.addOrder(order1);

        // Order 2: Kebab with extra sauce
        String orderId2 = orderManager.generateOrderId();
        Order order2 = new Order(orderId2);
        KebabRecipe kebab = new KebabRecipe(inventory);
        order2.addRecipes(kebab);
        order2.addModification(kebab, sauce, true);
        orderManager.addOrder(order2);

        // Process orders and prepare meals
        kitchen.prepareRecipes();

        // Display prepared meals from collection point
        System.out.println("\nPrepared Meals:");
        System.out.println("---------------");
        while (collectionPoint.hasReadyOrders()) {
            List<Meal> completedOrder = collectionPoint.collectNextOrder();
            for (Meal meal : completedOrder) {
                System.out.println(meal);
            }
        }

        System.out.println("\nOrder Processing Completed");
    }

    public static void main(String[] args) {
        // Step 1: Setup and initialize inventory
        InventoryService inventory = setupInventory();

        // Step 2: Process customer orders
        processOrders(inventory);

        // Step 3: Display final inventory status
        System.out.println("\nFinal Inventory Status:");
        System.out.println("----------------------");
        System.out.println("Tomatoes: " + inventory.getStock("Tomatoes") + " units");
        System.out.println("Garlic: " + inventory.getStock("Garlic") + " units");
        System.out.println("Onions: " + inventory.getStock("Onions") + " units");
        System.out.println("Beef: " + inventory.getStock("Beef Patty") + " units");
        System.out.println("Chicken: " + inventory.getStock("Chicken") + " units");
        System.out.println("Cheese: " + inventory.getStock("Cheese") + " units");
        System.out.println("Garlic Sauce: " + inventory.getStock("Garlic Sauce") + " units");
    }
}
