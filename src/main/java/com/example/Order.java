package com.example;

public class Order {

    private User madeBy;
    private double total;
    private Location location;

    public Order(User madeBy, double total, Location location) {
        this.madeBy = madeBy;
        this.total = total;
        this.location = location;
    }

    public User getMadeBy() {
        return madeBy;
    }

    public void setMadeBy(User madeBy) {
        this.madeBy = madeBy;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
