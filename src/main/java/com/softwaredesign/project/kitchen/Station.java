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
    private Chef taskChef; // Chef who is currently working on the task

    private Recipe currentRecipe;
    private RecipeTask currentTask;

    private int cookingProgress; // can be used to track progress of a task

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
        // Ensure the task has a recipe reference
        if (task.getRecipe() == null) {
            System.out.println("[WARNING] Task added to backlog without recipe reference: " + task.getName());
            // Don't add tasks without a recipe reference
            System.out.println("[FIX] Rejecting task without recipe reference: " + task.getName());
            return;
        }
        
        System.out.println("[DEBUG-STATION-ADD] Adding task " + task.getName() + 
                         " from recipe " + task.getRecipe().getName() + 
                         " with orderId " + task.getRecipe().getOrderId() + 
                         " to " + type + " station backlog");
        
        // Check if this specific task is already in the backlog to prevent duplicates
        boolean taskAlreadyExists = false;
        
        // First, print all backlog tasks with their details
        System.out.println("[DEBUG-STATION-ADD] Current backlog for " + type + " station:");
        for (int i = 0; i < backlog.size(); i++) {
            RecipeTask existingTask = backlog.get(i);
            Recipe existingRecipe = existingTask.getRecipe();
            String existingOrderId = existingRecipe != null ? existingRecipe.getOrderId() : "null";
            System.out.println("  " + i + ": " + existingTask.getName() + 
                             " (recipe: " + (existingRecipe != null ? existingRecipe.getName() : "null") + 
                             ", orderId: " + existingOrderId + 
                             ", hashCode: " + existingTask.hashCode() + ")");
        }
        
        for (RecipeTask existingTask : backlog) {
            // Manually check if tasks appear to be duplicates
            boolean sameTaskName = existingTask.getName().equals(task.getName());
            boolean sameStationType = existingTask.getStationType() == task.getStationType();
            
            // Get recipes and orderIds
            Recipe existingRecipe = existingTask.getRecipe();
            Recipe newTaskRecipe = task.getRecipe();
            String existingOrderId = existingRecipe != null ? existingRecipe.getOrderId() : null;
            String newOrderId = newTaskRecipe != null ? newTaskRecipe.getOrderId() : null;
            
            System.out.println("[DEBUG-STATION-ADD] Comparing with existing task: " + existingTask.getName() + 
                             ", sameTaskName: " + sameTaskName + 
                             ", sameStationType: " + sameStationType);
            
            if (existingRecipe != null && newTaskRecipe != null) {
                boolean sameRecipeName = existingRecipe.getName().equals(newTaskRecipe.getName());
                System.out.println("[DEBUG-STATION-ADD]   Both have recipes. sameRecipeName: " + sameRecipeName);
                
                if (existingOrderId != null && newOrderId != null) {
                    boolean sameOrderId = existingOrderId.equals(newOrderId);
                    System.out.println("[DEBUG-STATION-ADD]   Both have orderIds. sameOrderId: " + sameOrderId + 
                                     " (" + existingOrderId + " vs " + newOrderId + ")");
                } else {
                    System.out.println("[DEBUG-STATION-ADD]   At least one orderId is null. existingOrderId: " + 
                                     existingOrderId + ", newOrderId: " + newOrderId);
                }
            }
            
            // Now use the equals method and track the result
            boolean equals = existingTask.equals(task);
            System.out.println("[DEBUG-STATION-ADD]   Tasks equal according to equals(): " + equals);
            
            if (equals) {
                taskAlreadyExists = true;
                System.out.println("[DEBUG-STATION-ADD] Prevented duplicate task: " + task.getName() + 
                               " for recipe: " + (task.getRecipe() != null ? task.getRecipe().getName() : "unknown") +
                               " with orderId: " + (task.getRecipe() != null ? task.getRecipe().getOrderId() : "unknown"));
                break;
            }
        }
        
        // Only add the task if it's not already in the backlog
        if (!taskAlreadyExists) {
            // FIXED: Insert the task at the correct position based on order ID (to process older orders first)
            String orderId = task.getRecipe().getOrderId();
            boolean inserted = false;
            
            // If the task has an order ID, find the correct position to insert it
            if (orderId != null) {
                // Extract the numerical portion of the order ID (e.g., "1000" from "Order-1000")
                int orderNum = extractOrderNumber(orderId);
                
                // Find the correct position to insert based on order number
                for (int i = 0; i < backlog.size(); i++) {
                    RecipeTask existingTask = backlog.get(i);
                    String existingOrderId = existingTask.getRecipe() != null ? 
                                             existingTask.getRecipe().getOrderId() : null;
                    
                    if (existingOrderId != null) {
                        int existingOrderNum = extractOrderNumber(existingOrderId);
                        
                        // If the new task's order number is lower, insert it here
                        if (orderNum < existingOrderNum) {
                            backlog.add(i, task);
                            inserted = true;
                            System.out.println("[DEBUG] Inserted task " + task.getName() + 
                                           " for order " + orderId + " at position " + i + 
                                           " (before order " + existingOrderId + ")");
                            break;
                        }
                    }
                }
            }
            
            // If not inserted yet, add to the end
            if (!inserted) {
                backlog.add(task);
            }
            
            System.out.println("[DEBUG-BACKLOG] Current " + type + " station backlog contents:");
            for (int i = 0; i < backlog.size(); i++) {
                RecipeTask t = backlog.get(i);
                System.out.println("  " + i + ": " + t.getName() + " (Order: " + 
                    (t.getRecipe() != null ? t.getRecipe().getOrderId() : "null") + ")");
            }
        }
    }
    
    /**
     * Helper method to extract the numerical portion of an order ID
     * @param orderId The order ID (e.g., "Order-1000")
     * @return The numerical value (e.g., 1000)
     */
    private int extractOrderNumber(String orderId) {
        if (orderId == null || !orderId.contains("-")) {
            return Integer.MAX_VALUE; // Default to highest value for unrecognized format
        }
        
        try {
            String[] parts = orderId.split("-");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            System.out.println("[WARNING] Could not parse order number from ID: " + orderId);
            return Integer.MAX_VALUE; // Default to highest value for unparseable IDs
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
                    
                    // Preserve the assigned task in the backlog so that the station always shows tasks (even if in progress)
                    
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
    
    /**
     * Assigns a task from a recipe to this station
     * @param recipe The recipe the task belongs to
     * @param task The task to assign
     */
    public void assignTask(Recipe recipe, RecipeTask task) {
        if (currentTask != null) {
            System.out.println("[DEBUG-STATION] " + type + " station already has task " + 
                currentTask.getName() + ", not assigning " + task.getName());
            
            // Add to backlog instead
            if (!backlog.contains(task)) {
                backlog.add(task);
                System.out.println("[DEBUG-STATION] Added " + task.getName() + " to " + type + " station backlog");
            }
            return;
        }
        
        System.out.println("[DEBUG-STATION] Assigning task " + task.getName() + " to " + type + " station");
        currentRecipe = recipe;
        currentTask = task;
        cookingProgress = 0;
        needsIngredients = true;
        
        // If we have a chef assigned, mark them as working
        if (assignedChef != null) {
            assignedChef.setWorking(true);
            System.out.println("[DEBUG-STATION] Chef " + assignedChef.getName() + 
                " is now working on task " + task.getName());
        } else {
            System.out.println("[DEBUG-STATION] No chef assigned to " + type + 
                " station, task " + task.getName() + " waiting for chef");
        }
        
        // Ingredients will be provided by the kitchen during its writeState method
        // via provideIngredientsToStations()
        if (kitchen == null) {
            System.out.println("[DEBUG-STATION] Kitchen reference is null, cannot request ingredients");
            // Auto-provide ingredients for testing
            needsIngredients = false;
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
     * Tries to pull the next task from the backlog queue if available.
     */
    private void tryAssignNewTask() {
        System.out.println("[DEBUG] " + type + " station checking backlog for new tasks");
        
        // Print the current backlog to help diagnose the issue
        System.out.println("[DEBUG-BACKLOG] " + type + " station backlog before assignment:");
        for (int i = 0; i < backlog.size(); i++) {
            RecipeTask t = backlog.get(i);
            Recipe r = t.getRecipe();
            String orderId = (r != null) ? r.getOrderId() : "unknown";
            System.out.println("  " + i + ": " + t.getName() + " (Order: " + orderId + ")");
        }
        
        if (!backlog.isEmpty()) {
            // Get the first task (which should be the oldest order due to our sorting)
            RecipeTask nextTask = backlog.remove(0);
            Recipe recipe = nextTask.getRecipe();
            
            // Log the order ID to confirm we're taking the correct task
            String orderId = (recipe != null) ? recipe.getOrderId() : "unknown";
            System.out.println("[DEBUG-IMPORTANT] " + type + " station taking task " + 
                             nextTask.getName() + " from order " + orderId);
            
            // Ensure we're not already working on something
            if (currentTask != null) {
                System.out.println("[ERROR] Station already has task " + currentTask.getName() + 
                                " from order " + (currentRecipe != null ? currentRecipe.getOrderId() : "unknown") + 
                                " but trying to assign " + nextTask.getName() + 
                                " from order " + orderId);
                
                // Put the task back at the front of the backlog and exit
                backlog.add(0, nextTask);
                return;
            }
            
            // Proceed with assignment
            currentRecipe = recipe;
            currentTask = nextTask;
            cookingProgress = 0;
            needsIngredients = true;

            if (assignedChef != null) {
                assignedChef.setWorking(true);
                System.out.println("[DEBUG-STATION] Chef " + assignedChef.getName() + 
                                 " is now working on task " + nextTask.getName() + 
                                 " from order " + orderId);
            } else {
                System.out.println("[DEBUG-STATION] No chef assigned to " + type + 
                                 " station, queued task " + nextTask.getName() + 
                                 " from order " + orderId + 
                                 " remains waiting for chef assignment");
            }
            return;
        }
        
        // Instead of asking kitchen for new recipes, simply log that we're waiting for new tasks
        System.out.println("[DEBUG] " + type + " station backlog is empty, waiting for new tasks");
    }

    public Recipe getCurrentRecipe() {
        return currentRecipe;
    }
    
    @Override
    public String toString() {
        return type.toString() + " Station";
    }

    /**
     * Sets the CollectionPoint for this station.
     * This ensures all stations use the same CollectionPoint instance.
     * @param collectionPoint The CollectionPoint to use for this station
     */
    public void setCollectionPoint(CollectionPoint collectionPoint) {
        this.collectionPoint = collectionPoint;
    }

    /**
     * Completes the current meal and adds it to the collection point.
     * This is a critical method for order fulfillment.
     */
    private void completeMeal() {
        if (currentRecipe != null && currentTask != null && currentTask.isCompleted()) {
            // Check if this is the last task for the recipe
            if (currentRecipe.isComplete()) {
                System.out.println("[Station] Recipe complete: " + currentRecipe.getName());
                
                try {
                    // Build the meal using the recipe's buildMeal method
                    Meal meal = currentRecipe.buildMeal();
                    
                    System.out.println("[Station] Adding completed meal to collection point: " + 
                        meal.getOrderId() + " - " + currentRecipe.getName());
                    
                    // This is the critical step - use the correct CollectionPoint
                    if (collectionPoint != null) {
                        collectionPoint.addCompletedMeal(meal);
                        System.out.println("[Station] Successfully added meal to collection point");
                    } else {
                        System.err.println("[Station] ERROR: CollectionPoint is null! Meal cannot be added.");
                    }
                } catch (Exception e) {
                    System.err.println("[Station] ERROR creating meal: " + e.getMessage());
                }
                
                // Clear current recipe and task
                currentRecipe = null;
                currentTask = null;
                cookingProgress = 0;
            } else {
                // Only the task is complete, but more tasks remain for the recipe
                currentTask = null;
                cookingProgress = 0;
            }
        }
    }
}
