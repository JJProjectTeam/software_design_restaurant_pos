package com.softwaredesign.project.model;

public class BudgetSingleton {
    private static BudgetSingleton instance;
    private double budget;

    private BudgetSingleton() {
        budget = 1000.0; // Default initial budget
    }

    public static synchronized BudgetSingleton getInstance() {
        if (instance == null) {
            instance = new BudgetSingleton();
        }
        return instance;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double newBudget) {
        this.budget = newBudget;
    }

    public void updateBudget(double delta) {
        this.budget += delta;
    }

    // For testing and resetting purposes
    public static void reset() {
        instance = null;
    }
} 