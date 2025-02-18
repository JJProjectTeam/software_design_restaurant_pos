package com.softwaredesign.project;

import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.staff.chefstrategies.*;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;

import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        // Initialize core components
        InventoryService inventoryService = new Inventory();
        
        // Add ALL possible ingredients to inventory
        inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Ketchup", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Onion", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Pickle", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Mayo", 10, 0.5, StationType.PREP);

        System.out.println("Inventory: " + inventoryService.getStock("Beef Patty"));

        Menu menu = new Menu(inventoryService);
        CollectionPoint collectionPoint = new CollectionPoint();
        StationManager stationManager = new StationManager();
        OrderManager orderManager = new OrderManager(collectionPoint, stationManager);
        
        // Create seating plan with enough capacity
        System.out.println("Creating seating plan...");
        SeatingPlan seatingPlan = new SeatingPlan(5, 10, menu); 
        
        // Create and assign waiters
        System.out.println("\nCreating waiters...");
        List<Waiter> waiters = new ArrayList<>();
        waiters.add(new Waiter(15.0, 1.0, orderManager, menu));
        waiters.add(new Waiter(15.0, 1.0, orderManager, menu));
        
        // Assign tables to waiters
        int waiterIndex = 0;
        for (Table table : seatingPlan.getAllTables()) {
            waiters.get(waiterIndex).assignTable(table);
            System.out.println("Table " + table.getTableNumber() + " assigned to waiter " + waiterIndex);
            waiterIndex = (waiterIndex + 1) % waiters.size();
        }
        
        // Create and seat customers
        System.out.println("\nSeating customers...");
        // Group of 2
        List<DineInCustomer> group1 = new ArrayList<>();
        group1.add(new DineInCustomer());
        group1.add(new DineInCustomer());
        Table table1 = seatingPlan.findTableForGroup(group1);
        if (table1 != null) {
            System.out.println("Group of 2 seated at table " + table1.getTableNumber());
        } else {
            System.out.println("No table available for group of 2");
            return;
        }
        
        // Group of 4
        List<DineInCustomer> group2 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            group2.add(new DineInCustomer());
        }
        Table table2 = seatingPlan.findTableForGroup(group2);
        if (table2 != null) {
            System.out.println("Group of 4 seated at table " + table2.getTableNumber());
        } else {
            System.out.println("No table available for group of 4");
            return;
        }
        
        // Have customers finish browsing
        System.out.println("\nCustomers browsing menus...");
        for (Table table : seatingPlan.getAllTables()) {
            for (DineInCustomer customer : table.getCustomers()) {
                customer.finishBrowsing();
                System.out.println("Customer at table " + table.getTableNumber() + " finished browsing");
            }
        }

        // Take orders
        System.out.println("\nTaking orders...");
        for (Waiter waiter : waiters) {
            for (Table table : waiter.getAssignedTables()) {
                if (!table.getCustomers().isEmpty()) {
                    System.out.println("Waiter taking order from table " + table.getTableNumber());
                    waiter.takeTableOrder(table);
                }
            }
        }
        
        // Demonstrate kitchen operations (separate from front-of-house)
        System.out.println("\nDemonstrating kitchen operations...");
        
        // Create chefs with different strategies
        List<Chef> chefs = new ArrayList<>();
        chefs.add(new Chef(20.0, 1.5, new ShortestQueueFirst(), stationManager));
        chefs.add(new Chef(20.0, 1.5, new LongestQueueFirstStrategy(), stationManager));
        chefs.add(new Chef(20.0, 1.5, new OldestOrderFirstStrategy(), stationManager));
        
        // Assign stations to chefs
        for (Chef chef : chefs) {
            chef.assignToStation(StationType.GRILL);
            chef.assignToStation(StationType.PREP);
            System.out.println("Chef assigned to grill and prep stations");
            
            // Demonstrate different working strategies
            Station nextStation = chef.chooseNextStation();
            System.out.println("Chef chose " + nextStation + " based on their strategy");
        }
    }
}
