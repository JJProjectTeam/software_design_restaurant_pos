package com.softwaredesign.project.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.softwaredesign.project.order.BurgerRecipe;
import com.softwaredesign.project.order.KebabRecipe;

import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Ingredient;

/*
 * Notes about the menu class
 * In the next iteration, availableRecipes will be populated from some sort of 'database' or storage
 * Eventually this will either be completely assembled by the user, or we will have premade ones which they can choose from
 * (I think)
 * 
 * getRandomAdditionalIngredient and getRandomIngredientFromRecipe will eventually be replaced with calls to the inventory
 * 
 * 
 * 
 * 
 */
public class Menu {
    private List<Recipe> availableRecipes;
    
    public Menu() {
        availableRecipes = new ArrayList<>();
        initializeSampleMenu();
    }
    
    // TODO: I'm not familiar with the inventory service, so I'm not sure if this is correct. Maybe you should pass in the inventory service to the menu constructor?
    private InventoryService inventoryService;

    private void initializeSampleMenu() {
        // Placeholder recipes for testing
        availableRecipes.add(new BurgerRecipe(inventoryService));
        availableRecipes.add(new KebabRecipe(inventoryService)); 
    }

    
    public List<Recipe> getAvailableRecipes() {
        return new ArrayList<>(availableRecipes);
    }
    
    public Recipe getRandomRecipe() {
        Random random = new Random();
        Recipe selectedRecipe = availableRecipes.get(random.nextInt(availableRecipes.size()));
        
        // Randomly customize the recipe
        return selectedRecipe;
    }

    public Ingredient getRandomAdditionalIngredient() {
        // TODO: This should eventually check Inventory for available ingredients
        return getRandomIngredient();
    }

    public Ingredient getRandomIngredientFromRecipe(Recipe recipe) {
        if (recipe == null || recipe.getIngredients().isEmpty()) {
            return null;
        }

        Random random = new Random();
        List<Ingredient> recipeIngredients = recipe.getIngredients();
        return recipeIngredients.get(random.nextInt(recipeIngredients.size()));
    }

    private Ingredient getRandomIngredient() {
        // Placeholder ingredients - will be replaced with actual inventory items
        String[] sampleIngredients = {"Cheese", "Tomato", "Lettuce", "Onion", "Pickles", "Mayo", "Mustard"};
        Random random = new Random();
        return new Ingredient(sampleIngredients[random.nextInt(sampleIngredients.length)], inventoryService);
    }
}
