package com.softwaredesign.project.staff.staffspeeds;

public class StimulantAddictDecorator extends SpeedDecorator {
    public StimulantAddictDecorator(ISpeedComponent speed) {
        super(speed);
    }

    @Override
    public double getSpeedMultiplier() {
        return decoratedSpeed.getSpeedMultiplier() * 2.0;
    }
}
