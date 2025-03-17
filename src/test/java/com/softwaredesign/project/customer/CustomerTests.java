package com.softwaredesign.project.customer;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.model.customer.DineInCustomer;
import com.softwaredesign.project.model.inventory.Inventory;
import com.softwaredesign.project.model.inventory.InventoryService;
import com.softwaredesign.project.model.kitchen.StationType;
import com.softwaredesign.project.model.menu.Menu;
import com.softwaredesign.project.model.order.Recipe;
public class CustomerTests {
    private DineInCustomer customer;
    private Menu menu;
    @Before
    public void setUp() {
        customer = new DineInCustomer();
        InventoryService inventoryService = new Inventory();
        inventoryService.addIngredient("Beef Patty", 10, 1.0, StationType.GRILL);
        inventoryService.addIngredient("Bun", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Lettuce", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Tomato", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Cheese", 10, 1.0, StationType.PREP);
        inventoryService.addIngredient("Mustard", 10, 0.5, StationType.PREP);
        inventoryService.addIngredient("Ketchup", 10, 0.5, StationType.PREP);
        
        menu = new Menu(inventoryService);
    }

    @Test
    public void testCustomerStartsBrowsing() {
        assertFalse(customer.isDoneBrowsing());
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotOrderWhileBrowsing() {
        customer.selectRecipeFromMenu(menu);
    }

    @Test
    public void testFinishBrowsing() {
        customer.finishBrowsing();
        assertTrue(customer.isDoneBrowsing());
    }

    @Test
    public void testCanSelectRecipeAfterBrowsing() {
        customer.finishBrowsing();
        Recipe recipe = customer.selectRecipeFromMenu(menu);
        assertNotNull(recipe);
    }
}
