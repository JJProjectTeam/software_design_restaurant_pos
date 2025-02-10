package com.softwaredesign.project;

import com.softwaredesign.project.model.PizzaBuilder;
import com.softwaredesign.project.model.Ingredient;
import com.softwaredesign.project.model.Meal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.softwaredesign.project.model.Recipe;
import com.softwaredesign.project.model.BurgerBuilder;
import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.controller.Kitchen; 
import com.softwaredesign.project.controller.OrderManager;

/**


 * Hello world!
 *
 * 
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        testBuilderPattern();
    }

    public static int add(int a, int b) {
        return a + b;
    }

    // test for the Builder pattern
    public static void testBuilderPattern() {
        // Option 2: Creating list and adding ingredients
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient("Cheese", 3.0));
        ingredients.add(new Ingredient("Pepperoni", 1.0));
        Recipe pizzaBuilder = new PizzaBuilder(ingredients);

        pizzaBuilder.addIngredient(new Ingredient("Mushroom", 2.0));

        ArrayList<Ingredient> burgerIngredients = new ArrayList<>();
        burgerIngredients.add(new Ingredient("Beef", 5.0));
        burgerIngredients.add(new Ingredient("Cheese", 3.0));
        burgerIngredients.add(new Ingredient("Lettuce", 1.0));
        Recipe burgerBuilder = new BurgerBuilder(burgerIngredients);

        burgerBuilder.addIngredient(new Ingredient("Tomato", 1.0));
        burgerBuilder.removeIngredient(new Ingredient("Cheese", 3.0));

        // create a new order
        Order order = new Order(Arrays.asList(pizzaBuilder, burgerBuilder));

        // create a new order manager
        OrderManager orderManager = new OrderManager();
        orderManager.addOrder(order);

        // create a Kitchen object which acts like the director 
        Kitchen kitchen = new Kitchen(orderManager);

        // get the recipes from the order manager
        kitchen.getRecipes();
        // prepare the recipes
        List<Meal> mealsCreated = kitchen.prepareRecipes();
        Meal pizza = mealsCreated.get(0);
        Meal burger = mealsCreated.get(1);




        System.out.println(pizza);
        System.out.println(burger);
    }


}
