package com.softwaredesign.project.staff.staffspeeds;

import com.fasterxml.jackson.databind.JsonSerializable.Base;

public class BaseSpeed implements ISpeedComponent {
    double speed;
    @Override
    public double getSpeedMultiplier() {
        return speed;
    }

    public BaseSpeed(double speed) {
        this.speed = speed;
    }
    public BaseSpeed() {
        this.speed = 1.0;
    }
}
