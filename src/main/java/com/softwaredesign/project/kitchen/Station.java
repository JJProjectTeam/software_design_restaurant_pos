package com.softwaredesign.project.kitchen;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.staff.Chef;

public class Station extends Entity {
    private final StationType type;
    private List<RecipeTask> backlog;
    private Chef assignedChef;
    private Recipe currentRecipe;
    private RecipeTask currentTask;
    private Chef taskChef; // Chef who is currently working on the task
    private int cookingProgress;
    private CollectionPoint collectionPoint;
    private boolean needsIngredients;
    private Kitchen kitchen;  // Reference to the kitchen

    public Station(StationType type, CollectionPoint collectionPoint) {
        this.type = type;
        this.backlog = new ArrayList<>();
        this.collectionPoint = collectionPoint;
        this.cookingProgress = 0;
        this.needsIngredients = false;
    }
    
    public void setKitchen(Kitchen kitchen) {
        this.kitchen = kitchen;
    }

    public StationType getType() {
        return type;
    }
    
    /**
     * Finds a recipe with tasks that can be processed at this station
     * and have their dependencies met
     * @return A recipe with ready tasks, or null if none found
     */
    public Recipe findRecipeWithReadyTasks() {
        // Check if any recipes have tasks that can be done at this station and have dependencies met
        List<Recipe> availableRecipes = new ArrayList<>();
        
        // First check the current recipe if there is one
        if (currentRecipe != null) {
            for (RecipeTask task : currentRecipe.getUncompletedTasks()) {
                if (task.getStationType() == this.type && task.areDependenciesMet() && !task.isCompleted()) {
                    return currentRecipe;
                }
            }
        }
        
        // Then check the backlog
        for (RecipeTask task : backlog) {
            if (task.getStationType() == this.type && task.areDependenciesMet() && !task.isCompleted()) {
                return task.getRecipe();
            }
        }
        
        return null;
    }

    public void addTask(RecipeTask task) {
        // Check if this specific task is already in the backlog to prevent duplicates
        boolean taskAlreadyExists = false;
        
        for (RecipeTask existingTask : backlog) {
            // Check if it's the same task (same name and same station type)
            if (existingTask.getName().equals(task.getName()) && 
                existingTask.getStationType() == task.getStationType()) {
                Recipe existingRecipe = existingTask.getRecipe();
                Recipe newRecipe = task.getRecipe();
                
                // If both tasks have the same recipe, it's a duplicate
                if (existingRecipe != null && newRecipe != null && 
                    existingRecipe.equals(newRecipe)) {
                    taskAlreadyExists = true;
                    break;
                }
                
                // If recipes have the same order ID, consider it a duplicate
                if (existingRecipe != null && newRecipe != null && 
                    existingRecipe.getOrderId() != null && 
                    existingRecipe.getOrderId().equals(newRecipe.getOrderId())) {
                    taskAlreadyExists = true;
                    break;
                }
            }
        }
        
        // Only add the task if it's not already in the backlog
        if (!taskAlreadyExists) {
            backlog.add(task);
        }
    }
    
    // Method to add all tasks from an order to this station's backlog
    public void addTasksFromOrder(Order order) {
        for (Recipe recipe : order.getRecipes()) {
            for (RecipeTask task : recipe.getUncompletedTasks()) {
                if (task.getStationType() == this.type) {
                    // Use addTask to prevent duplicates
                    addTask(task);
                }
            }
        }
    }

    public LocalDateTime getOldestTaskTime() {
        return LocalDateTime.now(); //TODO task must be given a timestamp when added to stations
    }

    // get tasks from station
    public List<RecipeTask> getBacklog() {
        return backlog;
    }

    public int getBacklogSize() {
        return backlog.size();
    }
    
    /**
     * Checks if this station has any items in its backlog.
     * @return true if the backlog has at least one task
     */
    public boolean hasBacklogItems() {
        return !backlog.isEmpty();
    }

    public void registerChef(Chef chef) {
        System.out.println("\n[DEBUG-STATION] Registering chef " + chef.getName() + " at " + type + " station");
        System.out.println("[DEBUG-STATION] Current assignedChef: " + 
            (assignedChef != null ? assignedChef.getName() : "NONE"));
        System.out.println("[DEBUG-STATION] Chef's current station: " + 
            (chef.getCurrentStation() != null ? chef.getCurrentStation().getType() : "NONE"));
        
        // If we already have a chef assigned, unregister them first
        if (assignedChef != null && assignedChef != chef) {
            System.out.println("[DEBUG-STATION] Station already has chef " + assignedChef.getName() + ", unregistering them");
            // Log if a task is being reassigned
            if (currentTask != null) {
                System.out.println("Chef " + assignedChef.getName() + " has left " + type + " station, task reassigned");
            }
            // Unregister the current chef
            assignedChef.removeStationAssignment(this);
            assignedChef = null;
        }
        
        // Check if the chef is already assigned to another station and unregister them from there
        Station currentChefStation = chef.getCurrentStation();
        if (currentChefStation != null && currentChefStation != this) {
            System.out.println("[DEBUG-STATION] Chef " + chef.getName() + " is currently at " + 
                currentChefStation.getType() + " station, unregistering from there");
            System.out.println("Chef " + chef.getName() + " was at " + currentChefStation.getType() + 
                              " station but is now moving to " + type + " station");
            // Unregister from the other station
            if (currentChefStation.getAssignedChef() == chef) {
                System.out.println("[DEBUG-STATION] Unregistering chef from " + currentChefStation.getType());
                currentChefStation.unregisterChef();
            } else {
                System.out.println("[DEBUG-STATION] WARNING: Chef thinks they're at " + 
                    currentChefStation.getType() + " but that station doesn't have them assigned!");
            }
        }
        
        // Set this station as the chef's current station
        System.out.println("[DEBUG-STATION] Setting " + type + " as current station for " + chef.getName());
        chef.setCurrentStation(this);
        assignedChef = chef;
        
        System.out.println("Chef " + chef.getName() + " is now registered at " + type + " station");
        
        // If there's a task but no chef was assigned to it yet, assign this chef
        if (currentTask != null && taskChef == null) {
            System.out.println("[DEBUG-STATION] Assigning chef to task: " + currentTask.getName());
            taskChef = chef;
        }
        
        // Verify the registration worked
        System.out.println("[DEBUG-STATION] After registration: " + 
            chef.getName() + " current station is " + 
            (chef.getCurrentStation() != null ? chef.getCurrentStation().getType() : "NONE") + 
            ", station's assigned chef is " + 
            (assignedChef != null ? assignedChef.getName() : "NONE"));
    }

    public void unregisterChef() {
        System.out.println("\n[DEBUG-STATION] Unregistering chef from " + type + " station");
        System.out.println("[DEBUG-STATION] Current assignedChef: " + 
            (assignedChef != null ? assignedChef.getName() : "NONE"));
        
        // Note: We don't unregister the taskChef here because they remain
        // associated with the current task even if they leave the station
        
        if (assignedChef != null) {
            System.out.println("[DEBUG-STATION] Found assigned chef: " + assignedChef.getName());
            
            // If this chef considers this their current station, update their state
            if (assignedChef.getCurrentStation() == this) {
                System.out.println("[DEBUG-STATION] Chef " + assignedChef.getName() + 
                    " considers this their current station, clearing reference");
                System.out.println("Chef " + assignedChef.getName() + " is no longer registered at " + type + " station");
                // Clear the chef's station reference
                assignedChef.setCurrentStation(null);
            } else {
                System.out.println("[DEBUG-STATION] WARNING: Chef " + assignedChef.getName() + 
                    " is assigned here but thinks they're at " + 
                    (assignedChef.getCurrentStation() != null ? 
                        assignedChef.getCurrentStation().getType() : "NONE"));
            }
            
            Chef departingChef = assignedChef;
            System.out.println("[DEBUG-STATION] Setting assignedChef to null");
            assignedChef = null;
            
            // Make sure the chef's assigned stations list no longer contains this station
            System.out.println("[DEBUG-STATION] Removing station from chef's assigned stations list");
            departingChef.removeStationAssignment(this);
            
            System.out.println("[DEBUG-STATION] After unregistration: Chef " + departingChef.getName() + 
                " current station is " + 
                (departingChef.getCurrentStation() != null ? 
                    departingChef.getCurrentStation().getType() : "NONE"));
        } else {
            System.out.println("[DEBUG-STATION] No chef assigned to unregister");
            assignedChef = null;
        }
    }

    public Chef getAssignedChef() {
        return assignedChef;
    }
    
    /**
     * Checks if a chef is assigned to this station
     * @return true if a chef is assigned, false otherwise
     */
    public boolean hasChef() {
        return assignedChef != null;
    }

    public void assignRecipe(Recipe recipe) {
        if (currentRecipe == null && currentTask == null) {
            // Try to find an incomplete task for this station
            for (RecipeTask task : recipe.getIncompleteTasks()) {
                if (task.getStationType() == this.type) {
                    currentRecipe = recipe;
                    currentTask = task;
                    cookingProgress = 0;
                    needsIngredients = true;
                    
                    // Mark the task as assigned
                    task.setAssigned(true);
                    
                    // Find and remove the order from the backlog that contains this recipe
                    String orderId = recipe.getOrderId();
                    if (orderId != null) {
                        for (int i = 0; i < backlog.size(); i++) {
                            RecipeTask backlogTask = backlog.get(i);
                            Recipe backlogRecipe = backlogTask.getRecipe();
                            if (backlogRecipe != null && orderId.equals(backlogRecipe.getOrderId()) && 
                                backlogTask.getStationType() == type) {
                                // Debug output
                                System.out.println(type + " station removing related task " + backlogTask.getName() + 
                                                   " for order " + orderId + " from backlog");
                                backlog.remove(i);
                                break;
                            }
                        }
                    }
                    
                    System.out.println(type + " station assigned task: " + task.getName() + 
                                      " for recipe: " + recipe.getName());
                    return;
                }
            }
            
            // No tasks for this station type in this recipe
            System.out.println(type + " station has no tasks in recipe: " + recipe.getName());
        } else {
            System.out.println(type + " station is busy with another task");
        }
    }
    
    public void assignTask(Recipe recipe, RecipeTask task) {
        if (currentRecipe == null && currentTask == null) { // if no task is assigned
            // Check if all dependencies are met before assigning the task
            if (!task.areDependenciesMet()) {
                String unmetDependencies = task.getUnmetDependenciesString();
                System.out.println(type + " station cannot start task: " + task.getName() + 
                              " - waiting for: " + unmetDependencies);
                return;
            }
            
            // Check if the assigned chef is already working on another task
            if (assignedChef != null && assignedChef.isWorking()) {
                System.out.println("Cannot assign task to " + assignedChef.getName() + 
                               " as they are already working on another task");
                return;
            }
            
            if (task.getStationType() == this.type) { // if the task is for this station
                currentRecipe = recipe;
                currentTask = task;
                // Set the chef working on this task to the currently assigned chef
                taskChef = assignedChef;
                cookingProgress = 0;
                needsIngredients = true;
                
                // Mark the task as assigned
                task.setAssigned(true);
                
                // Mark the chef as working
                if (taskChef != null) {
                    taskChef.setWorking(true);
                }
                
                // Find and remove the task from the backlog that matches this recipe/task
                // First, remove this specific task from the backlog
                boolean taskRemoved = false;
                for (int i = 0; i < backlog.size(); i++) {
                    RecipeTask backlogTask = backlog.get(i);
                    if (backlogTask == task) {
                        // Debug output
                        System.out.println(type + " station removing task " + task.getName() + " from backlog");
                        backlog.remove(i);
                        taskRemoved = true;
                        break;
                    }
                }
                
                // If we didn't find the exact task, try to find a task from the same recipe
                if (!taskRemoved && recipe != null && recipe.getOrderId() != null) {
                    String orderId = recipe.getOrderId();
                    for (int i = 0; i < backlog.size(); i++) {
                        RecipeTask backlogTask = backlog.get(i);
                        Recipe backlogRecipe = backlogTask.getRecipe();
                        if (backlogRecipe != null && orderId.equals(backlogRecipe.getOrderId()) && 
                            backlogTask.getStationType() == type) {
                            // Debug output
                            System.out.println(type + " station removing related task " + backlogTask.getName() + 
                                             " for order " + orderId + " from backlog");
                            backlog.remove(i);
                            break;
                        }
                    }
                }
                
                StringBuilder assignmentMessage = new StringBuilder();
                assignmentMessage.append(type).append(" station assigned task: ").append(task.getName());
                assignmentMessage.append(" for recipe: ").append(recipe.getName());
                
                // Add chef information if available
                if (taskChef != null) {
                    assignmentMessage.append(" (assigned to ").append(taskChef.getName()).append(")");
                }
                
                // Add order ID if available
                if (recipe.getOrderId() != null) {
                    assignmentMessage.append(" (Order ID: ").append(recipe.getOrderId()).append(")");
                }
                
                System.out.println(assignmentMessage.toString());
            } else {
                System.out.println("Cannot assign task for " + task.getStationType() + 
                                  " to " + this.type + " station");
            }
        } else {
            System.out.println(type + " station is busy with another task");
        }
    }

    public boolean isBusy() {
        return currentTask != null;
    }

    public boolean needsIngredients() {
        return needsIngredients;
    }
    
    /**
     * Get the current task being processed at this station
     * @return the current recipe task or null if no task is assigned
     */
    public RecipeTask getCurrentTask() {
        return currentTask;
    }
    
    /**
     * Get the current cooking progress for the assigned task
     * @return the cooking progress value
     */
    public int getCookingProgress() {
        return cookingProgress;
    }

    public void provideIngredients() {
        if (currentTask != null && needsIngredients) {
            needsIngredients = false;
            System.out.println(type + " station received ingredients for task: " + 
                               currentTask.getName() + " of " + currentRecipe.getName());
        }
    }

    @Override
    public void readState() {
        // In read state, we check if we can cook
        if (currentTask != null && hasChef() && !needsIngredients) {
            // Ready to progress cooking
        }
    }

    @Override
    public void writeState() {
        // In write state, we update cooking progress
        if (currentTask != null && hasChef() && !needsIngredients) {
            // Update taskChef if it's not set yet
            if (taskChef == null) {
                taskChef = assignedChef;
            }
            
            cookingProgress++;
            int requiredTime = currentTask.getCookingTime();
            
            StringBuilder progressMessage = new StringBuilder();
            progressMessage.append(type).append(" station cooking ").append(currentTask.getName());
            progressMessage.append(" for ").append(currentRecipe.getName());
            progressMessage.append(" - Progress: ").append(cookingProgress).append("/").append(requiredTime);
            
            // Add chef information if available - use taskChef instead of assignedChef
            if (taskChef != null) {
                progressMessage.append(" (cooked by ").append(taskChef.getName()).append(")");
            }
            
            // Add order ID if available
            if (currentRecipe.getOrderId() != null) {
                progressMessage.append(" (Order ID: ").append(currentRecipe.getOrderId()).append(")");
            }
            
            System.out.println(progressMessage.toString());
            
            if (cookingProgress >= requiredTime) {
                // Task is done
                StringBuilder completionMessage = new StringBuilder();
                completionMessage.append(type).append(" station completed task: ").append(currentTask.getName());
                completionMessage.append(" for ").append(currentRecipe.getName());
                
                // Add chef information if available - use taskChef instead of assignedChef
                if (taskChef != null) {
                    completionMessage.append(" (completed by ").append(taskChef.getName()).append(")");
                }
                
                // Add order ID if available
                if (currentRecipe.getOrderId() != null) {
                    completionMessage.append(" (Order ID: ").append(currentRecipe.getOrderId()).append(")");
                }
                
                System.out.println(completionMessage.toString());
                
                // Mark the task as completed and no longer assigned
                currentTask.setCompleted(true);
                currentTask.setAssigned(false);
                Recipe completedRecipe = currentRecipe;
                
                // Check if the entire recipe is completed
                if (completedRecipe.allTasksCompleted()) {
                    // All tasks are complete, build the meal
                    System.out.println("All tasks for " + completedRecipe.getName() + " are complete, building meal");
                    
                    // Get order ID from recipe
                    String orderId = completedRecipe.getOrderId();
                    
                    try {
                        // Build the meal - use buildMeal() not createMeal()!
                        Meal completedMeal = completedRecipe.buildMeal();
                        
                        // Set the order ID on the meal explicitly (the buildMeal already does this, but let's be extra sure)
                        if (orderId != null) {
                            completedMeal.setOrderId(orderId);
                        }
                        
                        // Add to collection point
                        collectionPoint.addCompletedMeal(completedMeal);
                        
                    } catch (Exception e) {
                        System.out.println("[ERROR] Failed to add meal to collection point: " + e.getMessage());
                        
                        // Try to re-register the order if needed
                        if (orderId != null && kitchen != null && kitchen.getOrderManager() != null) {
                            OrderManager orderManager = kitchen.getOrderManager();
                            for (Order order : orderManager.getPendingOrders()) {
                                if (order.getOrderId().equals(orderId)) {
                                    // Re-register the order with the collection point
                                    collectionPoint.registerOrder(orderId, order.getRecipes().size());
                                    System.out.println("[DEBUG] Re-registered order " + orderId + " with collection point");
                                    
                                    // Try again with proper error handling
                                    try {
                                        // Build the meal with the buildMeal method
                                        Meal retryMeal = completedRecipe.buildMeal();
                                        
                                        // Double-check that the order ID is set correctly
                                        if (orderId != null) {
                                            retryMeal.setOrderId(orderId);
                                        }
                                        
                                        // Now add the meal to the collection point
                                        collectionPoint.addCompletedMeal(retryMeal);
                                        System.out.println("[DEBUG] Successfully added meal after re-registration");
                                    } catch (Exception ex) {
                                        System.out.println("[ERROR] Failed to add meal even after re-registration: " + ex.getMessage());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Recipe " + completedRecipe.getName() + " still has tasks remaining");
                    
                        // Find the next station that has tasks for this recipe
                    boolean foundNextTask = false;
                    StationType nextStationType = null;
                    
                    // This approach checks if the recipe has more uncompeted tasks
                    for (RecipeTask nextTask : completedRecipe.getUncompletedTasks()) {
                        // Check if the task is not yet completed and its dependencies are met
                        if (!nextTask.isCompleted()) {
                            // Update the task dependencies now that the current task is completed
                            nextTask.updateDependenciesStatus();
                            
                            if (nextTask.areDependenciesMet()) {
                                nextStationType = nextTask.getStationType();
                                foundNextTask = true;
                                break;
                            }
                        }
                    }
                    
                    if (foundNextTask && nextStationType != null) {
                        // Get the order this recipe belongs to
                        String orderId = completedRecipe.getOrderId();
                        if (orderId != null && kitchen != null) {
                            // Get the station manager from the kitchen
                            StationManager stationManager = kitchen.getStationManager();
                            if (stationManager != null) {
                                // Get the next station
                                Station nextStation = stationManager.getStation(nextStationType);
                                if (nextStation != null) {
                                    // Get the order manager from the kitchen
                                    OrderManager orderManager = kitchen.getOrderManager();
                                    if (orderManager != null) {
                                        // Find the right order using the order ID
                                        for (Order order : orderManager.getPendingOrders()) {
                                            if (order.getOrderId().equals(orderId)) {
                                                // Add this order to the next station's backlog
                                                System.out.println("[DEBUG] Moving order " + orderId + " from " + type + 
                                                            " to " + nextStationType + " station backlog");
                                                nextStation.addTasksFromOrder(order);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("[DEBUG] No next station found for recipe " + completedRecipe.getName());
                    }
                }
                
                // Store current chef for later use
                Chef currentChef = taskChef;
                
                // Reset the working status of the chef
                if (taskChef != null) {
                    taskChef.setWorking(false);
                }
                
                // Reset station state
                currentTask = null;
                currentRecipe = null;
                taskChef = null;
                cookingProgress = 0;
                
                // Try to find a new task for this station immediately
                tryAssignNewTask();
                
                // If we still don't have a task and the chef is still assigned here,
                // have them look for work elsewhere
                if (currentChef != null && !currentChef.isWorking()) {
                    System.out.println("[DEBUG] Having chef " + currentChef.getName() + " look for new work");
                    currentChef.chooseNextStation();
                }
            }
        }
        // If we have a chef but no task, try to assign a new task
        else if (hasChef() && currentTask == null && !assignedChef.isWorking()) {
            tryAssignNewTask();
        }
    }
    
    /**
     * Tries to find and assign a new task to this station
     */
    private void tryAssignNewTask() {
        System.out.println("[DEBUG] " + type + " station looking for new tasks");
        
        // First check if we have any ready recipes with tasks for this station
        Recipe readyRecipe = findRecipeWithReadyTasks();
        if (readyRecipe != null) {
            System.out.println("[DEBUG] " + type + " station found recipe with ready tasks: " + readyRecipe.getName());
            
            // Find an appropriate task in this recipe
            for (RecipeTask task : readyRecipe.getUncompletedTasks()) {
                if (task.getStationType() == this.type && task.areDependenciesMet() && !task.isCompleted()) {
                    System.out.println("[DEBUG] " + type + " station found ready task: " + task.getName());
                    assignTask(readyRecipe, task);
                    return;
                }
            }
        }
        
        // If we have a kitchen reference, ask it to look for new recipes
        if (kitchen != null) {
            System.out.println("[DEBUG] " + type + " station asking kitchen for new recipes");
            kitchen.getRecipes();  // This will populate the kitchen with any new recipes
            kitchen.updateTaskAvailability();  // Update task dependencies
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
