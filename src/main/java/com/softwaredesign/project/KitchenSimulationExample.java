package com.softwaredesign.project;

import com.softwaredesign.project.engine.GameEngine;
import com.softwaredesign.project.engine.KitchenSimulator;
import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.menu.BurgerRecipe;
import com.softwaredesign.project.menu.KebabRecipe;
import com.softwaredesign.project.order.Order;

/**
 * Example class that demonstrates the kitchen simulation with the game engine.
 */
public class KitchenSimulationExample {

    public static void main(String[] args) {
        // Create inventory service
        InventoryService inventory = setupInventory(); 
        
        // Create the kitchen simulator
        KitchenSimulator simulator = new KitchenSimulator(); // huh? 
        
        // Create and add orders 
        createOrders(simulator, inventory);
        
        // Run the simulation for a number of steps
        System.out.println("\nStarting Kitchen Simulation:");
        System.out.println("---------------------------");
        
        // Start the engine
        simulator.start();
        
        // Run for 20 steps (enough to complete some recipes)
        for (int i = 0; i < 20; i++) {
            System.out.println("\n=== STEP " + (i+1) + " ===");
            simulator.step();
            
            // Add a small delay to make output readable
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Stop the engine
        simulator.stop();
        
        System.out.println("\nKitchen Simulation Completed");
    }
    
    private static InventoryService setupInventory() {
        // Create new inventory instance
        Inventory inventory = new Inventory();

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
    
    private static void createOrders(KitchenSimulator simulator, InventoryService inventory) {
        System.out.println("Creating Orders:");
        System.out.println("---------------");
        
        // Create ingredients for modifications
        Ingredient cheese = new Ingredient("Cheese", inventory);
        Ingredient sauce = new Ingredient("Garlic Sauce", inventory);
        
        // Order 1: Burger with extra cheese
        String orderId1 = simulator.getOrderManager().generateOrderId(); // wild...
        Order order1 = new Order(orderId1);
        BurgerRecipe burger = new BurgerRecipe(inventory);
        order1.addRecipes(burger);
        order1.addModification(burger, cheese, true);
        simulator.getOrderManager().addOrder(order1);
        System.out.println("Added Order 1: Burger with extra cheese");
        
        // Order 2: Kebab with extra sauce
        String orderId2 = simulator.getOrderManager().generateOrderId();
        Order order2 = new Order(orderId2);
        KebabRecipe kebab = new KebabRecipe(inventory);
        order2.addRecipes(kebab);
        order2.addModification(kebab, sauce, true);
        simulator.getOrderManager().addOrder(order2);
        System.out.println("Added Order 2: Kebab with extra sauce");
    }
}
