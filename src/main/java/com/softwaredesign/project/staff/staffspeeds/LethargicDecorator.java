package com.softwaredesign.project.staff.staffspeeds;

public class LethargicDecorator extends SpeedDecorator {
    public LethargicDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        return decoratedSpeed.getSpeedMultiplier() * 0.7;
    }

}
