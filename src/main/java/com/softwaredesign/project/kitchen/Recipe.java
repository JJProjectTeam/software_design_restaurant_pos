package com.softwaredesign.project.kitchen;

import com.softwaredesign.project.engine.Entity;
import java.util.LinkedList;
import java.util.Queue;

public class Recipe implements Entity {
    private enum State {
        WAITING,    // Initial state
        PREPPED,    // After prep station
        COOKED     // After cook station
    }

    private State currentState;
    private State nextState;
    private final String name;
    private Station currentStation;
    private Station nextStation;
    private final Queue<Class<? extends Station>> workflow;
    private Class<? extends Station> nextRequiredStation;

    public Recipe(String name) {
        this.name = name;
        this.currentState = State.WAITING;
        this.nextState = State.WAITING;
        this.currentStation = null;
        this.nextStation = null;
        this.workflow = new LinkedList<>();
        this.nextRequiredStation = null;
    }

    public void addWorkflowStep(Class<? extends Station> stationType) {
        workflow.offer(stationType);
        if (nextRequiredStation == null) {
            nextRequiredStation = workflow.peek();
        }
    }

    @Override
    public void readState() {
        if (currentStation != null && nextRequiredStation != null && 
            nextRequiredStation.isInstance(currentStation)) {
            // We're in the right type of station, determine next state
            if (currentState == State.WAITING && nextRequiredStation == PrepStation.class) {
                nextState = State.PREPPED;
                nextStation = null;
            } else if (currentState == State.PREPPED && nextRequiredStation == CookStation.class) {
                nextState = State.COOKED;
                nextStation = null;
            }
        }

        // If we don't have a next station assigned but need one, try to find an available one
        if (nextStation == null && nextRequiredStation != null && currentStation == null) {
            Station availableStation = StationManager.getInstance().findAvailableStation(nextRequiredStation);
            if (availableStation != null) {
                assignToStation(availableStation);
            }
        }
    }

    @Override
    public void writeState() {
        currentState = nextState;
        currentStation = nextStation;
        
        // If we've completed the current station's work, advance to next required station
        if (currentStation == null && !workflow.isEmpty() && 
            (currentState == State.PREPPED || currentState == State.COOKED)) {
            workflow.poll(); // Remove the completed station
            nextRequiredStation = workflow.peek(); // Get next required station (null if none left)
        }
    }

    public void assignToStation(Station station) {
        // Only accept assignment if this is the type of station we need next
        if (nextRequiredStation != null && nextRequiredStation.isInstance(station)) {
            this.nextStation = station;
        }
    }

    public boolean canAssignToStation(Station station) {
        return nextRequiredStation != null && nextRequiredStation.isInstance(station);
    }

    public boolean isComplete() {
        return currentState == State.COOKED && workflow.isEmpty();
    }

    public boolean isPrepped() {
        return currentState == State.PREPPED;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Station> getNextRequiredStation() {
        return nextRequiredStation;
    }
}
