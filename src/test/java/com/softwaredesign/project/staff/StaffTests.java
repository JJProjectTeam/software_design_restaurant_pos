package com.softwaredesign.project.staff;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.staff.chefstrategies.*;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.order.Station;
import com.softwaredesign.project.order.StationType;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.order.CollectionPoint;

public class StaffTests {
    private Waiter waiter;
    private Chef chef;
    private Menu menu;
    private OrderManager orderManager;

    @Before
    public void setUp() {
        InventoryService inventoryService = new Inventory();
        menu = new Menu(inventoryService);
        CollectionPoint collectionPoint = new CollectionPoint();
        orderManager = new OrderManager(collectionPoint);
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
    //TODO looking at this now, this test (MINE) is kinda shit, will add more
    public void testChefStrategyChange() {
        ChefStrategy newStrategy = new LongestQueueFirstStrategy();
        chef.setWorkStrategy(newStrategy);
        
        chef.assignToStation(StationType.GRILL);
        chef.assignToStation(StationType.PREP);
        
        Station nextStation = chef.chooseNextStation();
        assertNotNull("Chef should choose a station", nextStation);
        assertTrue("Station should be either PREP or GRILL", 
            nextStation.getType() == StationType.PREP || 
            nextStation.getType() == StationType.GRILL);
    }

    @Test
    public void testStaffPayRate() {
        assertEquals(15.0, waiter.getPayPerHour(), 0.01);
        assertEquals(20.0, chef.getPayPerHour(), 0.01);
    }
}