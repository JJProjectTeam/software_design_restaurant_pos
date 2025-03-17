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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Station extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(Station.class);
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
            logger.info("[WARNING] Task added to backlog without recipe reference: " + task.getName());
            // Don't add tasks without a recipe reference
            logger.info("[FIX] Rejecting task without recipe reference: " + task.getName());
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
                logger.info("[DEBUG-STATION-ADD] Prevented duplicate task: " + task.getName() + 
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
        logger.info("\n[DEBUG-STATION] Registering chef " + chef.getName() + " at " + type + " station");
        logger.info("[DEBUG-STATION] Current assignedChef: " + 
            (assignedChef != null ? assignedChef.getName() : "NONE"));
        logger.info("[DEBUG-STATION] Chef's current station: " + 
            (chef.getCurrentStation() != null ? chef.getCurrentStation().getType() : "NONE"));
        
        // If we already have a chef assigned, unregister them first
        if (assignedChef != null && assignedChef != chef) {
            logger.info("[DEBUG-STATION] Station already has chef " + assignedChef.getName() + ", unregistering them");
            // Log if a task is being reassigned
            if (currentTask != null) {
                logger.info("Chef " + assignedChef.getName() + " has left " + type + " station, task reassigned");
            }
            // Unregister the current chef
            assignedChef.removeStationAssignment(this);
            assignedChef = null;
        }
        
        // Check if the chef is already assigned to another station and unregister them from there
        Station currentChefStation = chef.getCurrentStation();
        if (currentChefStation != null && currentChefStation != this) {
            logger.info("[DEBUG-STATION] Chef " + chef.getName() + " is currently at " + 
                currentChefStation.getType() + " station, unregistering from there");
            logger.info("Chef " + chef.getName() + " was at " + currentChefStation.getType() + 
                              " station but is now moving to " + type + " station");
            
            // Handle the working status - we need to check if the chef is actively working
            if (chef.isWorking()) {
                logger.info("[DEBUG-STATION-SYNC] Chef " + chef.getName() + " is still marked as working. " +
                    "Checking if they're the taskChef at their current station...");
                
                // Check if this chef is the taskChef at their current station
                if (currentChefStation.getCurrentTask() != null) {
                    logger.info("[DEBUG-STATION-SYNC] Chef's current station has an active task.");
                    
                    // Do NOT unregister the chef while they're in the middle of a task
                    logger.info("[DEBUG-STATION-SYNC] Chef " + chef.getName() + 
                        " is in the middle of a task. Cannot reassign until task is complete.");
                    return;
                } else {
                    // No active task, safe to reset working status
                    chef.setWorking(false);
                    logger.info("[DEBUG-STATION-SYNC] Reset working status for chef " + chef.getName());
                }
            }
            
            // Unregister from the other station
            if (currentChefStation.getAssignedChef() == chef) {
                logger.info("[DEBUG-STATION] Unregistering chef from " + currentChefStation.getType());
                currentChefStation.unregisterChef();
            } else {
                logger.info("[DEBUG-STATION] WARNING: Chef thinks they're at " + 
                    currentChefStation.getType() + " but that station doesn't have them assigned!");
                
                // Force update the chef's current station reference to avoid inconsistency
                chef.setCurrentStation(null);
            }
        }
        
        // Set this station as the chef's current station
        logger.info("[DEBUG-STATION] Setting " + type + " as current station for " + chef.getName());
        chef.setCurrentStation(this);
        assignedChef = chef;
        
        logger.info("Chef " + chef.getName() + " is now registered at " + type + " station");
        
        // If there's a task but no chef was assigned to it yet, assign this chef
        if (currentTask != null && taskChef == null) {
            logger.info("[DEBUG-STATION] Assigning chef to task: " + currentTask.getName());
            taskChef = chef;
            chef.setWorking(true);
        }
        
        // Verify the registration worked
        logger.info("[DEBUG-STATION] After registration: " + 
            chef.getName() + " current station is " + 
            (chef.getCurrentStation() != null ? chef.getCurrentStation().getType() : "NONE") + 
            ", station's assigned chef is " + 
            (assignedChef != null ? assignedChef.getName() : "NONE"));
    }

    public void unregisterChef() {
        logger.info("\n[DEBUG-STATION] Unregistering chef from " + type + " station");
        logger.info("[DEBUG-STATION] Current assignedChef: " + 
            (assignedChef != null ? assignedChef.getName() : "NONE"));
        logger.info("[DEBUG-STATION] Current taskChef: " + 
            (taskChef != null ? taskChef.getName() : "NONE"));
        
        // Special case: if there's an active task and taskChef is set,
        // we should NOT unregister if the assignedChef is the taskChef
        if (currentTask != null && taskChef != null && assignedChef == taskChef) {
            logger.info("[DEBUG-STATION] WARNING: Cannot unregister chef " + 
                assignedChef.getName() + " as they are actively cooking a task");
            return;
        }
        
        if (assignedChef != null) {
            logger.info("[DEBUG-STATION] Found assigned chef: " + assignedChef.getName());
            
            // If this chef considers this their current station, update their state
            if (assignedChef.getCurrentStation() == this) {
                logger.info("[DEBUG-STATION] Chef " + assignedChef.getName() + 
                    " considers this their current station, clearing reference");
                logger.info("Chef " + assignedChef.getName() + " is no longer registered at " + type + " station");
                // Clear the chef's station reference
                assignedChef.setCurrentStation(null);
            } else {
                logger.info("[DEBUG-STATION] WARNING: Chef " + assignedChef.getName() + 
                    " is assigned here but thinks they're at " + 
                    (assignedChef.getCurrentStation() != null ? 
                        assignedChef.getCurrentStation().getType() : "NONE"));
                
                // Fix the inconsistency - if the chef thinks they're at a different station,
                // but we have them as assigned, clear our reference
            }
            
            Chef departingChef = assignedChef;
            logger.info("[DEBUG-STATION] Setting assignedChef to null");
            assignedChef = null;
            
            // Make sure the chef's assigned stations list no longer contains this station
            logger.info("[DEBUG-STATION] Removing station from chef's assigned stations list");
            departingChef.removeStationAssignment(this);
            
            // Reset working status if appropriate
            if (departingChef.isWorking() && (taskChef == null || taskChef != departingChef)) {
                departingChef.setWorking(false);
                logger.info("[DEBUG-STATION] Reset working status for departing chef " + departingChef.getName());
            }
            
            logger.info("[DEBUG-STATION] After unregistration: Chef " + departingChef.getName() + 
                " current station is " + 
                (departingChef.getCurrentStation() != null ? 
                    departingChef.getCurrentStation().getType() : "NONE"));
        } else {
            logger.info("[DEBUG-STATION] No chef assigned to unregister");
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
                    
                    logger.info(type + " station assigned task: " + task.getName() + 
                                      " for recipe: " + recipe.getName());
                    return;
                }
            }
            
            // No tasks for this station type in this recipe
            logger.info(type + " station has no tasks in recipe: " + recipe.getName());
        } else {
            logger.info(type + " station is busy with another task");
        }
    }
    
    /**
     * Assigns a task from a recipe to this station
     * @param recipe The recipe the task belongs to
     * @param task The task to assign
     */
    public void assignTask(Recipe recipe, RecipeTask task) {
        if (currentTask != null) {
            logger.info("[DEBUG-STATION] " + type + " station already has task " + 
                currentTask.getName() + ", not assigning " + task.getName());
            
            // Add to backlog instead
            if (!backlog.contains(task)) {
                backlog.add(task);
                logger.info("[DEBUG-STATION] Added " + task.getName() + " to " + type + " station backlog");
            }
            return;
        }
        
        logger.info("[DEBUG-STATION] Assigning task " + task.getName() + " to " + type + " station");
        currentRecipe = recipe;
        currentTask = task;
        cookingProgress = 0;
        needsIngredients = true;

        if (assignedChef != null) {
            assignedChef.setWorking(true);
            logger.info("[DEBUG-STATION] Chef " + assignedChef.getName() + 
                " is now working on task " + task.getName());
        } else {
            logger.info("[DEBUG-STATION] No chef assigned to " + type + 
                " station, task " + task.getName() + " waiting for chef");
        }
        
        // Ingredients will be provided by the kitchen during its writeState method
        // via provideIngredientsToStations()
        if (kitchen == null) {
            logger.info("[DEBUG-STATION] Kitchen reference is null, cannot request ingredients");
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
            logger.info(type + " station received ingredients for task: " + 
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
            
            // Calculate required work with a minimum threshold to prevent instant completion
            int taskWorkRequired = currentTask.getCookingWorkRequired();
            double chefSpeedMultiplier = assignedChef.getSpeedMultiplier();
            
            // Ensure a minimum required work of at least 1 tick
            int requiredWork = Math.max(1, (int) Math.round(taskWorkRequired / chefSpeedMultiplier));
            
            StringBuilder progressMessage = new StringBuilder();
            progressMessage.append(type).append(" station cooking ").append(currentTask.getName());
            progressMessage.append(" for ").append(currentRecipe.getName());
            progressMessage.append(" - Progress: ").append(cookingProgress).append("/").append(requiredWork);
            progressMessage.append(" (Task work: ").append(taskWorkRequired);
            progressMessage.append(", Chef speed: ").append(chefSpeedMultiplier).append(")");
            
            // Add chef information if available - use taskChef instead of assignedChef
            if (taskChef != null) {
                progressMessage.append(" (cooked by ").append(taskChef.getName()).append(")");
            }
            
            // Add order ID if available
            if (currentRecipe.getOrderId() != null) {
                progressMessage.append(" (Order ID: ").append(currentRecipe.getOrderId()).append(")");
            }
            
            logger.info(progressMessage.toString());
            
            if (cookingProgress >= requiredWork) {
                // Task is done
                StringBuilder completionMessage = new StringBuilder();
                completionMessage.append(type).append(" station completed task: ").append(currentTask.getName());
                completionMessage.append(" for ").append(currentRecipe.getName());
                completionMessage.append(" after ").append(cookingProgress).append(" ticks");
                
                // Add chef information if available - use taskChef instead of assignedChef
                if (taskChef != null) {
                    completionMessage.append(" (completed by ").append(taskChef.getName()).append(")");
                }
                
                // Add order ID if available
                if (currentRecipe.getOrderId() != null) {
                    completionMessage.append(" (Order ID: ").append(currentRecipe.getOrderId()).append(")");
                }
                
                logger.info(completionMessage.toString());
                
                // Mark the task as completed and no longer assigned
                currentTask.setCompleted(true);
                currentTask.setAssigned(false);
                Recipe completedRecipe = currentRecipe;
                
                // Check if the entire recipe is completed
                if (completedRecipe.allTasksCompleted()) {
                    // All tasks are complete, build the meal
                    logger.info("All tasks for " + completedRecipe.getName() + " are complete, building meal");
                    
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
                        logger.info("[ERROR] Failed to add meal to collection point: " + e.getMessage());
                        
                        // Try to re-register the order if needed
                        if (orderId != null && kitchen != null && kitchen.getOrderManager() != null) {
                            OrderManager orderManager = kitchen.getOrderManager();
                            for (Order order : orderManager.getPendingOrders()) {
                                if (order.getOrderId().equals(orderId)) {
                                    // Re-register the order with the collection point
                                    collectionPoint.registerOrder(orderId, order.getRecipes().size());
                                    logger.info("[DEBUG] Re-registered order " + orderId + " with collection point");
                                    
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
                                        logger.info("[DEBUG] Successfully added meal after re-registration");
                                    } catch (Exception ex) {
                                        logger.info("[ERROR] Failed to add meal even after re-registration: " + ex.getMessage());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    logger.info("Recipe " + completedRecipe.getName() + " still has tasks remaining");
                    
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
                                                logger.info("[DEBUG] Moving order " + orderId + " from " + type + 
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
                        logger.info("[DEBUG] No next station found for recipe " + completedRecipe.getName());
                    }
                }
                
                // Store current chef for later use
                Chef currentChef = taskChef;
                
                // Reset the working status of the chef BEFORE clearing references
                if (taskChef != null) {
                    taskChef.setWorking(false);
                }
                
                // Check if assignedChef and taskChef are different (which would indicate potential duplication)
                if (assignedChef != null && taskChef != null && assignedChef != taskChef) {
                    logger.info("[DEBUG-CHEF-SYNC] Possible duplication detected: " + 
                        "assignedChef is " + assignedChef.getName() + " but taskChef is " + taskChef.getName());
                    // Synchronize by clearing assignedChef if it's not the same as taskChef
                    // This ensures only the chef who did the work remains assigned
                    if (assignedChef.getCurrentStation() == this) {
                        assignedChef.setCurrentStation(null);
                    }
                    assignedChef = null;
                }
                
                // Reset station state
                currentTask = null;
                currentRecipe = null;
                taskChef = null;
                cookingProgress = 0;
                
                // Try to assign a new task for this station immediately
                tryAssignNewTask();
                
                // If we still don't have a task and the chef is still assigned here,
                // have them look for work elsewhere
                if (currentChef != null && !currentChef.isWorking()) {
                    logger.info("[DEBUG] Having chef " + currentChef.getName() + " look for new work");
                    currentChef.chooseNextStation();
                }
            }
        }
        // If we have a chef but no task, try to assign a new task
        else if (hasChef() && currentTask == null && !assignedChef.isWorking()) {
            tryAssignNewTask();
            
            // If we still don't have a task after trying to assign one, 
            // and we have a chef who isn't working, this might be a good time for them to look elsewhere
            if (currentTask == null && assignedChef != null && !assignedChef.isWorking()) {
                logger.info("[DEBUG-CHEF-IDLE] Chef " + assignedChef.getName() + 
                    " is idle at " + type + " station with no tasks. Releasing chef to look for work elsewhere.");
                
                // Store chef reference before clearing it
                Chef idleChef = assignedChef;
                
                // We should unregister this chef from the station since there's no work here
                unregisterChef();
                
                // Have the chef look for work elsewhere
                if (idleChef != null) {
                    idleChef.chooseNextStation();
                }
            }
        }
    }
    
    /**
     * Tries to pull the next task from the backlog queue if available.
     */
    private void tryAssignNewTask() {
        logger.info("[DEBUG] " + type + " station checking backlog for new tasks");
        
        // Print the current backlog to help diagnose the issue
        logger.info("[DEBUG-BACKLOG] " + type + " station backlog before assignment:");
        for (int i = 0; i < backlog.size(); i++) {
            RecipeTask t = backlog.get(i);
            Recipe r = t.getRecipe();
            String orderId = (r != null) ? r.getOrderId() : "unknown";
            logger.info("  " + i + ": " + t.getName() + " (Order: " + orderId + ")");
        }
        
        if (!backlog.isEmpty()) {
            // Get the first task (which should be the oldest order due to our sorting)
            RecipeTask nextTask = backlog.remove(0);
            Recipe recipe = nextTask.getRecipe();
            logger.info("[DEBUG] " + type + " station pulling queued task: " + nextTask.getName());
            currentRecipe = recipe;
            currentTask = nextTask;
            cookingProgress = 0; // Explicitly reset cooking progress to zero
            needsIngredients = true; // Always need ingredients for a new task
            
            // Additional logging to verify working values
            logger.info("[DEBUG-TASK-VALUES] New task initialized. cookingWorkRequired=" + 
                nextTask.getCookingWorkRequired() + ", chef speed=" + 
                (assignedChef != null ? assignedChef.getSpeedMultiplier() : "n/a") + 
                ", estimated required work=" + 
                (assignedChef != null ? 
                    Math.max(1, (int)Math.round(nextTask.getCookingWorkRequired() / assignedChef.getSpeedMultiplier())) : 
                    "n/a"));

            if (assignedChef != null) {
                assignedChef.setWorking(true);
                logger.info("[DEBUG-STATION] Chef " + assignedChef.getName() + " is now working on task " + nextTask.getName());
            } else {
                logger.info("[DEBUG-STATION] No chef assigned to " + type + " station, queued task " + nextTask.getName() + " remains waiting for chef assignment");
            }
            return;
        }
        
        // Instead of asking kitchen for new recipes, simply log that we're waiting for new tasks
        logger.info("[DEBUG] " + type + " station backlog is empty, waiting for new tasks");
        return;
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
                logger.info("[Station] Recipe complete: " + currentRecipe.getName());
                
                try {
                    // Build the meal using the recipe's buildMeal method
                    Meal meal = currentRecipe.buildMeal();
                    
                    logger.info("[Station] Adding completed meal to collection point: " + 
                        meal.getOrderId() + " - " + currentRecipe.getName());
                    
                    // This is the critical step - use the correct CollectionPoint
                    if (collectionPoint != null) {
                        collectionPoint.addCompletedMeal(meal);
                        logger.info("[Station] Successfully added meal to collection point");
                    } else {
                        logger.error("[Station] ERROR: CollectionPoint is null! Meal cannot be added.");
                    }
                } catch (Exception e) {
                            logger.error("[Station] ERROR creating meal: " + e.getMessage());
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
