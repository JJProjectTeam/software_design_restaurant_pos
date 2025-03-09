package com.softwaredesign.project.orderfulfillment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.orderfulfillment.Table;

public class SeatingPlan {
    private List<Table> tables;
    private int maxTableCapacity;
    
    public SeatingPlan(int totalTables, int totalSeats, int maxTableCapacity, Menu menu) {
        this.maxTableCapacity = maxTableCapacity;
        if (totalTables <= 0 || totalSeats <= 0) {
            throw new IllegalArgumentException("Total tables and seats must be positive");
        }
        
        if (totalSeats < totalTables) {
            throw new IllegalArgumentException("Must have at least one seat per table");
        }

        tables = new ArrayList<>();
        Random random = new Random();
        
        // First, ensure each table gets at least one seat
        int remainingTables = totalTables;
        int remainingSeats = totalSeats - totalTables; // Reserve one seat per table
        
        // Create all tables with minimum 1 seat
        for (int i = 0; i < totalTables; i++) {
            tables.add(new Table(i + 1, menu, 1));
        }
        
        // Distribute remaining seats randomly
        while (remainingSeats > 0) {
            for (Table table : tables) {
                if (remainingSeats <= 0) break;
                if (table.getTableCapacity() < maxTableCapacity) {
                    int addSeats = Math.min(
                        random.nextInt(maxTableCapacity - table.getTableCapacity()) + 1,
                        remainingSeats
                    );
                    tables.set(tables.indexOf(table), 
                             new Table(table.getTableNumber(), menu, table.getTableCapacity() + addSeats));
                    remainingSeats -= addSeats;
                }
            }
        }
    }
    
    public Table findTableForGroup(List<DineInCustomer> customerGroup) {
        if (customerGroup == null || customerGroup.isEmpty()) {
            return null;
        }
        
        int groupSize = customerGroup.size();
        
        // Check if group is too large for any table
        if (groupSize > maxTableCapacity) {
            System.out.println("Sorry, we cannot accommodate groups larger than " + maxTableCapacity);
            return null;
        }
        
        // Find first table that can accommodate the group
        for (Table table : tables) {
            if (table.getTableCapacity() >= groupSize && table.getCustomers().isEmpty()) {
                customerGroup.forEach(table::addCustomer);
                System.out.println("Group of " + groupSize + " seated at table " + table.getTableNumber());
                return table;
            }
        }
        
        System.out.println("Sorry, no available tables for a group of " + groupSize);
        return null;
    }
    
    public List<Table> getAllTables() {
        return new ArrayList<>(tables);
    }
    
    public Table getTable(int tableNumber) {
        return tables.stream()
            .filter(t -> t.getTableNumber() == tableNumber)
            .findFirst()
            .orElse(null);
    }
}
