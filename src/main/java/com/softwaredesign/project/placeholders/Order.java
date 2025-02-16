package com.softwaredesign.project.placeholders;

import java.time.LocalDateTime;

public class Order {
    // PLACEHOLDER TO AVOID ERRORS
    private LocalDateTime orderTime;

    public Order(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void addRecipe(Recipe recipe) {
        // PLACEHOLDER TO AVOID ERRORS
    }
    public void addModification(Recipe recipe, Ingredient ingredient, boolean isAddition) {
        // PLACEHOLDER TO AVOID ERRORS
    }
}
