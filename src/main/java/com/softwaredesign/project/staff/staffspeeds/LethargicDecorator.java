package com.softwaredesign.project.staff.staffspeeds;

public class LethargicDecorator extends SpeedDecorator {
    public LethargicDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        return super.getSpeedMultiplier() * 0.5;
    }
}
