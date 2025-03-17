package com.softwaredesign.project.model.staff.staffspeeds;

public class CaffeineAddictDecorator extends SpeedDecorator {
    public CaffeineAddictDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        return decoratedSpeed.getSpeedMultiplier() * 1.5;
    }
}
