package com.softwaredesign.project.model.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.softwaredesign.project.model.placeholders.ConcreteRecipe;
import com.softwaredesign.project.model.placeholders.Recipe;
import com.softwaredesign.project.model.placeholders.Ingredient;


public class Menu {
    private List<Recipe> availableRecipes;
    
    public Menu() {
        availableRecipes = new ArrayList<>();
        initializeSampleMenu();
    }
    
    private void initializeSampleMenu() {
        // Placeholder recipes for testing
        availableRecipes.add(new ConcreteRecipe());
        availableRecipes.add(new ConcreteRecipe());
        availableRecipes.add(new ConcreteRecipe());
        availableRecipes.add(new ConcreteRecipe());
    }

    
    public List<Recipe> getAvailableRecipes() {
        return new ArrayList<>(availableRecipes);
    }
    
    public Recipe getRandomRecipe() {
        Random random = new Random();
        Recipe selectedRecipe = availableRecipes.get(random.nextInt(availableRecipes.size()));
        
        // Randomly customize the recipe
        customizeRecipe(selectedRecipe);
        
        // TODO: Implement Interceptor Pattern
        // RecipeValidator.getInstance().validateRecipe(selectedRecipe);
        // This will check if recipe with customizations can be made with current inventory
        
        return selectedRecipe;
    }

    private void customizeRecipe(Recipe recipe) {
        Random random = new Random();
        int numberOfModifications = random.nextInt(4); // 0 to 3 modifications

        for (int i = 0; i < numberOfModifications; i++) {
            boolean isAddition = random.nextBoolean();
            Ingredient randomIngredient = getRandomIngredient();

            if (isAddition) {
                recipe.addIngredient(randomIngredient);
            } else {
                recipe.removeIngredient(randomIngredient);
            }
        }
    }

    private Ingredient getRandomIngredient() {
        // Placeholder ingredients - will be replaced with actual inventory items
        String[] sampleIngredients = {"Cheese", "Tomato", "Lettuce", "Onion", "Pickles", "Mayo", "Mustard"};
        Random random = new Random();
        return new Ingredient(sampleIngredients[random.nextInt(sampleIngredients.length)]);
    }
}
