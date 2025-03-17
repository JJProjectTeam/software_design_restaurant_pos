package com.softwaredesign.project.model.staff;


public abstract class StaffMember {
    private double pay;

    public StaffMember(double pay) {
        this.pay = pay;
    }

    public double getPay() {
        return pay;
    }

    public void setPay(double pay) {
        this.pay = pay;
    }


}
