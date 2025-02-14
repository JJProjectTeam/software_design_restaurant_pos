package com.softwaredesign.project;

import com.softwaredesign.project.model.customer.DineInCustomer;
import com.softwaredesign.project.model.orderfulfillment.Table;
import com.softwaredesign.project.model.placeholders.ConcreteRecipe;
import com.softwaredesign.project.model.placeholders.Recipe;
import com.softwaredesign.project.model.placeholders.OrderManager;
import com.softwaredesign.project.model.staff.Waiter;


public class App 
{
    public static void main( String[] args){

        Table table = new Table(1);
        DineInCustomer customer1 = new DineInCustomer();
        DineInCustomer customer2 = new DineInCustomer();
        // Define someRecipe
        Recipe someRecipe = new ConcreteRecipe();

        table.addCustomer(customer1);
        table.addCustomer(customer2);

        // Assuming OrderManager is a class that needs to be imported and instantiated
        OrderManager orderManager = new OrderManager();
        Waiter waiter = new Waiter(15.0, 1.0, orderManager);
        waiter.assignTable(table);

        // Customers browse and select
        customer1.selectRecipe(someRecipe);
        customer1.finishBrowsing();
        customer2.selectRecipe(someRecipe);
        customer2.finishBrowsing();

        // Waiter takes order when everyone is ready
        waiter.takeTableOrder(table);    }

}
