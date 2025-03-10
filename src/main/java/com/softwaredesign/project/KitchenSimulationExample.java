package com.softwaredesign.project;

import com.softwaredesign.project.inventory.ConcreteIngredientFactory;
import com.softwaredesign.project.inventory.IIngredientFactory;
import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.engine.KitchenSimulator;
import com.softwaredesign.project.gui.KitchenSimulationGUI;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.menu.BurgerRecipe;
import com.softwaredesign.project.menu.KebabRecipe;
import com.softwaredesign.project.order.Order;

public class KitchenSimulationExample {

    // GUI reference for the application
    private static KitchenSimulationGUI gui;
    
    public static void main(String[] args) {
        // Create inventory service
        InventoryService inventory = setupInventory(); 
        
        // Create ingredient factory
        IIngredientFactory ingredientFactory = new ConcreteIngredientFactory(inventory);
        
        // Create the kitchen simulator
        KitchenSimulator simulator = new KitchenSimulator();
        
        // Create and show the GUI
        gui = new KitchenSimulationGUI(simulator);
        gui.setVisible(true);
        
        // Allow GUI to fully initialize
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
        
        // Create and add orders 
        createOrders(simulator, inventory, ingredientFactory);
        
        // Run the simulation for a number of steps
        System.out.println("\nStarting Kitchen Simulation:");
        System.out.println("---------------------------");
        
        // Start the engine
        simulator.start();
        
        // Step manually once to initialize the simulation state
        System.out.println("\n=== INITIAL STEP ===\n");
        simulator.step();
        
        // Add more orders after a few button clicks
        System.out.println("\n*** USE THE STEP BUTTON IN THE GUI TO ADVANCE THE SIMULATION ***\n");
        System.out.println("*** ADDITIONAL ORDERS WILL BE ADDED AFTER 15 STEPS ***\n");
        
        // Create a thread to monitor steps and add more orders
        new Thread(() -> {
            int stepCount = 1;
            while (true) {
                try {
                    Thread.sleep(1000); // Check every second
                    if (simulator.getStepCount() >= 15 && stepCount < 15) {
                        System.out.println("\n*** ADDING MORE ORDERS MIDWAY ***");
                        createAdditionalOrders(simulator, inventory, ingredientFactory);
                    }
                    stepCount = simulator.getStepCount();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        
        // Note: We're not stopping the engine since we want to use the step button
        // The engine will continue running until the application is closed
        
        System.out.println("\nKitchen Simulation Completed");
    }
    
    private static InventoryService setupInventory() {
        // Create new inventory instance
        Inventory inventory = new Inventory();

        // Add initial ingredients to inventory with their station types
        System.out.println("Initializing Inventory:");
        System.out.println("----------------------");
        inventory.addIngredient("Tomato", 100, 2.50, StationType.PREP);
        inventory.addIngredient("Garlic", 80, 1.00, StationType.PREP);
        inventory.addIngredient("Onion", 150, 1.50, StationType.PREP);
        inventory.addIngredient("Beef Patty", 200, 5.00, StationType.GRILL);
        inventory.addIngredient("Bun", 200, 5.00, StationType.PLATE);
        inventory.addIngredient("Chicken", 200, 4.00, StationType.GRILL);
        inventory.addIngredient("Lettuce", 250, 1.00, StationType.PREP, StationType.PLATE);
        inventory.addIngredient("Cheese", 300, 2.00, StationType.PLATE);
        inventory.addIngredient("Garlic Sauce", 400, 1.00, StationType.PLATE);
        inventory.addIngredient("Tomato", 300, 2.00, StationType.PREP, StationType.PLATE);
        inventory.addIngredient("Kebab Meat", 500, 5.00, StationType.GRILL);
        inventory.addIngredient("Pita Bread", 400, 5.00, StationType.PLATE);

        System.out.println();
        return inventory;
    }
    
    private static void createOrders(KitchenSimulator simulator, InventoryService inventory, IIngredientFactory ingredientFactory) {
        System.out.println("Creating Initial Orders:");
        System.out.println("----------------------");
        
        // Create ingredients for modifications using the factory
        Ingredient cheese = ingredientFactory.makeIngredient("Cheese");
        Ingredient sauce = ingredientFactory.makeIngredient("Garlic Sauce");
        Ingredient lettuce = ingredientFactory.makeIngredient("Lettuce");
        Ingredient tomato = ingredientFactory.makeIngredient("Tomato");
        
        // Order 1: Burger with extra cheese
        String orderId1 = simulator.getOrderManager().generateOrderId();
        Order order1 = new Order(orderId1);
        BurgerRecipe burger = new BurgerRecipe(inventory);
        order1.addRecipes(burger);
        order1.addModification(burger, cheese, true);
        simulator.getOrderManager().addOrder(order1);
        System.out.println("Added " + orderId1 + ": Burger with extra cheese");
        
        // Order 2: Kebab with extra sauce
        String orderId2 = simulator.getOrderManager().generateOrderId();
        Order order2 = new Order(orderId2);
        KebabRecipe kebab = new KebabRecipe(inventory);
        order2.addRecipes(kebab);
        order2.addModification(kebab, sauce, true);
        simulator.getOrderManager().addOrder(order2);
        System.out.println("Added " + orderId2 + ": Kebab with extra sauce");
        
        // Order 3: Complex order with both kebab and burger
        String orderId3 = simulator.getOrderManager().generateOrderId();
        Order order3 = new Order(orderId3);
        BurgerRecipe burger2 = new BurgerRecipe(inventory);
        KebabRecipe kebab2 = new KebabRecipe(inventory);
        order3.addRecipes(burger2, kebab2);
        order3.addModification(burger2, lettuce, true);
        order3.addModification(kebab2, tomato, true);
        simulator.getOrderManager().addOrder(order3);
        System.out.println("Added " + orderId3 + ": Complex order with Burger (extra lettuce) and Kebab (extra tomato)");
        
        // Order 4: Another kebab with no modifications
        String orderId4 = simulator.getOrderManager().generateOrderId();
        Order order4 = new Order(orderId4);
        KebabRecipe kebab3 = new KebabRecipe(inventory);
        order4.addRecipes(kebab3);
        simulator.getOrderManager().addOrder(order4);
        System.out.println("Added " + orderId4 + ": Kebab (no modifications)");
        
        // Order 5: Another burger with no modifications
        String orderId5 = simulator.getOrderManager().generateOrderId();
        Order order5 = new Order(orderId5);
        BurgerRecipe burger3 = new BurgerRecipe(inventory);
        order5.addRecipes(burger3);
        simulator.getOrderManager().addOrder(order5);
        System.out.println("Added " + orderId5 + ": Burger (no modifications)");
        
        // Print current stock levels
        System.out.println("\nCurrent Stock Levels:");
        System.out.println("--------------------");
        ingredientFactory.listStock().keys().asIterator().forEachRemaining(key -> {
            System.out.println(key + ": " + ingredientFactory.listStock().get(key));
        });
    }
    
    private static void createAdditionalOrders(KitchenSimulator simulator, InventoryService inventory, IIngredientFactory ingredientFactory) {
        System.out.println("Creating Additional Orders:");
        System.out.println("-------------------------");
        
        // Create ingredients for modifications
        Ingredient cheese = ingredientFactory.makeIngredient("Cheese");
        Ingredient sauce = ingredientFactory.makeIngredient("Garlic Sauce");
        
        // Add several more orders to test queue management strategies
        for (int i = 1; i <= 6; i++) {
            String orderId = simulator.getOrderManager().generateOrderId();
            Order order = new Order(orderId);
            
            // Alternate between burger and kebab recipes
            if (i % 2 == 0) {
                BurgerRecipe burger = new BurgerRecipe(inventory);
                order.addRecipes(burger);
                if (i % 4 == 0) {
                    order.addModification(burger, cheese, true);
                    System.out.println("Added " + orderId + ": Burger with extra cheese");
                } else {
                    System.out.println("Added " + orderId + ": Burger");
                }
            } else {
                KebabRecipe kebab = new KebabRecipe(inventory);
                order.addRecipes(kebab);
                if (i % 3 == 0) {
                    order.addModification(kebab, sauce, true);
                    System.out.println("Added " + orderId + ": Kebab with extra sauce");
                } else {
                    System.out.println("Added " + orderId + ": Kebab");
                }
            }
            
            simulator.getOrderManager().addOrder(order);
        }
    }
}
