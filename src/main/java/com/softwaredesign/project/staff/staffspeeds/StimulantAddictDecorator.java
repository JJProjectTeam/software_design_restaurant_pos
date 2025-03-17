package com.softwaredesign.project.staff.staffspeeds;

public class StimulantAddictDecorator extends SpeedDecorator {
    public StimulantAddictDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        // Use a more reasonable multiplier to prevent instant task completion
        // Original was 2.0 which could cause tasks to complete too quickly
        double baseMultiplier = decoratedSpeed.getSpeedMultiplier();
        double stimulantBoost = 1.5; // Reduced from 2.0
        
        // Cap the final multiplier to prevent issues with very fast completion
        double finalMultiplier = baseMultiplier * stimulantBoost;
        return Math.min(finalMultiplier, 2.0); // Cap at 2.0
    }
}
