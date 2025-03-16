package com.softwaredesign.project.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.softwaredesign.project.exceptions.RecipeValidationException;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.order.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DineInCustomer extends Customer {
    private boolean isBrowsing;
    private Recipe selectedRecipe;
    private List<Ingredient> addedIngredients;
    private List<Ingredient> removedIngredients;
    private static final Logger logger = LoggerFactory.getLogger(DineInCustomer.class);

    public DineInCustomer() {
        this.isBrowsing = true;
        this.selectedRecipe = null;
        this.addedIngredients = new ArrayList<>();
        this.removedIngredients = new ArrayList<>();
    }

    @Override
    public Recipe selectRecipeFromMenu(Menu menu) {
        if (isBrowsing) {
            throw new IllegalStateException("Customer is still browsing!");
        }

        boolean validChoice = false;
        while (!validChoice) {
            selectedRecipe = menu.getRandomRecipe();
            try {
                // Validate base recipe ingredients
                // RecipeValidator.getInstance().validateIngredients(selectedRecipe.getIngredients());
                validChoice = true;
            } catch (RecipeValidationException e) {
                logger.info("Sorry, that item is unavailable. Selecting something else...");
            }
        }
        return selectedRecipe;
    }

    @Override
    public void requestRecipeModification(Menu menu) {
        if (selectedRecipe == null) {
            throw new IllegalArgumentException("Can't modify a recipe that hasn't been selected yet!");
        }
        
        Random random = new Random();
        //this could be bad magic numbers, TODO should player set this? Ruan: we could have a global config file for this
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
