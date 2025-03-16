package com.softwaredesign.project.model;

import java.util.HashMap;
import java.util.Map;

public class StatisticsSingleton {
    private static StatisticsSingleton instance;
    private Map<String, Integer> intStats;
    private Map<String, Double> doubleStats;
    private Map<String, String> stringStats;

    private StatisticsSingleton() {
        intStats = new HashMap<>();
        doubleStats = new HashMap<>();
        stringStats = new HashMap<>();
    }

    public static synchronized StatisticsSingleton getInstance() {
        if (instance == null) {
            instance = new StatisticsSingleton();
        }
        return instance;
    }

    // Generic statistics methods
    public void setStat(String key, Object value) {
        if (value instanceof Integer) {
            intStats.put(key, (Integer) value);
        } else if (value instanceof Double) {
            doubleStats.put(key, (Double) value);
        } else if (value instanceof String) {
            stringStats.put(key, (String) value);
        } else if (value instanceof Float) {
            doubleStats.put(key, ((Float) value).doubleValue());
        } else if (value instanceof Long) {
            intStats.put(key, ((Long) value).intValue());
        } else {
            stringStats.put(key, value.toString());
        }
    }

    public void incrementStat(String key) {
        incrementStat(key, 1);
    }

    public void incrementStat(String key, Number amount) {
        if (doubleStats.containsKey(key)) {
            doubleStats.put(key, doubleStats.get(key) + amount.doubleValue());
        } else if (intStats.containsKey(key)) {
            intStats.put(key, intStats.get(key) + amount.intValue());
        } else {
            // Default to int for new keys with whole numbers
            if (amount.doubleValue() == amount.intValue()) {
                intStats.put(key, amount.intValue());
            } else {
                // Use double for decimal values
                doubleStats.put(key, amount.doubleValue());
            }
        }
    }

    // Generic get method with type parameter
    @SuppressWarnings("unchecked")
    public <T> T getStat(String key, Class<T> type) {
        if (type == Integer.class && intStats.containsKey(key)) {
            return (T) Integer.valueOf(intStats.get(key));
        } else if ((type == Double.class || type == Number.class) && doubleStats.containsKey(key)) {
            return (T) Double.valueOf(doubleStats.get(key));
        } else if (type == String.class && stringStats.containsKey(key)) {
            return (T) stringStats.get(key);
        } else if (type == Integer.class && doubleStats.containsKey(key)) {
            // Convert double to int if requested
            return (T) Integer.valueOf(doubleStats.get(key).intValue());
        } else if (type == Double.class && intStats.containsKey(key)) {
            // Convert int to double if requested
            return (T) Double.valueOf(intStats.get(key).doubleValue());
        } else if (type == String.class) {
            // Try to convert from int or double to string
            if (intStats.containsKey(key)) {
                return (T) intStats.get(key).toString();
            } else if (doubleStats.containsKey(key)) {
                return (T) String.format("%.2f", doubleStats.get(key));
            }
        }

        // Return default values based on requested type
        if (type == Integer.class) {
            return (T) Integer.valueOf(0);
        } else if (type == Double.class) {
            return (T) Double.valueOf(0.0);
        } else if (type == String.class) {
            return (T) "";
        }

        return null;
    }

    // Convenience methods for common types
    public int getInt(String key) {
        return getStat(key, Integer.class);
    }

    public double getDouble(String key) {
        return getStat(key, Double.class);
    }

    public String getString(String key) {
        return getStat(key, String.class);
    }

    public boolean containsStat(String key) {
        return intStats.containsKey(key) || doubleStats.containsKey(key) || stringStats.containsKey(key);
    }

    // Get all statistics as formatted strings
    public Map<String, String> getAllStatsFormatted() {
        Map<String, String> allStats = new HashMap<>();

        for (Map.Entry<String, Integer> entry : intStats.entrySet()) {
            allStats.put(entry.getKey(), entry.getValue().toString());
        }

        for (Map.Entry<String, Double> entry : doubleStats.entrySet()) {
            allStats.put(entry.getKey(), String.format("%.2f", entry.getValue()));
        }

        for (Map.Entry<String, String> entry : stringStats.entrySet()) {
            allStats.put(entry.getKey(), entry.getValue());
        }

        return allStats;
    }

    // Reset all statistics
    public void resetAllStats() {
        intStats.clear();
        doubleStats.clear();
        stringStats.clear();
    }

    // For testing and resetting purposes
    public static void reset() {
        instance = null;
    }
}