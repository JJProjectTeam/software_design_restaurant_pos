package com.softwaredesign.project.staff.staffspeeds;

public class LethargicDecorator extends SpeedDecorator {
    public LethargicDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        return decoratedSpeed.getSpeedMultiplier() * getSpeedModifier();
    }

    @Override
    protected double getSpeedModifier() {
        return 0.7; // 30% speed reduction
    }
}
