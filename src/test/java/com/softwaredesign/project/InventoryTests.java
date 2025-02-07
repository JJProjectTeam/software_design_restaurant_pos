package com.softwaredesign.project;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.model.inventory.Inventory;
import com.softwaredesign.project.model.inventory.InventoryAlert;
import com.softwaredesign.project.views.InventoryAlertView;
import com.softwaredesign.project.controller.InventoryController;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class InventoryTests {
    private Inventory inventory;
    private InventoryController controller;
    private InventoryAlert alert;
    private InventoryAlertView alertView;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @Before
    public void setUp() {
        inventory = new Inventory();
        controller = new InventoryController(inventory);
        alertView = new InventoryAlertView();
        alert = new InventoryAlert(5, alertView); // Set threshold to 5
        inventory.attach(alert);

        // Capture console output for testing alerts
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    public void testAddIngredient() {
        controller.addIngredient("Tomatoes", 10, 2.50);
        assertEquals(10, inventory.getStock("Tomatoes"));
        assertEquals(2.50, inventory.getPrice("Tomatoes"), 0.001);
    }

    @Test
    public void testUseIngredient() {
        controller.addIngredient("Garlic", 8, 1.00);
        controller.useIngredient("Garlic", 3);
        assertEquals(5, inventory.getStock("Garlic"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUseNonexistentIngredient() {
        controller.useIngredient("Onions", 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUseMoreThanAvailable() {
        controller.addIngredient("Tomatoes", 5, 2.50);
        controller.useIngredient("Tomatoes", 6);
    }

    @Test
    public void testLowStockAlert() {
        controller.addIngredient("Carrots", 6, 1.50);
        controller.useIngredient("Carrots", 2); // Stock becomes 4, should warn
        
        String output = outputStream.toString();
        assertTrue(output.contains("WARNING: Low stock alert for Carrots"));
        assertTrue(output.contains("Only 4 units remaining"));
    }

    @Test
    public void testMultipleIngredients() {
        controller.addIngredient("Tomatoes", 10, 2.50);
        controller.addIngredient("Garlic", 8, 1.00);
        controller.addIngredient("Onions", 15, 1.50);

        assertEquals(10, inventory.getStock("Tomatoes"));
        assertEquals(8, inventory.getStock("Garlic"));
        assertEquals(15, inventory.getStock("Onions"));
    }

    @Test
    public void testNoAlertForSufficientStock() {
        controller.addIngredient("Potatoes", 10, 3.00);
        controller.useIngredient("Potatoes", 2); // Stock becomes 8, no trigger warning
        
        String output = outputStream.toString();
        assertFalse(output.contains("WARNING: Low stock alert for Potatoes"));
    }

    @Test
    public void testConsecutiveStockUpdates() {
        controller.addIngredient("Tomatoes", 10, 2.50);
        controller.useIngredient("Tomatoes", 3); // 7 remaining, no warning
        controller.useIngredient("Tomatoes", 3); // 4 remaining, should warn
        
        String output = outputStream.toString();
        assertTrue(output.contains("WARNING: Low stock alert for Tomatoes"));
        assertTrue(output.contains("Only 4 units remaining"));
    }

    @Test
    public void testZeroStock() {
        controller.addIngredient("Garlic", 5, 1.00);
        controller.useIngredient("Garlic", 5); // Using all stock
        
        String output = outputStream.toString();
        assertTrue(output.contains("WARNING: Low stock alert for Garlic"));
        assertTrue(output.contains("Only 0 units remaining"));
        assertEquals(0, inventory.getStock("Garlic"));
    }
}
