package com.softwaredesign.project.model;

public class BankBalanceSingleton {
    private static BankBalanceSingleton instance;
    private double bankBalance;

    private BankBalanceSingleton() {
    }

    public static synchronized BankBalanceSingleton getInstance() {
        if (instance == null) {
            instance = new BankBalanceSingleton();
        }
        return instance;
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(double newBankBalance) {
        this.bankBalance = newBankBalance;
    }

    public void updateBankBalance(double delta) {
        this.bankBalance += delta;
    }

    // For testing and resetting purposes
    public static void reset() {
        instance = null;
    }
} 