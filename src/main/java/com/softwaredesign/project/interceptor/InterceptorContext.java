package com.softwaredesign.project.interceptor;

import java.util.Map;

/**
 * Context object that carries information about the inventory state
 */
public class InterceptorContext {
    private final Map<String, Integer> stockLevels;
    private boolean isGameOver;

    public InterceptorContext(Map<String, Integer> stockLevels) {
        this.stockLevels = stockLevels;
        this.isGameOver = false;
    }

    public Map<String, Integer> getStockLevels() {
        return stockLevels;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public boolean isGameOver() {
        return isGameOver;
    }
}
