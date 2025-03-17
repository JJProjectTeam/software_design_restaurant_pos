package com.softwaredesign.project.staff.staffspeeds;


public class BaseSpeed implements ISpeedComponent {
    double speed;
    @Override
    public double getSpeedMultiplier() {
        return speed;
    }

    public BaseSpeed(double speed) {
        // Ensure speed is not too high to avoid instant task completion
        this.speed = Math.min(speed, 2.0);
    }
    
    public BaseSpeed() {
        // Default speed of 1.0 means tasks take their full work required
        this.speed = 1.0;
    }
}
