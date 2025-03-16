package com.softwaredesign.project.order;

import com.softwaredesign.project.engine.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Recipe {
    private static final Logger logger = LoggerFactory.getLogger(Recipe.class);
    protected String name;
    protected List<Ingredient> ingredients;
    protected List<RecipeTask> tasks;
    protected String orderId;
    protected final InventoryService inventoryService;

    protected Recipe(String name, InventoryService inventoryService) {
        if (inventoryService == null) {
            throw new IllegalArgumentException("InventoryService cannot be null");
        }
        if (name == null || name.isEmpty()) {
            logger.error("Recipe name cannot be null or empty");
            throw new IllegalArgumentException("Recipe name cannot be null or empty");
        }
        this.name = name;
        this.inventoryService = inventoryService;
        this.ingredients = new ArrayList<>();
        this.tasks = new ArrayList<>();
        initializeBaseIngredients();
        initializeTasks();
        
        // Ensure all tasks have their recipe reference set
        for (RecipeTask task : tasks) {
            if (task.getRecipe() == null) {
                task.setRecipe(this);
                logger.info("[FIX] Setting recipe reference for task: " + task.getName() + " in recipe: " + name);
            }
        }
        logger.info("Created new recipe: {}", name);
    }

    protected abstract void initializeBaseIngredients();
    
    // New method for initializing tasks - to be implemented by subclasses
    protected abstract void initializeTasks();

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getName() {
        return name;
    }

    // New method to check if all tasks are completed
    public boolean allTasksCompleted() {
        if (tasks.isEmpty()) {
            return true;
        }
        
        for (RecipeTask task : tasks) {
            if (!task.isCompleted()) {
                return false;
            }
        }
        
        return true;
    }
    
    // New method to add a task
    public void addTask(RecipeTask task) {
        tasks.add(task);
        task.setRecipe(this); // Set the reference to this recipe
    }
    
    // New method to get the list of tasks
    public List<RecipeTask> getTasks() {
        return tasks;
    }
    
    /**
     * Returns a list of all tasks in this recipe that are not yet completed
     * @return List of uncompleted tasks
     */
    public List<RecipeTask> getUncompletedTasks() {
        return tasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }
    
    // New method to get incomplete tasks
    public List<RecipeTask> getIncompleteTasks() {
        return tasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    public boolean isComplete() {
        return allTasksCompleted();
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Recipe recipe = (Recipe) o;
        return name.equals(recipe.name);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name);
    }

    public Meal buildMeal() {
        // Collect all ingredients from tasks
        List<Ingredient> allIngredients = new ArrayList<>();
        // Add direct ingredients from recipe
        allIngredients.addAll(ingredients);
        // Add ingredients from tasks
        for (RecipeTask task : tasks) {
            allIngredients.addAll(task.getIngredients());
        }
        
        return new Meal(name, allIngredients, inventoryService, orderId);
    }
}