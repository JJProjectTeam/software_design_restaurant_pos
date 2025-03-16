package com.softwaredesign.project.staff;

import com.softwaredesign.project.staff.staffspeeds.ISpeedComponent;

public abstract class StaffMember {
    private double payPerHour;
    private ISpeedComponent speedDecorator;

    public StaffMember(double payPerHour, ISpeedComponent speedDecorator) {
        this.payPerHour = payPerHour;
        this.speedDecorator = speedDecorator;
    }

    public double getPayPerHour() {
        return payPerHour;
    }

    public void setPayPerHour(double payPerHour) {
        this.payPerHour = payPerHour;
    }

    public double getSpeedMultiplier() {
        return speedDecorator.getSpeedMultiplier();
    }
}
