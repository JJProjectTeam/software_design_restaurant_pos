package com.softwaredesign.project.controller;
import com.softwaredesign.project.model.Recipe;
import java.util.List;
import com.softwaredesign.project.model.Order;

public class Waiter {
    private OrderController orderController;


    public Waiter(OrderController orderController) {
        this.orderController = orderController;
    }

    public void placeOrder(List<Recipe> recipes) {
        orderController.placeOrder(recipes);
    }

    public void checkOrderStatus(Order order) {
        orderController.checkOrderStatus(order);
    }
}
