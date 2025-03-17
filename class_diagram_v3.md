classDiagram

namespace order {
    class Order {
        <<entity>>
        + List<Recipe> recipes
        + addRecipe(recipe: Recipe)
        + isComplete(): boolean
        --
        pre addRecipe: recipe != null
        post addRecipe: recipes.contains(recipe)
    }
    class OrderManager {
        <<control>>
        + List<Order> orders
        + manageOrders()
        + StationMapping getStationMapping(Recipe recipe)
        --
        pre getStationMapping: recipe != null
        post getStationMapping: return != null
    }
    class Recipe {
        <<entity>>
        <<Abstract>> %% Factory Pattern (Abstract Creator for Concrete Recipes)
        + List<Ingredient> ingredients
        + Meal build(StationMapping mapping)
        + addIngredient(Ingredient ingredient)
        + removeIngredient(Ingredient ingredient)
        --
        pre build: mapping != null
        post build: return instanceof Meal
    }
    class BurgerRecipe {
        <<entity>>
    }
    class PizzaRecipe {
        <<entity>>
    }
    class KebabRecipe {
        <<entity>>
    }
    class Meal {
        <<entity>>
        + name: String
        + List<Ingredient> ingredients
        + List<Station> remainingStations
        + mealState: String
        + updateState() %% State Pattern (Meal updates its state)
        --
        post updateState: mealState is updated
    }
    class StationMapping {
        <<entity>>
        + Map<Recipe, List<Station>> mapping
        + List<Station> getStationsForRecipe(Recipe recipe)
        --
        pre getStationsForRecipe: recipe != null
        post getStationsForRecipe: return list of stations or empty list
    }
}

namespace staff {
    class Staff {
        <<entity>>
        <<Abstract>>
        + pay: double
        + speedMultiplier: double
    }
    class Waiter {
        <<entity>>
        + takeOrder(Order order)
        + assignTable(InPersonCustomer customer, Table table)
        + deliverMeal(Meal meal, Table table)
        --
        pre takeOrder: order != null
        pre assignTable: customer != null && table != null
        pre deliverMeal: meal != null && table != null
    }
    class Chef {
        <<entity>>
        + List<Station> assignedStations
        + IChefStrategy strategy %% Strategy Pattern (Chef uses a strategy to prioritize work)
        + workThroughBacklog()
        --
        pre workThroughBacklog: strategy != null
    }
}

namespace foodprep {
    class Kitchen {
        <<entity>>
        + List<Station> stations
        + prepareMeal(Recipe recipe)
        --
        pre prepareMeal: recipe != null
    }

    class Station {
        <<entity>>
        <<Abstract>>
        + List<Meal> backlog
        + processMeal(Meal meal)
        --
        pre processMeal: meal != null && !backlog.isEmpty()
        post processMeal: meal.remainingStations.size() decreased
    }

    class PrepStation {
        <<entity>>
    }
    class GrillStation {
        <<entity>>
    }
    class PresentationStation {
        <<entity>>
    }
}

namespace inventory {
    class Ingredient {
        <<entity>>
        + name: String
        + cost: double
    }
    class Inventory {
        <<entity>>
        + HashMap<Ingredient, int> ingredients
        + deplete()
        + registerObserver(IMenuObserver observer) %% Observer Pattern (Inventory notifies Menu)
        + notifyObservers()
        --
        pre registerObserver: observer != null
        post deplete: ingredient quantities reduced
    }
}

namespace orderfulfillment {
    class IMenuObserver {
        <<interface>> %% Observer Pattern (Menu observes Inventory)
        + update()
    }
    
    class Menu {
        <<entity>>
        + List<Recipe> availableRecipes
        + displayAvailableRecipes()
        --
        post displayAvailableRecipes: all available recipes shown
    }

    class SeatingPlan {
        <<entity>>
        + List<Table> tables
    }

    class Table {
        <<entity>>
        + tableNumber: int
        + placeOrder()
    }
}

namespace customer {    
    class Customer {
        <<entity>>
        <<Abstract>>
        + List<Customer> friends
        + placeOrder(Recipe recipe)
        --
        pre placeOrder: recipe != null
    }
    class TakeAwayCustomer {
        <<entity>>
        + placeOrderOnline(Recipe recipe)
        --
        pre placeOrderOnline: recipe != null
    }
    class InPersonCustomer {
        <<entity>>
        + giveOrderToWaiter(Waiter waiter, Recipe recipe)
        + requestTable(Waiter waiter)
        --
        pre giveOrderToWaiter: waiter != null && recipe != null
        pre requestTable: waiter != null
    }
}

namespace strategy {
    class IChefStrategy {
        <<interface>> %% Strategy Pattern (Different chef behaviors)
        + chooseMealToPrepare(List<Station> stations)
        --
        pre chooseMealToPrepare: stations != null
        post chooseMealToPrepare: return meal or null
    }
    class EarliestOrderStrategy {
        <<control>>
    }
    class BusiestStationStrategy {
        <<control>>
    }
}

namespace view {
    class RestaurantView {
        <<dialog>>
        + displayKitchen(Kitchen kitchen)
        + displayOrders(List<Order> orders)
        + displayInventory(Inventory inventory)
        --
        pre displayKitchen: kitchen != null
        pre displayOrders: orders != null
        pre displayInventory: inventory != null
    }
    
    class MenuView {
        <<dialog>>
        + showAvailableRecipes(Menu menu)
        --
        pre showAvailableRecipes: menu != null
    }
    
    class CustomerView {
        <<dialog>>
        + displaySeatingPlan(SeatingPlan plan)
        --
        pre displaySeatingPlan: plan != null
    }
}

namespace controller {
    class OrderController {
        <<control>>
        + processOrder(Order order)
        + cancelOrder(Order order)
        --
        pre processOrder: order != null
        pre cancelOrder: order != null && order exists
        post cancelOrder: order removed from system
    }
    
    class KitchenController {
        <<control>>
        + assignStations(List<Recipe> recipes)
        + monitorProgress()
        --
        pre assignStations: recipes != null
    }
    
    class InventoryController {
        <<control>>
        + updateInventory(Ingredient ingredient, int quantity)
        + checkAvailability(Ingredient ingredient, int required)
        --
        pre updateInventory: ingredient != null && quantity > 0
        pre checkAvailability: ingredient != null && required > 0
        post checkAvailability: returns true if available, false otherwise
    }
}

%% Relationships with Multiplicity

Staff <|-- Waiter
Staff <|-- Chef

Customer <|-- TakeAwayCustomer
Customer <|-- InPersonCustomer

Inventory "1" --> "0..*" Ingredient : contains

SeatingPlan "1" --> "1..*" Table : "has many"
Waiter "1" --> "0..*" Table : "assigns customers to"

Order "1" --> "1..*" Recipe : "contains"
OrderManager "1" --> "0..*" Order : "manages"

Recipe <|-- BurgerRecipe
Recipe <|-- PizzaRecipe
Recipe <|-- KebabRecipe

Recipe "1" --> "1..*" Ingredient : "contains"
Recipe "1" --> "1" Meal : "builds"

OrderManager "1" --> "1" StationMapping : "uses to assign stations"
StationMapping "1" --> "1..*" Station : "maps recipes to"

Meal "1" --> "1..*" Station : "visits"
Meal "0..*" --> "0..1" Table : "delivered to"

Waiter "1" --> "0..*" Meal : "delivers to table"

Kitchen "1" --> "1" OrderManager : "gets orders from"
Kitchen "1" --> "0..*" Recipe : "builds meals from"
Kitchen "1" --> "1..*" Station : "contains"

%% Meal Processing
Station <|-- PrepStation
Station <|-- GrillStation
Station <|-- PresentationStation
Station "1" --> "0..*" Meal : "processes meals"

%% Chef Strategy
Chef "1" --> "1" IChefStrategy : "uses"
IChefStrategy <|.. EarliestOrderStrategy
IChefStrategy <|.. BusiestStationStrategy

%% Customer Ordering
InPersonCustomer "1" --> "1" Waiter : "requests table"
Waiter "1" --> "0..1" Table : "assigns customer to"

InPersonCustomer "1" --> "1" Waiter : "gives order to"
Waiter "1" --> "1" OrderManager : "forwards order"
TakeAwayCustomer "1" --> "1" OrderManager : "places order online"

%% View Relationships
RestaurantView "1" --> "1" Kitchen : "displays"
RestaurantView "1" --> "0..*" Order : "shows"
MenuView "1" --> "1" Menu : "displays"
CustomerView "1" --> "1" SeatingPlan : "shows"

%% Controller Relationships
OrderController "1" --> "1" OrderManager : "manages"
KitchenController "1" --> "1" Kitchen : "controls"
InventoryController "1" --> "1" Inventory : "maintains"