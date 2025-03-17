package com.softwaredesign.project.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Ingredient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Notes about the menu class
 * In the next iteration, availableRecipes will be populated from some sort of 'database' or storage
 * Eventually this will either be completely assembled by the user, or we will have premade ones which they can choose from
 * (I think)
 * 
 * getRandomAdditionalIngredient and getRandomIngredientFromRecipe will eventually be replaced with calls to the inventory
 */
public class Menu {
    private static final Logger logger = LoggerFactory.getLogger(Menu.class);
    private final InventoryService inventoryService;
    private List<Recipe> availableRecipes;
    private boolean recipesInitialized = false;

    public Menu(InventoryService inventoryService) {
        if (inventoryService == null) {
            throw new IllegalArgumentException("InventoryService cannot be null");
        }
        this.inventoryService = inventoryService;
        this.availableRecipes = new ArrayList<>();
    }

    // Call this method when gameplay actually starts
    public void initializeRecipes() {
        if (!recipesInitialized) {
            initializeSampleMenu();
            recipesInitialized = true;
        }
    }

    private void initializeSampleMenu() {
        logger.info("[Menu] Initializing sample menu with recipes");
        // Pass inventoryService to recipe constructor
        availableRecipes.add(new BurgerRecipe(inventoryService));
    }
    
    public List<Recipe> getAvailableRecipes() {
        // Make sure recipes are initialized when needed
        if (!recipesInitialized) {
            initializeRecipes();
        }
        return new ArrayList<>(availableRecipes);
    }
    
    public Recipe getRandomRecipe() {
        // Make sure recipes are initialized when needed
        if (!recipesInitialized) {
            initializeRecipes();
        }
        
        Random random = new Random();
        Recipe selectedRecipe = availableRecipes.get(random.nextInt(availableRecipes.size()));
        
        if (!inventoryService.getIngredientStores().containsAll(selectedRecipe.getIngredients())) {
            logger.info("[Menu] Selected recipe cannot be made due to insufficient ingredients. Trying again.");
            return null;
        }
        // Randomly customize the recipe
        return selectedRecipe;
    }

    public Ingredient getRandomAdditionalIngredient() {
        // Get list of available ingredients from inventory
        List<String> availableIngredients = new ArrayList<>();
        for (String ingredient : Arrays.asList("Mustard", "Ketchup", "Onion", "Pickle", "Mayo")) {
            if (inventoryService.getStock(ingredient) > 0) {
                availableIngredients.add(ingredient);
            }
        }
        
        if (availableIngredients.isEmpty()) {
            return null;
        }
        
        // Pick a random available ingredient
        String randomIngredient = availableIngredients.get(new Random().nextInt(availableIngredients.size()));
        return new Ingredient(randomIngredient, inventoryService);
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
        // TODO Placeholder ingredients - will be replaced with actual inventory items
        String[] sampleIngredients = {"Cheese", "Tomato", "Lettuce", "Onion", "Pickles", "Mayo", "Mustard"};
        Random random = new Random();
        return new Ingredient(sampleIngredients[random.nextInt(sampleIngredients.length)], inventoryService);
    }

    public void addRecipe(Recipe recipe) {
        availableRecipes.add(recipe);
    }
}
