package com.softwaredesign.project.kitchen;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.staff.Chef;

public class Station extends Entity {
    private final StationType type;
    private List<Order> backlog;
    private Chef assignedChef;
    private Recipe currentRecipe;
    private int cookingProgress;
    private static final int COOKING_TIME = 5; // 5 ticks to complete cooking
    private CollectionPoint collectionPoint;
    private boolean needsIngredients;

    public Station(StationType type, CollectionPoint collectionPoint) {
        this.type = type;
        this.backlog = new ArrayList<>();
        this.collectionPoint = collectionPoint;
        this.cookingProgress = 0;
        this.needsIngredients = false;
    }

    public StationType getType() {
        return type;
    }

    public void addOrder(Order order) {
        backlog.add(order);
    }

    public LocalDateTime getOldestOrderTime() {
        return LocalDateTime.now(); //TODO order must be given an id either on creation or when added to stations
    }

    // get orders from station
    public List<Order> getBacklog() {
        return backlog;
    }

    public int getBacklogSize() {
        return backlog.size();
    }

    public void registerChef(Chef chef) {
        assignedChef = chef;
    }

    public void unregisterChef() {
        assignedChef = null;
    }

    public Chef getAssignedChef() {
        return assignedChef;
    }

    public boolean hasChef() {
        return assignedChef != null;
    }

    public void assignRecipe(Recipe recipe) {
        if (currentRecipe == null) {
            currentRecipe = recipe;
            cookingProgress = 0;
            needsIngredients = true;
            System.out.println(type + " station assigned recipe: " + recipe.getName());
        } else {
            System.out.println(type + " station is busy with another recipe");
        }
    }

    public boolean isBusy() {
        return currentRecipe != null;
    }

    public boolean needsIngredients() {
        return needsIngredients;
    }

    public void provideIngredients() {
        if (currentRecipe != null && needsIngredients) {
            needsIngredients = false;
            System.out.println(type + " station received ingredients for " + currentRecipe.getName());
        }
    }

    @Override
    public void readState() {
        // In read state, we check if we can cook
        if (currentRecipe != null && hasChef() && !needsIngredients) {
            // Ready to progress cooking
        }
    }

    @Override
    public void writeState() {
        // In write state, we update cooking progress
        if (currentRecipe != null && hasChef() && !needsIngredients) {
            cookingProgress++;
            System.out.println(type + " station cooking " + currentRecipe.getName() + 
                              " - Progress: " + cookingProgress + "/" + COOKING_TIME);
            
            if (cookingProgress >= COOKING_TIME) {
                // Recipe is done
                System.out.println(type + " station completed cooking " + currentRecipe.getName());
                collectionPoint.addCompletedMeal(currentRecipe.buildMeal());
                currentRecipe = null;
                cookingProgress = 0;
            }
        }
    }

    public Recipe getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public String toString() {
        return type.toString() + " Station";
    }
}
