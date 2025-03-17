package com.softwaredesign.project.model.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.softwaredesign.project.model.exceptions.RecipeValidationException;
import com.softwaredesign.project.model.inventory.Ingredient;
import com.softwaredesign.project.model.menu.Menu;
import com.softwaredesign.project.model.order.Meal;
import com.softwaredesign.project.model.order.Recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DineInCustomer extends Customer {
    private boolean isBrowsing;
    private Recipe selectedRecipe;
    private List<Ingredient> addedIngredients;
    private List<Ingredient> removedIngredients;
    private boolean hasEaten;
    private int satisfaction;
    private static final Logger logger = LoggerFactory.getLogger(DineInCustomer.class);

    public DineInCustomer() {
        this.isBrowsing = true;
        this.selectedRecipe = null;
        this.addedIngredients = new ArrayList<>();
        this.removedIngredients = new ArrayList<>();
        this.hasEaten = false;
        this.satisfaction = 50; // Default satisfaction level (0-100)
    }

    @Override
    public Recipe selectRecipeFromMenu(Menu menu) {
        if (isBrowsing) {
            throw new IllegalStateException("Customer is still browsing!");
        }

            selectedRecipe = menu.getRandomRecipe();
        return selectedRecipe;
    }

    @Override
    public void requestRecipeModification(Menu menu) {
        if (selectedRecipe == null) {
            logger.info("Customer has not selected a recipe yet!");
            return;    
        }
        
        Random random = new Random();
        int numberOfModifications = random.nextInt(4); // 0-3 modifications
        
        
        for (int i = 0; i < numberOfModifications; i++) {
            // 50/50 chance of if the choice is to remove or add ingredient
            if (random.nextBoolean()) {
                Ingredient addIngredient = menu.getRandomAdditionalIngredient();
                if (addIngredient != null) {
                    try {
                        // Validate single new ingredient
                        // RecipeValidator.getInstance().validateIngredients(List.of(addIngredient));
                        addedIngredients.add(addIngredient);
                    } catch (RecipeValidationException e) {
                        logger.info("Can't add " + addIngredient.getName() + ". Skipping...");
                    }
                }
            } else {
                Ingredient removeIngredient = menu.getRandomIngredientFromRecipe(selectedRecipe);
                if (removeIngredient != null && !selectedRecipe.getIngredients().contains(removeIngredient)) {
                    removedIngredients.add(removeIngredient);
                }
            }
        }
        
    }

    public void requestRecipeModification(Recipe recipe, Menu menu) {
        Ingredient additionalIngredient = menu.getRandomAdditionalIngredient();
        if (additionalIngredient != null) {
            recipe.addIngredient(additionalIngredient);
            logger.info("Customer requested additional " + additionalIngredient.getName());
        }
    }
    
    /**
     * Customer eats a meal that has been delivered to their table.
     * @param meal The meal to eat
     */
    public void eatMeal(Meal meal) {
        // Increment satisfaction based on the meal
        int satisfactionChange = calculateSatisfaction(meal);
        satisfaction = Math.min(100, Math.max(0, satisfaction + satisfactionChange));
        
        System.out.println("Customer ate a meal. Satisfaction: " + satisfaction + "/100");
        
        hasEaten = true;
    }
    
    /**
     * Calculate satisfaction change from eating a meal.
     * In a real system, this would be more complex and consider many factors.
     * @param meal The meal being eaten
     * @return The change in satisfaction (positive or negative)
     */
    private int calculateSatisfaction(Meal meal) {
        // For simplicity, we'll just return a random positive value
        // In a real system, this would consider if the meal matches what they ordered,
        // if it was delivered promptly, quality, etc.
        return new Random().nextInt(20) + 10; // Random satisfaction boost between 10-30
    }
    
    /**
     * Checks if this customer has eaten.
     * @return true if the customer has eaten, false otherwise
     */
    public boolean hasEaten() {
        return hasEaten;
    }
    
    /**
     * Gets the customer's current satisfaction level.
     * @return The satisfaction level (0-100)
     */
    public int getSatisfaction() {
        return satisfaction;
    }

    public List<Ingredient> getAddedIngredients() {
        return addedIngredients;
    }

    public List<Ingredient> getRemovedIngredients() {
        return removedIngredients;
    }

    public Recipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void finishBrowsing() {
        this.isBrowsing = false;
    }

    public boolean isDoneBrowsing() {
        return !isBrowsing;
    }
}
