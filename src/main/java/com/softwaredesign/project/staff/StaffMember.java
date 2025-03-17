package com.softwaredesign.project.staff;


public abstract class StaffMember {
    private double payPerHour;

    public StaffMember(double payPerHour) {
        this.payPerHour = payPerHour;
    }

    public double getPayPerHour() {
        return payPerHour;
    }

    public void setPayPerHour(double payPerHour) {
        this.payPerHour = payPerHour;
    }


}
