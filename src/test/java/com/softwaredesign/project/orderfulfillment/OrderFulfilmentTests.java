package com.softwaredesign.project.orderfulfillment;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class OrderFulfilmentTests {
    private SeatingPlan seatingPlan;
    private Menu menu;

    @Before
    public void setUp() {
        InventoryService inventoryService = new Inventory();
        menu = new Menu(inventoryService);
        seatingPlan = new SeatingPlan(5, 15, menu);
    }

    @Test
    public void testValidSeatingPlanCreation() {
        assertNotNull(seatingPlan.getAllTables());
        assertEquals(5, seatingPlan.getAllTables().size());
    }

    @Test
    public void testFindTableForValidGroup() {
        List<DineInCustomer> group = new ArrayList<>();
        group.add(new DineInCustomer());
        group.add(new DineInCustomer());
        
        Table assignedTable = seatingPlan.findTableForGroup(group);
        assertNotNull(assignedTable);
        assertEquals(2, assignedTable.getCustomers().size());
    }

    @Test
    public void testRejectOversizedGroup() {
        List<DineInCustomer> largeGroup = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            largeGroup.add(new DineInCustomer());
        }
        
        Table assignedTable = seatingPlan.findTableForGroup(largeGroup);
        assertNull(assignedTable);
    }
}
