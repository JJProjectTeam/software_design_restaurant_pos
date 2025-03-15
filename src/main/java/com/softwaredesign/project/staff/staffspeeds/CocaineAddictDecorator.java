package com.softwaredesign.project.staff.staffspeeds;

public class CocaineAddictDecorator extends SpeedDecorator {
    public CocaineAddictDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        return decoratedSpeed.getSpeedMultiplier() * getSpeedModifier();
    }

    @Override
    protected double getSpeedModifier() {
        return 2.0; // 100% speed boost
    }
}
