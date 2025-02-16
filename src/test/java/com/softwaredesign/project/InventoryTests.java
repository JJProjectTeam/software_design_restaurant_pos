package com.softwaredesign.project;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryAlert;
import com.softwaredesign.project.inventory.IngredientStore;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

public class InventoryTests {
    private Inventory inventory;
    private InventoryAlert alert;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @Before
    public void setUp() {
        resetSingleton(); // Reset the singleton instance before each test
        inventory = Inventory.getInstance();
        alert = new InventoryAlert(5); // Set threshold to 5
        inventory.attach(alert);

        // Capture console output for testing alerts
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        resetSingleton(); // Clean up after each test
    }

    // Helper method to reset singleton instance between tests
    private void resetSingleton() {
        try {
            Field instance = Inventory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    @Test
    public void testSingletonPattern() {
        Inventory instance1 = Inventory.getInstance();
        Inventory instance2 = Inventory.getInstance();
        assertSame("Singleton instances should be the same", instance1, instance2);
    }

    @Test
    public void testAddIngredientBasic() {
        Inventory.getInstance().addIngredient("Beef", 20, 5.00);
        IngredientStore store = Inventory.getInstance().getIngredientStore("Beef");
        assertNotNull("IngredientStore should not be null", store);
        assertEquals("Stock should be 20", 20, store.getQuantity());
        assertEquals("Price should be 5.00", 5.00, store.getPrice(), 0.001);
    }

    @Test
    public void testDetachObserver() {
        // Remove the default alert first
        inventory.detach(alert);
        
        // Create a separate alert just for this test
        InventoryAlert testAlert = new InventoryAlert(5);
        inventory.attach(testAlert);
        
        // Add an ingredient to verify the alert is working
        outputStream.reset();
        inventory.addIngredient("TestItem1", 3, 1.00);
        String output1 = outputStream.toString();
        assertTrue("Observer should receive updates before detachment",
                  output1.contains("WARNING: Low stock alert for TestItem1"));
        
        // Detach the observer and verify it no longer receives updates
        inventory.detach(testAlert);
        outputStream.reset();
        inventory.addIngredient("TestItem2", 3, 1.00);
        String output2 = outputStream.toString();
        assertFalse("Detached observer should not receive updates for TestItem2",
                   output2.contains("WARNING: Low stock alert for TestItem2"));
    }

    @Test
    public void testUpdateInventory() {
        Inventory inventory = Inventory.getInstance();
        inventory.addIngredient("Cheese", 10, 3.00);
        inventory.addIngredient("Lettuce", 15, 1.50);
        
        Map<String, Integer> updates = new HashMap<>();
        updates.put("Cheese", 5);
        updates.put("Lettuce", 8);
        
        inventory.updateInventory(updates);
        
        assertEquals("Cheese stock should be updated", 5, inventory.getStock("Cheese"));
        assertEquals("Lettuce stock should be updated", 8, inventory.getStock("Lettuce"));
    }

    @Test
    public void testNonExistentIngredients() {
        Inventory inventory = Inventory.getInstance();
        assertEquals("Non-existent ingredient should return 0 stock", 
                    0, inventory.getStock("NonExistent"));
        assertEquals("Non-existent ingredient should return 0.0 price", 
                    0.0, inventory.getPrice("NonExistent"), 0.001);
        assertNull("Non-existent ingredient store should be null", 
                  inventory.getIngredientStore("NonExistent"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateInventoryWithNonExistentIngredient() {
        Inventory inventory = Inventory.getInstance();
        Map<String, Integer> updates = new HashMap<>();
        updates.put("NonExistent", 5);
        inventory.updateInventory(updates);
    }
}
