package com.softwaredesign.project;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryAlert;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class InventoryTests {
    private Inventory inventory;
    private InventoryAlert alert;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @Before
    public void setUp() {
        inventory = new Inventory();
        alert = new InventoryAlert(5); // Set threshold to 5
        inventory.attach(alert);

        // Capture console output for testing alerts
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    public void testAddIngredient() {
        inventory.addIngredient("Tomatoes", 10, 2.50);
        assertEquals(10, inventory.getStock("Tomatoes"));
        assertEquals(2.50, inventory.getPrice("Tomatoes"), 0.001);
        assertTrue(outputStream.toString().contains("Stock update: Tomatoes - 10 units in stock"));
    }

    @Test
    public void testUseIngredient() {
        inventory.addIngredient("Garlic", 8, 1.00);
        inventory.useIngredient("Garlic", 3);
        assertEquals(5, inventory.getStock("Garlic"));
        assertTrue(outputStream.toString().contains("Stock update: Garlic - 5 units in stock"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUseNonexistentIngredient() {
        inventory.useIngredient("Onions", 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUseMoreThanAvailable() {
        inventory.addIngredient("Tomatoes", 5, 2.50);
        inventory.useIngredient("Tomatoes", 6);
    }

    @Test
    public void testLowStockAlert() {
        inventory.addIngredient("Carrots", 6, 1.50);
        outputStream.reset(); // Clear previous output
        inventory.useIngredient("Carrots", 2); // Stock becomes 4, should warn
        
        String output = outputStream.toString();
        assertTrue(output.contains("WARNING: Low stock alert for Carrots"));
        assertTrue(output.contains("only 4 remaining"));
    }

    @Test
    public void testMultipleIngredients() {
        inventory.addIngredient("Tomatoes", 10, 2.50);
        inventory.addIngredient("Garlic", 8, 1.00);
        inventory.addIngredient("Onions", 15, 1.50);

        assertEquals(10, inventory.getStock("Tomatoes"));
        assertEquals(8, inventory.getStock("Garlic"));
        assertEquals(15, inventory.getStock("Onions"));
    }

    @Test
    public void testNoAlertForSufficientStock() {
        inventory.addIngredient("Potatoes", 10, 3.00);
        outputStream.reset(); // Clear previous output
        inventory.useIngredient("Potatoes", 2); // Stock becomes 8, no warning
        
        String output = outputStream.toString();
        assertFalse(output.contains("WARNING: Low stock alert for Potatoes"));
    }

    @Test
    public void testConsecutiveStockUpdates() {
        inventory.addIngredient("Tomatoes", 10, 2.50);
        outputStream.reset(); // Clear previous output
        inventory.useIngredient("Tomatoes", 3); // 7 remaining, no warning
        inventory.useIngredient("Tomatoes", 3); // 4 remaining, should warn
        
        String output = outputStream.toString();
        assertTrue(output.contains("WARNING: Low stock alert for Tomatoes"));
        assertTrue(output.contains("only 4 remaining"));
    }

    @Test
    public void testZeroStock() {
        inventory.addIngredient("Garlic", 5, 1.00);
        inventory.useIngredient("Garlic", 5); // Using all stock
        
        String output = outputStream.toString();
        assertTrue(output.contains("WARNING: Low stock alert for Garlic"));
        assertTrue(output.contains("only 0 remaining"));
        assertEquals(0, inventory.getStock("Garlic"));
    }
}
