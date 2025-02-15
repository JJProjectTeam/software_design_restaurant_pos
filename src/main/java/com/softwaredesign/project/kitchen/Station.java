package com.softwaredesign.project.kitchen;

import com.softwaredesign.project.engine.Entity;

public abstract class Station implements Entity {
    private Recipe currentRecipe;
    private Recipe nextRecipe;

    public Station() {
        StationManager.getInstance().registerStation(this);
    }

    @Override
    public void readState() {
        // During read phase, check if current recipe is done with this station
        if (currentRecipe != null && !currentRecipe.canAssignToStation(this)) {
            nextRecipe = null;  // Release the recipe
        }
    }

    @Override
    public void writeState() {
        // During write phase, update the current recipe
        currentRecipe = nextRecipe;
    }

    public boolean canAcceptRecipe() {
        return currentRecipe == null;
    }

    public void assignRecipe(Recipe recipe) {
        if (canAcceptRecipe() && recipe.canAssignToStation(this)) {
            this.nextRecipe = recipe;
            recipe.assignToStation(this);
        }
    }

    protected Recipe getCurrentRecipe() {
        return currentRecipe;
    }
}
