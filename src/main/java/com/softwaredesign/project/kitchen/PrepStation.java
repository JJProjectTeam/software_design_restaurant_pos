package com.softwaredesign.project.kitchen;

public class PrepStation extends Station {
    private Recipe recipeToTransfer;

    @Override
    public void readState() {
        super.readState();
        
        // Check if current recipe is prepped and ready to move
        Recipe currentRecipe = getCurrentRecipe();
        if (currentRecipe != null && currentRecipe.isPrepped()) {
            recipeToTransfer = currentRecipe;
        }
    }

    @Override
    public void writeState() {
        super.writeState();
        
        // Recipe will automatically find its next station through StationManager
        if (recipeToTransfer != null) {
            recipeToTransfer = null;
        }
    }
}
