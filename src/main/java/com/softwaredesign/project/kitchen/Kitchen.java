package com.softwaredesign.project.kitchen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.staff.Chef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kitchen extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(Kitchen.class);
    private OrderManager orderManager;
    private CollectionPoint collectionPoint;
    private StationManager stationManager;
    private List<Recipe> pendingRecipes = new ArrayList<>(); // recipes to be complete 
    private Map<Recipe, List<RecipeTask>> pendingTasks = new HashMap<>(); // tasks to be completed
    
    public Kitchen(OrderManager orderManager, CollectionPoint collectionPoint, StationManager stationManager) {
        this.orderManager = orderManager;
        this.collectionPoint = collectionPoint;
        this.stationManager = stationManager;
    }
    
    public void setOrderManager(OrderManager orderManager) {
        this.orderManager = orderManager;
    }
    
    public StationManager getStationManager() {
        return stationManager;
    }
    
    /**
     * Gets the order manager for this kitchen
     * @return The OrderManager instance
     */
    public OrderManager getOrderManager() {
        return orderManager;
    }

    /**
     * Gets the recipes from the order manager
     */
    public void getRecipes() {
        if (orderManager == null) {
            logger.info("OrderManager is not set");
            return;
        }
        
        List<Recipe> newRecipes = orderManager.processOrder(); // returns a registered order (list of recipes)
        logger.info("[DEBUG-KITCHEN] Got " + (newRecipes != null ? newRecipes.size() : 0) + " new recipes from OrderManager");
        
        if (newRecipes != null && !newRecipes.isEmpty()) { // if there are new recipes
            // Print details about each new recipe
            for (Recipe recipe : newRecipes) {
                logger.info("[DEBUG-KITCHEN] New recipe: " + recipe.getName() + 
                                  ", orderId: " + recipe.getOrderId() + 
                                  ", hashCode: " + recipe.hashCode());
            }
            
            // Log the state of pendingRecipes before adding new ones
            logger.info("[DEBUG-KITCHEN] pendingRecipes before adding new ones (size: " + 
                              pendingRecipes.size() + "):");
            for (Recipe recipe : pendingRecipes) {
                logger.info("  - " + recipe.getName() + 
                                  " (orderId: " + recipe.getOrderId() + 
                                  ", hashCode: " + recipe.hashCode() + ")");
            }
            
            pendingRecipes.addAll(newRecipes); 
            
            // Log the updated pendingRecipes
            logger.info("[DEBUG-KITCHEN] pendingRecipes after adding new ones (size: " + 
                              pendingRecipes.size() + "):");
            for (Recipe recipe : pendingRecipes) {
                logger.info("  - " + recipe.getName() + 
                                  " (orderId: " + recipe.getOrderId() + 
                                  ", hashCode: " + recipe.hashCode() + ")");
            }
            
            // Add all incomplete tasks from new recipes to pending tasks (unpack!)
            for (Recipe recipe : newRecipes) {
                List<RecipeTask> incompleteTasks = recipe.getIncompleteTasks();
                logger.info("[DEBUG-KITCHEN] Recipe " + recipe.getName() + 
                                  " (orderId: " + recipe.getOrderId() + 
                                  ") has " + incompleteTasks.size() + " incomplete tasks");
                
                // Check if we already have this recipe in pendingTasks
                if (pendingTasks.containsKey(recipe)) {
                    logger.info("[DEBUG-KITCHEN] WARNING: Recipe " + recipe.getName() + 
                                      " with orderId " + recipe.getOrderId() + 
                                      " already exists in pendingTasks map!");
                    
                    // Check which recipe in the map is equal to this one
                    for (Recipe existingRecipe : pendingTasks.keySet()) {
                        if (existingRecipe.equals(recipe)) {
                            logger.info("[DEBUG-KITCHEN] Found equal recipe: " + existingRecipe.getName() + 
                                              " with orderId " + existingRecipe.getOrderId());
                        }
                    }
                }
                
                pendingTasks.put(recipe, new ArrayList<>(incompleteTasks));
            }
            
            logger.info("Kitchen received " + newRecipes.size() + " new recipes");
        }
    }
    
    public void assignChefToStation(Chef chef, StationType stationType) {
        Station station = stationManager.getStation(stationType);
        if (station != null) {
            // We now handle this differently - we're adding to the list of stations a chef can work at,
            // NOT actively assigning them to work at a station right now.
            
            // DON'T do this anymore - this would make the chef actively work at a station
            // station.registerChef(chef);
            
            // Instead, just add the station to the chef's assigned stations list
            chef.assignToStation(stationType);
            logger.info("Chef assigned to " + stationType + " station pool");
        }
    }
    
    public void unassignChefFromStation(Chef chef, StationType stationType) {
        Station station = stationManager.getStation(stationType);
        if (station != null && station.getAssignedChef() == chef) {
            station.unregisterChef();
            logger.info("Chef unassigned from " + stationType + " station");
        }
    }
    
    private void assignRecipesToStations() {
        // First distribute tasks from recipes that already have some tasks in progress
        distributeInProgressRecipeTasks();
        
        // Then distribute tasks from new recipes
        distributeNewRecipeTasks();
    }

    private void distributeInProgressRecipeTasks() {
        // First, find recipes that have some tasks completed but not all
        List<Recipe> inProgressRecipes = new ArrayList<>();
        for (Recipe recipe : pendingTasks.keySet()) {
            if (!recipe.allTasksCompleted() && 
                recipe.getTasks().size() > pendingTasks.get(recipe).size()) {
                inProgressRecipes.add(recipe);
            }
        }
        
        // Distribute tasks from in-progress recipes
        for (Recipe recipe : inProgressRecipes) {
            List<RecipeTask> recipeTasks = pendingTasks.get(recipe);
            if (recipeTasks == null || recipeTasks.isEmpty()) {
                continue;
            }
            
            List<RecipeTask> assignedTasks = new ArrayList<>();
            
            for (RecipeTask task : recipeTasks) {
                // Instead of auto-assigning, add the task to the designated station's backlog
                Station station = stationManager.getStation(task.getStationType());
                if (station != null) {
                    station.addTask(task);
                    assignedTasks.add(task);
                    logger.info("[DEBUG] Queued task " + task.getName() + " in " + task.getStationType() + " station backlog");
                }
            }
            
            // Remove assigned tasks from pending list
            recipeTasks.removeAll(assignedTasks);
        }
    }
    
    private void distributeNewRecipeTasks() {
        if (pendingRecipes.isEmpty()) {
            return;
        }
        
        List<Recipe> assignedRecipes = new ArrayList<>();
        
        for (Recipe recipe : pendingRecipes) {
            List<RecipeTask> recipeTasks = pendingTasks.get(recipe);
            if (recipeTasks == null || recipeTasks.isEmpty()) {
                // All tasks have been assigned, remove recipe from pending
                assignedRecipes.add(recipe);
                continue;
            }
            
            boolean assignedAnyTask = false;
            List<RecipeTask> assignedTasks = new ArrayList<>();
            
            for (RecipeTask task : recipeTasks) {
                // Update dependencies status and check if they are met before queuing
                task.updateDependenciesStatus();
                if (task.areDependenciesMet()) {
                    // Only add the task to the station's backlog if dependencies are met
                    Station station = stationManager.getStation(task.getStationType());
                    if (station != null) {
                        station.addTask(task);
                        assignedTasks.add(task);
                        assignedAnyTask = true;
                        logger.info("[DEBUG] Queued new task " + task.getName() + " in " + task.getStationType() + " station backlog (dependencies met)");
                    }
                } else {
                    logger.info("[DEBUG] Task " + task.getName() + " for " + recipe.getName() + " has unmet dependencies, not queuing yet");
                }
            }
            
            // Remove assigned tasks from pending list
            recipeTasks.removeAll(assignedTasks);
            
            // If we've assigned all tasks, remove the recipe from pending
            if (recipeTasks.isEmpty()) {
                assignedRecipes.add(recipe);
            } 
            // If we've assigned at least one task, but not all, keep the recipe in pending
            else if (assignedAnyTask) {
                // Recipe stays in pendingRecipes but with updated pending tasks
            } 
            // If we couldn't assign any tasks, leave everything as is
        }
        
        pendingRecipes.removeAll(assignedRecipes);
    }
    
    private Station findAvailableStationForTask(RecipeTask task) {
        // First check if the task's dependencies are met
        if (!task.areDependenciesMet()) {
            return null; // Can't assign a task whose dependencies aren't met
        }
        
        List<Station> stations = stationManager.getStationsByType(task.getStationType());
        
        if (stations.isEmpty()) {
            logger.info("[ERROR] No stations found for type " + task.getStationType() + " for task " + task.getName());
            return null;
        }
        
        // First preference: Station that's not busy and has a chef ready
        for (Station station : stations) {
            if (!station.isBusy() && station.hasChef() && !station.getAssignedChef().isWorking()) {
                logger.info("[DEBUG] Found optimal station " + station.getType() + " for task " + 
                                  task.getName() + " (not busy with available chef)");
                return station;
            }
        }
        
        // Second preference: Station that's not busy (even if it doesn't have a chef yet)
        // This will allow the task to be queued up for when a chef arrives
        for (Station station : stations) {
            if (!station.isBusy()) {
                logger.info("[DEBUG] Found available station " + station.getType() + " for task " + 
                                  task.getName() + " (no chef currently assigned)");
                return station;
            }
        }
        
        // No available station found, log that information
        logger.info("[DEBUG] All stations of type " + task.getStationType() + 
                          " are busy. Task " + task.getName() + " will wait.");
        return null;
    }
    
    private void provideIngredientsToStations() {
        for (Station station : stationManager.getAllStations()) {
            if (station.needsIngredients()) {
                station.provideIngredients();
            }
        }
    }

    @Override
    public void readState() {
        logger.info("Reading Kitchen state");
        // Get new recipes from order manager
        getRecipes();
    }

    @Override
    public void writeState() {
        logger.info("Writing Kitchen state");
        // Check for tasks that were previously waiting for dependencies that are now met
        updateTaskAvailability();
        
        // Assign recipe tasks to available stations with chefs
        assignRecipesToStations();
        
        // Provide ingredients to stations that need them
        provideIngredientsToStations();
        
        // Actively check for any stations that are idle and try to assign tasks to them
        checkAndAssignTasks();
    }
    
    /**
     * Checks for any idle stations and tries to assign them tasks from pending orders.
     * This method is particularly important for ensuring that new orders flow through
     * the system even when no dependencies or other triggers have changed.
     */
    private void checkAndAssignTasks() {
        logger.info("\n[DEBUG] Checking for idle stations to assign tasks to...");
        
        // Get all stations from the station manager
        for (StationType stationType : StationType.values()) {
            List<Station> stations = stationManager.getStationsByType(stationType);
            
            for (Station station : stations) {
                // Skip stations that are already busy
                if (station == null || station.isBusy()) {
                    continue;
                }
                
                // If this is a PREP station, log with higher visibility
                if (stationType == StationType.PREP) {
                    logger.info("[IMPORTANT] PREP station is idle, looking for tasks to assign");
                } else {
                    logger.info("[DEBUG] " + stationType + " station is idle, looking for tasks to assign");
                }
                
                // Check all orders for tasks that could be assigned to this station
                for (Order order : orderManager.getPendingOrders()) {
                    for (Recipe recipe : order.getRecipes()) {
                        // Skip completed recipes
                        if (recipe.isComplete()) {
                            continue;
                        }
                        
                        // Look for tasks that match this station type
                        for (RecipeTask task : recipe.getUncompletedTasks()) {
                            if (task.getStationType() == stationType && 
                                !task.isAssigned() && 
                                !task.isCompleted()) {
                                
                                // Update dependencies status to be sure
                                task.updateDependenciesStatus();
                                
                                if (task.areDependenciesMet()) {
                                    logger.info("[DEBUG] Found task " + task.getName() + 
                                                     " for idle " + stationType + " station" +
                                                     " from order: " + recipe.getOrderId());
                                    
                                    // Assign the task to the station
                                    station.assignTask(recipe, task);
                                    
                                    // If this is a PREP task, log it with higher visibility
                                    if (stationType == StationType.PREP) {
                                        logger.info("[IMPORTANT] Successfully assigned PREP task " + 
                                                         task.getName() + " to PREP station");
                                    }
                                    
                                    // If we assigned a task, break out of the task loop
                                    // but continue checking other stations
                                    break;
                                }
                            }
                        }
                        
                        // If station is now busy, break out of recipe loop
                        if (station.isBusy()) {
                            break;
                        }
                    }
                    
                    // If station is now busy, break out of order loop
                    if (station.isBusy()) {
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Checks if any tasks that were previously not available due to dependencies
     * are now ready to be assigned
     */
    public void updateTaskAvailability() {
        for (Recipe recipe : pendingRecipes) {
            // Skip completed recipes
            if (recipe.isComplete()) {
                continue;
            }
            
            // Check for tasks that have dependencies now met
            List<RecipeTask> readyTasks = new ArrayList<>();
            for (RecipeTask task : recipe.getUncompletedTasks()) {
                // Update the dependencies status for this task
                task.updateDependenciesStatus();
                
                if (task.areDependenciesMet() && !task.isCompleted()) {
                    readyTasks.add(task);
                    logger.info("Task is now available: " + task.getName() + " for " + recipe.getName() + 
                                    " (dependencies met)");
                    
                    // Always ensure the order is in the station's backlog for any ready task
                    StationType stationType = task.getStationType();
                    Station station = stationManager.getStation(stationType);
                    if (station != null) {
                        String orderId = recipe.getOrderId();
                        if (orderId != null) {
                            boolean taskAlreadyInBacklog = false;
                            
                            // Check if this specific task is already in the backlog
                            for (RecipeTask backlogTask : station.getBacklog()) {
                                // Check if it's the same task (same name and same station type)
                                if (backlogTask.getName().equals(task.getName()) && 
                                    backlogTask.getStationType() == task.getStationType()) {
                                    Recipe backlogRecipe = backlogTask.getRecipe();
                                    Recipe currentRecipe = task.getRecipe();
                                    
                                    // If they have the same recipe or order ID, it's the same task
                                    if ((backlogRecipe != null && currentRecipe != null && 
                                         backlogRecipe.equals(currentRecipe)) ||
                                        (backlogRecipe != null && currentRecipe != null && 
                                         orderId.equals(backlogRecipe.getOrderId()))) {
                                        taskAlreadyInBacklog = true;
                                        break;
                                    }
                                }
                            }
                            
                            // If this specific task is not in backlog, add it
                            if (!taskAlreadyInBacklog) {
                                // FIXED: Add the task to the station backlog directly without checking
                                // if the order is still in the pending orders queue
                                logger.info("[DEBUG] Adding order " + orderId + " to " + 
                                            stationType + " station backlog (task now ready)");
                                // Only add the specific task that's ready, not all tasks from the order
                                station.addTask(task);
                            }
                        }
                    }
                }
            }
        }
    }
}
