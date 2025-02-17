package com.softwaredesign.project.staff;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.staff.*;
import com.softwaredesign.project.staff.chefstrategies.*;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.order.Station;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Inventory;

public class StaffTests {
    private Waiter waiter;
    private Chef chef;
    private Menu menu;
    private OrderManager orderManager;

    @Before
    public void setUp() {
        InventoryService inventoryService = new Inventory();
        menu = new Menu(inventoryService);
        orderManager = new OrderManager();
        waiter = new Waiter(15.0, 1.0, orderManager, menu);
        chef = new Chef(20.0, 1.5, new ShortestQueueFirst());
    }

    @Test
    public void testWaiterTableAssignment() {
        Table table = new Table(1, menu, 4);
        waiter.assignTable(table);
        assertTrue(waiter.getAssignedTables().contains(table));
    }

    @Test
    public void testChefStrategyChange() {
        ChefStrategy newStrategy = new LongestQueueFirstStrategy();
        chef.setWorkStrategy(newStrategy);
        
        Station station1 = new Station();
        Station station2 = new Station();
        chef.getAssignedStations().add(station1);
        chef.getAssignedStations().add(station2);
        
        assertNotNull(chef.chooseNextStation());
    }

    @Test
    public void testStaffPayRate() {
        assertEquals(15.0, waiter.getPayPerHour(), 0.01);
        assertEquals(20.0, chef.getPayPerHour(), 0.01);
    }
}