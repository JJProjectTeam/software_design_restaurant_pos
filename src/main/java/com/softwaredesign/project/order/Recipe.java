package com.softwaredesign.project.order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.Station;

public abstract class Recipe {
    protected String name;
    protected List<Ingredient> ingredients;
    protected List<RecipeTask> tasks;
    protected Queue<Station> stationsToVisit;
    protected String orderId;
    protected final InventoryService inventoryService;

    protected Recipe(String name, InventoryService inventoryService) {
        if (inventoryService == null) {
            throw new IllegalArgumentException("InventoryService cannot be null");
        }
        this.name = name;
        this.inventoryService = inventoryService;
        this.ingredients = new ArrayList<>();
        this.tasks = new ArrayList<>();
        initializeBaseIngredients();
        initializeTasks();
        stationsToVisit = new LinkedList<>();
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

    public Queue<Station> getStationsToVisit() {
        return stationsToVisit;
    }

    public void addStation(Station station) {
        stationsToVisit.add(station);
    }

    public void removeStation(Station station) {
        stationsToVisit.remove(station);
    }

    public boolean isComplete() {
        return stationsToVisit.isEmpty() && allTasksCompleted();
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
        return Objects.hash(name);
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