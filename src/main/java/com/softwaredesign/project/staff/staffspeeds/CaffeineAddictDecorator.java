package com.softwaredesign.project.staff.staffspeeds;

public class CaffeineAddictDecorator extends SpeedDecorator {
    public CaffeineAddictDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        return super.getSpeedMultiplier() * 1.5;
    }
}
