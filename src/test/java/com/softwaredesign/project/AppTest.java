package com.softwaredesign.project;

import static org.junit.Assert.*;
import org.junit.Test;

import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.staff.Waiter;

public class AppTest {
    @Test
    public void testSystemInitialization() {
        Menu menu = new Menu();
        OrderManager orderManager = new OrderManager();
        SeatingPlan seatingPlan = new SeatingPlan(5, 15, menu);
        Waiter waiter = new Waiter(15.0, 1.0, orderManager, menu);

        assertNotNull(menu);
        assertNotNull(orderManager);
        assertNotNull(seatingPlan);
        assertNotNull(waiter);
        
        assertTrue(seatingPlan.getAllTables().size() > 0);
    }

    @Test
    public void testValidSeatingPlanConfiguration() {
        Menu menu = new Menu();
        SeatingPlan seatingPlan = new SeatingPlan(5, 15, menu);
        
        for (var table : seatingPlan.getAllTables()) {
            assertTrue(table.getTableCapacity() >= 1);
            assertTrue(table.getTableCapacity() <= 4);
        }
    }
}
