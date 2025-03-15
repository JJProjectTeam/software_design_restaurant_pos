package com.softwaredesign.project.staff.staffspeeds;

public class CaffeineAddictDecorator extends SpeedDecorator {
    public CaffeineAddictDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        return decoratedSpeed.getSpeedMultiplier() * getSpeedModifier();
    }

    @Override
    protected double getSpeedModifier() {
        return 1.5; // 50% speed boost
    }
}
