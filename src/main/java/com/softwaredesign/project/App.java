package com.softwaredesign.project;

import com.softwaredesign.project.model.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.model.staff.Waiter;
import com.softwaredesign.project.model.staff.Chef;
import com.softwaredesign.project.model.customer.DineInCustomer;
import com.softwaredesign.project.model.menu.Menu;
import com.softwaredesign.project.model.orderfulfillment.Table;
import com.softwaredesign.project.model.placeholders.OrderManager;
import com.softwaredesign.project.model.staff.chefstrategies.*;
import com.softwaredesign.project.model.placeholders.Station;

import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        // Initialize core components
        Menu menu = new Menu();
        OrderManager orderManager = new OrderManager();
        
        // Create seating plan (15 seats across 5 tables)
        System.out.println("Creating seating plan...");
        SeatingPlan seatingPlan = new SeatingPlan(4, 10, menu);
        
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
        System.out.println("Group of 2 seated at table " + table1.getTableNumber());
        
        // Group of 4
        List<DineInCustomer> group2 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            group2.add(new DineInCustomer());
        }
        Table table2 = seatingPlan.findTableForGroup(group2);
        System.out.println("Group of 4 seated at table " + table2.getTableNumber());
        
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
        chefs.add(new Chef(20.0, 1.5, new ShortestQueueFirst()));
        chefs.add(new Chef(20.0, 1.5, new LongestQueueFirstStrategy()));
        chefs.add(new Chef(20.0, 1.5, new OldestOrderFirstStrategy()));
        
        // Create and assign stations
        Station grillStation = new Station();
        Station prepStation = new Station();
        Station plateStation = new Station();
        
        // Assign stations to chefs
        for (Chef chef : chefs) {
            chef.getAssignedStations().add(grillStation);
            chef.getAssignedStations().add(prepStation);
            System.out.println("Chef assigned to grill and prep stations");
            
            // Demonstrate different working strategies
            Station nextStation = chef.chooseNextStation();
            System.out.println("Chef chose " + nextStation + " based on their strategy");
        }
    }
}
