package com.softwaredesign.project.model.staff;

public abstract class StaffMember {
    private double payPerHour;
    private double speedMultiplier;

    public StaffMember(double payPerHour, double speedMultiplier) {
        this.payPerHour = payPerHour;
        this.speedMultiplier = speedMultiplier;
    }

    public double getPayPerHour() {
        return payPerHour;
    }

    public void setPayPerHour(double payPerHour) {
        this.payPerHour = payPerHour;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }
}
