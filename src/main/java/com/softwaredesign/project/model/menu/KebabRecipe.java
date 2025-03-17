package com.softwaredesign.project.model.menu;

import com.softwaredesign.project.model.inventory.Ingredient;
import com.softwaredesign.project.model.inventory.InventoryService;
import com.softwaredesign.project.model.kitchen.StationType;
import com.softwaredesign.project.model.order.Recipe;
import com.softwaredesign.project.model.order.RecipeTask;

public class KebabRecipe extends Recipe {
    public KebabRecipe(InventoryService inventoryService) {
        super("Kebab", inventoryService);
    }

    @Override
    protected void initializeBaseIngredients() {
        addIngredient(new Ingredient("Kebab Meat", inventoryService));
        addIngredient(new Ingredient("Pita Bread", inventoryService));
        addIngredient(new Ingredient("Onion", inventoryService));
        addIngredient(new Ingredient("Tomato", inventoryService));
        addIngredient(new Ingredient("Garlic Sauce", inventoryService));
    }
    
    @Override
    protected void initializeTasks() {
        // Add tasks for preparing a kebab
        RecipeTask prepTask = new RecipeTask("Prepare kebab ingredients", StationType.PREP);
        prepTask.addIngredient(new Ingredient("Onion", inventoryService));
        prepTask.addIngredient(new Ingredient("Tomato", inventoryService));
        tasks.add(prepTask);
        
        RecipeTask grillTask = new RecipeTask("Cook kebab meat", StationType.GRILL, 10);
        grillTask.addIngredient(new Ingredient("Kebab Meat", inventoryService));
        // The grill task depends on the prep task
        grillTask.addDependency(prepTask);
        tasks.add(grillTask);
        
        RecipeTask plateTask = new RecipeTask("Assemble kebab", StationType.PLATE);
        plateTask.addIngredient(new Ingredient("Pita Bread", inventoryService));
        plateTask.addIngredient(new Ingredient("Garlic Sauce", inventoryService));
        // The plate task depends on both the prep and grill tasks
        plateTask.addDependency(prepTask);
        plateTask.addDependency(grillTask);
        tasks.add(plateTask);
    }
    
    /**
     * Creates a fresh copy of this kebab recipe.
     * The copy will have the same inventory service but will initialize its own tasks and ingredients.
     * @return A brand new KebabRecipe instance without any orderId
     */
    @Override
    public Recipe copy() {
        // Simply create a new KebabRecipe with the same inventory service
        // The constructor will initialize the ingredients and tasks
        return new KebabRecipe(inventoryService);
    }
}