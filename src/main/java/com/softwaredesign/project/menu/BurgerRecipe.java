package com.softwaredesign.project.menu;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;

public class BurgerRecipe extends Recipe {
    public BurgerRecipe(InventoryService inventoryService) {
        super("Burger", inventoryService);
    }

    @Override
    protected void initializeBaseIngredients() {
        // Make sure we use the inventoryService passed from the constructor
        ingredients.add(new Ingredient("Beef Patty", inventoryService));
        ingredients.add(new Ingredient("Bun", inventoryService));
        ingredients.add(new Ingredient("Lettuce", inventoryService));
        ingredients.add(new Ingredient("Tomato", inventoryService));
        ingredients.add(new Ingredient("Cheese", inventoryService));
    }
    
    @Override
    protected void initializeTasks() {
        // Add tasks for preparing a burger
        RecipeTask prepTask = new RecipeTask("Prepare burger ingredients", StationType.PREP);
        prepTask.addIngredient(new Ingredient("Lettuce", inventoryService));
        prepTask.addIngredient(new Ingredient("Tomato", inventoryService));
        prepTask.addIngredient(new Ingredient("Cheese", inventoryService));
        tasks.add(prepTask);
        
        RecipeTask grillTask = new RecipeTask("Cook beef patty", StationType.GRILL, 8);
        grillTask.addIngredient(new Ingredient("Beef Patty", inventoryService));
        // The grill task depends on the prep task
        grillTask.addDependency(prepTask);
        tasks.add(grillTask);
        
        RecipeTask plateTask = new RecipeTask("Assemble burger", StationType.PLATE);
        plateTask.addIngredient(new Ingredient("Bun", inventoryService));
        // The plate task depends on both the prep and grill tasks
        plateTask.addDependency(prepTask);
        plateTask.addDependency(grillTask);
        tasks.add(plateTask);
    }
    
    /**
     * Creates a fresh copy of this burger recipe.
     * The copy will have the same inventory service but will initialize its own tasks and ingredients.
     * @return A brand new BurgerRecipe instance without any orderId
     */
    @Override
    public Recipe copy() {
        // Simply create a new BurgerRecipe with the same inventory service
        // The constructor will initialize the ingredients and tasks
        return new BurgerRecipe(inventoryService);
    }
}