package com.softwaredesign.project.staff.staffspeeds;

public abstract class SpeedDecorator implements ISpeedComponent {
    protected final ISpeedComponent decoratedSpeed;

    protected SpeedDecorator(ISpeedComponent speed) {
        this.decoratedSpeed = speed;
    }

    @Override
    public double getSpeedMultiplier() {
        return decoratedSpeed.getSpeedMultiplier();
    }

}
