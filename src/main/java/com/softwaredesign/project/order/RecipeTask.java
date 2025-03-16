package com.softwaredesign.project.order;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.kitchen.StationType;

/**
 * Represents a specific task within a recipe that needs to be performed at a particular station.
 */
public class RecipeTask {
    private String name;
    private StationType stationType;
    private List<Ingredient> ingredients;
    private boolean completed;
    private static final int DEFAULT_COOKING_TIME = 5;
    private int cookingTime;
    private Set<RecipeTask> dependencies; // Tasks that must be completed before this task can start
    private boolean assigned; // Tracks if this task has been assigned to a station
    private Recipe recipe; // Reference to the parent recipe this task belongs to
    
    public RecipeTask(String name, StationType stationType) {
        this.name = name;
        this.stationType = stationType;
        this.ingredients = new ArrayList<>();
        this.completed = false;
        this.assigned = false;
        this.cookingTime = DEFAULT_COOKING_TIME;
        this.dependencies = new HashSet<>();
    }
    
    public RecipeTask(String name, StationType stationType, int cookingTime) {
        this(name, stationType);
        this.cookingTime = cookingTime;
    }
    
    /**
     * Adds a dependency - this task cannot start until the given task is completed
     * @param task The task that must be completed before this task can begin
     */
    public void addDependency(RecipeTask task) {
        dependencies.add(task);
    }
    
    /**
     * Checks if all dependencies have been completed
     * @return true if all dependencies are completed or if there are no dependencies
     */
    public boolean areDependenciesMet() {
        if (dependencies.isEmpty()) {
            return true; // No dependencies, so they're met by default
        }
        
        for (RecipeTask dependency : dependencies) {
            if (!dependency.isCompleted()) {
                return false; // At least one dependency is not completed
            }
        }
        
        return true; // All dependencies are completed
    }
    
    /**
     * Gets a string listing all incomplete dependencies
     * @return A string listing the names of incomplete dependencies, or empty string if none
     */
    public String getUnmetDependenciesString() {
        if (dependencies.isEmpty()) {
            return ""; // No dependencies
        }
        
        List<String> unmetDependencies = new ArrayList<>();
        for (RecipeTask dependency : dependencies) {
            if (!dependency.isCompleted()) {
                unmetDependencies.add(dependency.getName());
            }
        }
        
        return String.join(", ", unmetDependencies);
    }
    
    /**
     * Gets the set of task dependencies
     * @return The set of tasks this task depends on
     */
    public Set<RecipeTask> getDependencies() {
        return new HashSet<>(dependencies); // Return a copy to prevent external modification
    }
    
    /**
     * Checks and updates the status of dependencies
     * Call this after another task completes to ensure dependency status is updated
     */
    public void updateDependenciesStatus() {
        if (dependencies.isEmpty()) {
            return; // No dependencies to update
        }
        
        boolean allMet = true;
        for (RecipeTask dependency : dependencies) {
            if (!dependency.isCompleted()) {
                allMet = false;
                break;
            }
        }
        
        if (allMet) {
            System.out.println("[DEBUG] All dependencies are now met for task: " + name);
        }
    }
    
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }
    
    public List<Ingredient> getIngredients() {
        return ingredients;
    }
    
    public String getName() {
        return name;
    }
    
    public StationType getStationType() {
        return stationType;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    /**
     * Checks if this task has been assigned to a station
     * @return true if the task has been assigned
     */
    public boolean isAssigned() {
        return assigned;
    }
    
    /**
     * Sets whether this task has been assigned to a station
     * @param assigned true if the task is being assigned, false if unassigned
     */
    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
    
    public int getCookingTime() {
        return cookingTime;
    }
    
    /**
     * Gets the recipe this task belongs to
     * @return The parent recipe
     */
    public Recipe getRecipe() {
        return recipe;
    }
    
    /**
     * Sets the recipe this task belongs to
     * @param recipe The parent recipe
     */
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            System.out.println("[DEBUG-TASK-EQUALS] Same instance comparison for task: " + name);
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            System.out.println("[DEBUG-TASK-EQUALS] Different class comparison for task: " + name);
            return false;
        }
        RecipeTask that = (RecipeTask) o;
        
        // Basic equality check for name and station type
        boolean basicEquality = name.equals(that.name) && stationType == that.stationType;
        
        // If recipes are available, also check if they're from the same order
        if (recipe != null && that.recipe != null) {
            String thisOrderId = recipe.getOrderId();
            String thatOrderId = that.recipe.getOrderId();
            
            System.out.println("[DEBUG-TASK-EQUALS] Comparing tasks - THIS: " + name + 
                             " (stationType: " + stationType + 
                             ", orderId: " + thisOrderId + "), " +
                             "OTHER: " + that.name + 
                             " (stationType: " + that.stationType + 
                             ", orderId: " + thatOrderId + ")");
            
            if (thisOrderId != null && thatOrderId != null) {
                boolean result = basicEquality && thisOrderId.equals(thatOrderId);
                System.out.println("[DEBUG-TASK-EQUALS] Both orderIds available. Equal? " + result + 
                                  " (basic equality: " + basicEquality + 
                                  ", orderIds equal: " + thisOrderId.equals(thatOrderId) + ")");
                return result;
            }
        } else {
            System.out.println("[DEBUG-TASK-EQUALS] At least one recipe is null. THIS recipe: " + 
                             (recipe != null ? "available" : "null") + 
                             ", OTHER recipe: " + (that.recipe != null ? "available" : "null"));
        }
        
        System.out.println("[DEBUG-TASK-EQUALS] Falling back to basic equality: " + basicEquality);
        return basicEquality;
    }
    
    @Override
    public int hashCode() {
        // Include recipe's orderId in hash if available
        if (recipe != null && recipe.getOrderId() != null) {
            int hash = Objects.hash(name, stationType, recipe.getOrderId());
            System.out.println("[DEBUG-TASK-HASHCODE] Task " + name + 
                              " with orderId " + recipe.getOrderId() + 
                              " hashCode: " + hash);
            return hash;
        }
        int hash = Objects.hash(name, stationType);
        System.out.println("[DEBUG-TASK-HASHCODE] Task " + name + 
                          " without orderId hashCode: " + hash);
        return hash;
    }
    
    @Override
    public String toString() {
        return name + " (" + stationType + ")";
    }
}
