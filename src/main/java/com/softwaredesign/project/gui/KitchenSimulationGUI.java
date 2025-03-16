package com.softwaredesign.project.gui;

import com.softwaredesign.project.engine.KitchenSimulator;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.ChefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple GUI to show the kitchen simulation status.
 */
public class KitchenSimulationGUI extends JFrame {
    private KitchenSimulator simulator;
    private JTextArea prepStationArea;
    private JTextArea grillStationArea;
    private JTextArea plateStationArea;
    private JTextArea ordersArea;
    private JTextArea chefsArea;
    private JTextArea inProgressOrdersArea;
    private JButton stepButton;
    
    // Refresh rate in milliseconds
    private static final int REFRESH_RATE = 1000;

    public KitchenSimulationGUI(KitchenSimulator simulator) {
        this.simulator = simulator;
        
        // Set up the frame
        setTitle("Restaurant POS - Kitchen Simulation");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create the panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create the stations panel with a border for visibility
        JPanel stationsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        stationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Prep station panel
        JPanel prepPanel = createStationPanel("PREP Station");
        prepPanel.setBackground(new Color(230, 240, 255)); // Light blue
        prepStationArea = new JTextArea();
        prepStationArea.setEditable(false);
        prepStationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        prepPanel.add(new JScrollPane(prepStationArea), BorderLayout.CENTER);
        stationsPanel.add(prepPanel);
        
        // Grill station panel
        JPanel grillPanel = createStationPanel("GRILL Station");
        grillPanel.setBackground(new Color(255, 230, 230)); // Light red
        grillStationArea = new JTextArea();
        grillStationArea.setEditable(false);
        grillStationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        grillPanel.add(new JScrollPane(grillStationArea), BorderLayout.CENTER);
        stationsPanel.add(grillPanel);
        
        // Plate station panel
        JPanel platePanel = createStationPanel("PLATE Station");
        platePanel.setBackground(new Color(230, 255, 230)); // Light green
        plateStationArea = new JTextArea();
        plateStationArea.setEditable(false);
        plateStationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        platePanel.add(new JScrollPane(plateStationArea), BorderLayout.CENTER);
        stationsPanel.add(platePanel);
        
        // Create the orders panel
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBorder(BorderFactory.createTitledBorder("Current Orders"));
        ordersPanel.setBackground(new Color(255, 255, 230)); // Light yellow
        ordersArea = new JTextArea();
        ordersArea.setEditable(false);
        ordersArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ordersPanel.add(new JScrollPane(ordersArea), BorderLayout.CENTER);
        
        // Create the chefs panel
        JPanel chefsPanel = new JPanel(new BorderLayout());
        chefsPanel.setBorder(BorderFactory.createTitledBorder("Chefs"));
        chefsPanel.setBackground(new Color(255, 240, 215)); // Light orange
        chefsArea = new JTextArea();
        chefsArea.setEditable(false);
        chefsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chefsPanel.add(new JScrollPane(chefsArea), BorderLayout.CENTER);
        
        // Create control panel with step button
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        stepButton = new JButton("Step Simulation");
        stepButton.addActionListener(e -> {
            simulator.step();
            updateDisplay();
        });
        controlPanel.add(stepButton);
        
        // Create in-progress orders panel
        JPanel inProgressOrdersPanel = new JPanel(new BorderLayout());
        inProgressOrdersPanel.setBorder(BorderFactory.createTitledBorder("In-Progress Orders"));
        inProgressOrdersPanel.setBackground(new Color(245, 245, 255)); // Light lavender
        inProgressOrdersArea = new JTextArea();
        inProgressOrdersArea.setEditable(false);
        inProgressOrdersArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        inProgressOrdersPanel.add(new JScrollPane(inProgressOrdersArea), BorderLayout.CENTER);
        
        // Create bottom panel to hold orders, in-progress orders, and chefs
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        bottomPanel.add(ordersPanel);
        bottomPanel.add(inProgressOrdersPanel);
        bottomPanel.add(chefsPanel);
        
        // Add panels to main panel
        mainPanel.add(stationsPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Initial update of display
        updateDisplay();
        
        // Start the update timer
        startUpdateTimer();
        
        // Debug message
        System.out.println("GUI initialized and first update completed");
    }
    
    private JPanel createStationPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2), 
            title,
            TitledBorder.CENTER, 
            TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 14)));
        return panel;
    }
    
    private void startUpdateTimer() {
        // Create a timer to update the display regularly
        Timer timer = new Timer(REFRESH_RATE, e -> updateDisplay());
        timer.start();
    }
    
    private void updateDisplay() {
        try {
            // Debug message
            System.out.println("Updating GUI display...");
            
            // Update the station displays
            updateStationArea(StationType.PREP, prepStationArea);
            updateStationArea(StationType.GRILL, grillStationArea);
            updateStationArea(StationType.PLATE, plateStationArea);
            
            // Update the orders display
            updateOrdersArea();
            
            // Update the in-progress orders display
            updateInProgressOrdersArea();
            
            // Update the chefs display
            updateChefsArea();
            
            // Force repaint
            repaint();
        } catch (Exception e) {
            System.err.println("Error updating GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStationArea(StationType type, JTextArea area) {
        Station station = simulator.getKitchen().getStationManager().getStation(type);
        StringBuilder sb = new StringBuilder();
        
        // Add the chef information
        sb.append("Chef: ");
        if (station.getAssignedChef() != null) {
            sb.append(station.getAssignedChef().getName());
        } else {
            sb.append("None");
        }
        sb.append("\n\n");
        
        // Add the current task information
        sb.append("Current Task: ");
        if (station.isBusy()) {
            RecipeTask task = station.getCurrentTask();
            if (task != null) {
                sb.append(task.getName())
                  .append(" (")
                  .append(station.getCookingProgress())
                  .append("/")
                  .append(task.getCookingWorkRequired())
                  .append(")");
            } else {
                sb.append("None");
            }
        } else {
            sb.append("None");
        }
        sb.append("\n\n");
        
        // Add the backlog information
        sb.append("Backlog: ").append(station.getBacklogSize()).append(" tasks\n");
        List<RecipeTask> backlog = station.getBacklog();
        
        // Group tasks by recipe/order for better readability
        Map<String, List<RecipeTask>> tasksByOrderId = new HashMap<>();
        
        for (RecipeTask task : backlog) {
            Recipe recipe = task.getRecipe();
            String orderId = (recipe != null) ? recipe.getOrderId() : "Unknown Order";
            
            if (!tasksByOrderId.containsKey(orderId)) {
                tasksByOrderId.put(orderId, new ArrayList<>());
            }
            tasksByOrderId.get(orderId).add(task);
        }
        
        // Display tasks grouped by order
        for (Map.Entry<String, List<RecipeTask>> entry : tasksByOrderId.entrySet()) {
            sb.append("- Order: ").append(entry.getKey()).append("\n");
            for (RecipeTask task : entry.getValue()) {
                sb.append("  * ").append(task.getName());
                sb.append(" (").append(task.getStationType()).append(")\n");
            }
        }
        
        area.setText(sb.toString());
    }
    
    private void updateOrdersArea() {
        OrderManager orderManager = simulator.getOrderManager();
        Queue<Order> orders = orderManager.getPendingOrders();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Pending Orders: ").append(orders.size()).append("\n\n");
        
        for (Order order : orders) {
            sb.append("Order ID: ").append(order.getOrderId()).append("\n");
            sb.append("Recipes: ").append(order.getRecipes().size()).append("\n");
            sb.append("-------------------\n");
        }
        
        ordersArea.setText(sb.toString());
    }
    
    private void updateInProgressOrdersArea() {
        // Get information about orders in progress
        StationManager stationManager = simulator.getKitchen().getStationManager();
        
        // Map to track orders and their progress
        Map<String, Map<String, String>> orderProgressMap = new HashMap<>();
        
        // Check all stations for orders being processed
        for (StationType type : StationType.values()) {
            Station station = stationManager.getStation(type);
            
            // Get current order being processed
            if (station.isBusy() && station.getCurrentRecipe() != null) {
                String orderId = station.getCurrentRecipe().getOrderId();
                String recipeName = station.getCurrentRecipe().getName();
                RecipeTask task = station.getCurrentTask();
                
                if (orderId != null && task != null) {
                    // Create recipe entry if it doesn't exist for this order
                    orderProgressMap.putIfAbsent(orderId, new HashMap<>());
                    
                    // Add progress for this recipe
                    String progress = String.format("%s: %s task at %s (%d/%d)", 
                        recipeName, 
                        task.getName(), 
                        station.getType(),
                        station.getCookingProgress(),
                        task.getCookingWorkRequired());
                    
                    orderProgressMap.get(orderId).put(recipeName, progress);
                }
            }
            
            // Check backlog for more tasks
            for (RecipeTask task : station.getBacklog()) {
                Recipe recipe = task.getRecipe();
                if (recipe != null) {
                    String orderId = recipe.getOrderId();
                    if (orderId != null) {
                        // Create recipe entry if it doesn't exist for this order
                        orderProgressMap.putIfAbsent(orderId, new HashMap<>());
                        
                        // Add waiting status for this recipe and task
                        String status = String.format("%s - %s: Waiting at %s", 
                            recipe.getName(),
                            task.getName(),
                            station.getType());
                        
                        String recipeKey = recipe.getName() + " (" + task.getName() + ")";
                        orderProgressMap.get(orderId).put(recipeKey, status);
                    }
                }
            }
        }
        
        // Check collection point for completed orders awaiting other parts
        CollectionPoint collectionPoint = simulator.getCollectionPoint();
        
        // Get orders that are in the collection point (partially ready)
        for (String orderId : collectionPoint.getPartiallyCompletedOrderIds()) {
            // Create entry if it doesn't exist for this order
            orderProgressMap.putIfAbsent(orderId, new HashMap<>());
            
            // Get completed meals count and total expected
            int completed = collectionPoint.getCompletedMealsCount(orderId);
            int total = collectionPoint.getTotalMealsExpected(orderId);
            
            // Add completion status for this order
            String status = String.format("Completed: %d/%d dishes ready at Collection Point", 
                completed, total);
            
            // Use a special key that won't conflict with recipe names
            orderProgressMap.get(orderId).put("__completion_status__", status);
        }
        
        // Build the display text
        StringBuilder sb = new StringBuilder();
        sb.append("In-Progress Orders: ").append(orderProgressMap.size()).append("\n\n");
        
        if (orderProgressMap.isEmpty()) {
            sb.append("No orders in progress\n");
        } else {
            // Sort orders by ID for consistent display
            List<String> sortedOrderIds = new ArrayList<>(orderProgressMap.keySet());
            Collections.sort(sortedOrderIds);
            
            for (String orderId : sortedOrderIds) {
                sb.append("Order ID: ").append(orderId).append("\n");
                
                Map<String, String> recipeProgress = orderProgressMap.get(orderId);
                for (Map.Entry<String, String> entry : recipeProgress.entrySet()) {
                    sb.append("  ").append(entry.getValue()).append("\n");
                }
                
                sb.append("-------------------\n");
            }
        }
        
        inProgressOrdersArea.setText(sb.toString());
    }
    
    private void updateChefsArea() {
        ChefManager chefManager = simulator.getChefManager();
        List<Chef> chefs = chefManager.getAllChefs();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Kitchen Staff: ").append(chefs.size()).append(" chefs\n\n");
        
        for (Chef chef : chefs) {
            // Chef name and working status
            sb.append(chef.getName()).append("  ");
            if (chef.isWorking()) {
                sb.append("[BUSY]");
            } else {
                sb.append("[IDLE]");
            }
            sb.append("\n");
            
            // Current station and task information
            Station currentStation = chef.getCurrentStation();
            
            if (currentStation != null) {
                sb.append("  Station: ").append(currentStation.getType()).append("\n");
                
                // Verify chef is actually assigned to this station
                if (currentStation.getAssignedChef() != chef) {
                    sb.append("  WARNING: Station does not have this chef assigned!\n");
                }
                
                // Show task information if there is one
                if (currentStation.isBusy() && currentStation.getCurrentTask() != null && currentStation.getAssignedChef() == chef) {
                    RecipeTask task = currentStation.getCurrentTask();
                    Recipe recipe = currentStation.getCurrentRecipe();
                    
                    sb.append("  Task: ").append(task.getName())
                      .append(" (").append(currentStation.getCookingProgress())
                      .append("/").append(task.getCookingWorkRequired()).append(")\n");
                    
                    if (recipe != null) {
                        sb.append("  Recipe: ").append(recipe.getName());
                        if (recipe.getOrderId() != null) {
                            sb.append(" (Order: ").append(recipe.getOrderId()).append(")");
                        }
                        sb.append("\n");
                    }
                } else {
                    sb.append("  Task: None\n");
                }
            } else {
                sb.append("  Station: Not assigned\n");
                sb.append("  Task: None\n");
            }
            
            // Chef strategy information
            sb.append("  Strategy: ").append(chef.getWorkStrategy().getClass().getSimpleName()).append("\n");
            
            sb.append("-------------------\n");
        }
        
        chefsArea.setText(sb.toString());
    }
}
