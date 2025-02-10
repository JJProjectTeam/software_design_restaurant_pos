package com.softwaredesign.project.controller;
import com.softwaredesign.project.model.Recipe;
import java.util.List;
import com.softwaredesign.project.model.Order;

public class OrderController {
    private Kitchen kitchen;
    private OrderManager orderManager;


    public OrderController(Kitchen kitchen, OrderManager orderManager) {
        this.kitchen = kitchen;
        this.orderManager = orderManager;
    }


    // public void placeOrder(List<Recipe> recipes) {
    //     // Check if the order is possible to make 
    //     if (orderManager.checkOrderIngredients(recipes)) {
    //         // If possible, place the order 
            

    //         kitchen.placeOrder(recipes);
    //     } else {
    //         // If not possible, return an error
    //         System.out.println("Error: Order ingredients not available");
    //     }
    // }


    // public void checkOrderStatus(Order order) {
    //     orderManager.getOrderStatus(order);
    // }  

    
    
    

}
