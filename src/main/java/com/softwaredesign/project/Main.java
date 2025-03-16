// package com.softwaredesign.project;

// import com.softwaredesign.project.inventory.Ingredient;
// import com.softwaredesign.project.inventory.Inventory;
// import com.softwaredesign.project.inventory.InventoryAlert;
// import com.softwaredesign.project.inventory.InventoryService;
// import com.softwaredesign.project.order.*;
// import com.softwaredesign.project.orderfulfillment.CollectionPoint;
// import com.softwaredesign.project.kitchen.Kitchen;
// import com.softwaredesign.project.kitchen.StationManager;
// import com.softwaredesign.project.menu.BurgerRecipe;
// import com.softwaredesign.project.menu.KebabRecipe;

// import java.util.List;

// public class Main {
//     private static InventoryService setupInventory() {
//         // Create new inventory instance
//         Inventory inventory = new Inventory();

//         // Create and attach the alert with threshold of 5
//         InventoryAlert alert = new InventoryAlert(5);
//         inventory.attach(alert);

//         // Add initial ingredients to inventory
//         logger.info("Initializing Inventory:");
//         logger.info("----------------------");
//         inventory.addIngredient("Tomato", 10, 2.50);
//         inventory.addIngredient("Garlic", 8, 1.00);
//         inventory.addIngredient("Onion", 15, 1.50);
//         inventory.addIngredient("Beef Patty", 20, 5.00);
//         inventory.addIngredient("Bun", 20, 5.00);
//         inventory.addIngredient("Chicken", 20, 4.00);
//         inventory.addIngredient("Lettuce", 25, 1.00);
//         inventory.addIngredient("Cheese", 30, 2.00);
//         inventory.addIngredient("Garlic Sauce", 40, 1.00);
//         inventory.addIngredient("Tomato", 30, 2.00);
//         inventory.addIngredient("Kebab Meat", 50, 5.00);
//         inventory.addIngredient("Pita Bread", 40, 5.00);

//         logger.info();
//         return inventory;
//     }

//     private static void processOrders(InventoryService inventory) {
//         // Create CollectionPoint first
//         CollectionPoint collectionPoint = new CollectionPoint();

//         // Create OrderManager with CollectionPoint
//         StationManager stationManager = new StationManager();
//         OrderManager orderManager = new OrderManager(collectionPoint, stationManager);

//         // Create Kitchen with same CollectionPoint
//         Kitchen kitchen = new Kitchen(orderManager, inventory, collectionPoint);

//         // Test empty kitchen first
//         logger.info("Testing empty kitchen:");
//         kitchen.prepareRecipes(); // No return value needed anymore

//         // Create ingredients for modifications
//         Ingredient cheese = new Ingredient("Cheese", inventory);
//         Ingredient sauce = new Ingredient("Garlic Sauce", inventory);

//         // Create and populate orders
//         logger.info("Creating and processing orders:");
//         logger.info("------------------------------");

//         // Order 1: Burger with extra cheese
//         // TODO: Need to replace this with a factory maybe to make it sequential
//         String orderId1 = orderManager.generateOrderId();
//         Order order1 = new Order(orderId1);
//         BurgerRecipe burger = new BurgerRecipe(inventory);
//         order1.addRecipes(burger);
//         order1.addModification(burger, cheese, true);
//         orderManager.addOrder(order1);

//         // Order 2: Kebab with extra sauce
//         String orderId2 = orderManager.generateOrderId();
//         Order order2 = new Order(orderId2);
//         KebabRecipe kebab = new KebabRecipe(inventory);
//         order2.addRecipes(kebab);
//         order2.addModification(kebab, sauce, true);
//         orderManager.addOrder(order2);

//         // Process orders and prepare meals
//         kitchen.prepareRecipes();

//         // Display prepared meals from collection point
//         logger.info("\nPrepared Meals:");
//         logger.info("---------------");
//         while (collectionPoint.hasReadyOrders()) {
//             List<Meal> completedOrder = collectionPoint.collectNextOrder();
//             for (Meal meal : completedOrder) {
//                 logger.info(meal);
//             }
//         }

//         logger.info("\nOrder Processing Completed");
//     }

//     public static void main(String[] args) {
//         // Step 1: Setup and initialize inventory
//         InventoryService inventory = setupInventory();

//         // Step 2: Process customer orders
//         processOrders(inventory);

//         // Step 3: Display final inventory status
//         logger.info("\nFinal Inventory Status:");
//         logger.info("----------------------");
//         logger.info("Tomato: " + inventory.getStock("Tomato") + " units");
//         logger.info("Garlic: " + inventory.getStock("Garlic") + " units");
//         logger.info("Onion: " + inventory.getStock("Onion") + " units");
//         logger.info("Beef: " + inventory.getStock("Beef Patty") + " units");
//         logger.info("Chicken: " + inventory.getStock("Chicken") + " units");
//         logger.info("Cheese: " + inventory.getStock("Cheese") + " units");
//         logger.info("Garlic Sauce: " + inventory.getStock("Garlic Sauce") + " units");
//     }
// }
