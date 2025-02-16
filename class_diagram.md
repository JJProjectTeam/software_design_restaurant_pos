```mermaid
classDiagram
    OrderManager *-- Order
    Kitchen -- OrderManager
    Order *-- State
    Order *-- Recipe
    Waiter *-- OrderController
    OrderController *-- Kitchen
    Recipe <|-- ConcreteRecipe
    Command <|--OrderForwardCommand
    Command <|--OrderBackCommand
    OrderManager *-- OrderStateInvoker
    OrderController *-- OrderManager

    ConcreteRecipe ..> Meal

    Observer <|--InventoryAlertSystem
    Inventory -- InventoryAlertSystem

    State <|-- TodoState
    State <|-- DoingState
    State <|-- DoneState

    class Observer {
        <<interface>>
        update()
    }

    class InventoryAlertSystem {

    }

    class Ingredient {
        +name: String
        +cost: float
    }

    class Inventory {
        +stock: Dict[Ingredient, quantity: int]

        +addIngredient()
        +removeIngredient()
        +checkStock()
        +addStock()
        +removeStock()
    }

    class OrderManager {
        -orders: List~Order~
        -currentOrder: Order
        +getAllOrders() List~Order~
        <!-- We want the orderController to interface with the orderManager to get the State of the Order -->
        +getOrderStatus(order: Order)
        +selectOrder(Order)
        +addOrder(Order)
        +removeOrder(Order)
        +getCurrentOrder() Order
        +stepForwardOrder()
        +stepBackwardOrder()
    }

    class Kitchen {
        -orderManager: OrderManager
        +prepMeal()
        -updateInventory(ingredients: Dictionary)
    }

    class OrderStateInvoker {
        -orderManager: OrderManager
        +orderForwardStep()
        +orderBackStep()
        +undo()
        +setCommands(orderForwardCommand, orderBackCommand)
    }

    class Command {
        <<interface>>
        +execute(order: Order)
        +undo(order: Order)
    }

    class OrderForwardCommand {
        -previousState: State
        +execute(order: Order)
        +undo(order: Order)
    }

    class OrderBackCommand {
        -previousState: State
        +execute(order: Order)
        +undo(order: Order)
    }

    class Recipe {
        <<interface>>
        +meal: Meal
        +addIngredients()
        +addBaseIngredients()
        +build() Meal
    }

    class ConcreteRecipe {
        -ingredients: List~Ingredient~
        +addIngredients(Ingredient)
        +addBaseIngredients(Ingredient)
        +build() Meal
    }

    <!-- I removed the add and remove methods from the meal, since the builder for the meal would already contain the edits made by the user. We don't need for the meal to also be responsible for the same. m -->
    class Meal {
        +ingredients: List~Ingredient~
        +totalCost()
    }

    class Order {
        +state: State
        +item: List~Recipe~

        -setState(state: State)
        +processOrder()
        +cancelOrder()
        +pauseOrder()
    }

    class State {
        <<interface>>
        +processOrder(order: Order)
        +cancelOrder(order: Order)
        +pauseOrder(order: Order)
    }

    class TodoState {
        +processOrder(order: Order)
        +cancelOrder(order: Order)
        +pauseOrder(order: Order)
    }

    class DoingState {
        +processOrder(order: Order)
        +cancelOrder(order: Order)
        +pauseOrder(order: Order)
    }

    class DoneState {
        +processOrder(order: Order)
        +cancelOrder(order: Order)
        +pauseOrder(order: Order)
    }
    
    <!-- Reasons for the change: Waiter interacts with ordercontroller, therefore having one makes sense  -->
    <!-- Waiter might be asked to check the status as well as placing it. (could add cancel too but we can do that later ) -->
    class Waiter {
        -orderController: OrderController
        +placeOrder(recipes: List~Recipe~)
        +checkOrderStatus(order: Order)
    }

    <!-- Reasons for the change, the order controller has to pass the orders to the kitchen (placeOrder), the checkOrder was to interface with the Waiter, the checkOrder, I've made checkOrderIngredients() private since the orderController would only one using it before it places an order.  -->
    <!-- I've added a OrderManager, since if the OrderController wishes to know the state of the order it would have to talk to the orderManager. The waiter would have to talk to someone about the orderStatus and if it's only talking to the OrderController it should then interface with the OrderManager, I assume you don't want the waiter to have an orderManager too? -->

    class OrderController {
        orderManager : OrderManager
        kitchen: Kitchen
        -checkOrderIngredients(order: Order)
        +checkOrderStatus(order: Order)
        +placeOrder(recipies: List~Recipe~)

        +makeRecipe()
        +sendOrder()
    }

    %% class OrderPlacedEvent {
    %%     +order: Order
    %% }

    %% class MealPreparedEvent {
    %%     +order: Order
    %% }

    %% class StockThresholdReachedEvent {
    %%     +ingredient: Ingredient
    %% }

    %% class EventBus {
    %%     +publish(event: Event)
    %%     +subscribe(eventType: Class, handler: EventHandler)
    %% }

    %% class EventHandler {
    %%     +handle(event: Event)
    %% }

    %% class OrderPlacedHandler implements EventHandler {
    %%     +kitchen: Kitchen
    %%     +handle(event: OrderPlacedEvent)
    %% }

    %% class MealPreparedHandler implements EventHandler {
    %%     +orderManager: OrderManager
    %%     +handle(event: MealPreparedEvent)
    %% }

    %% class StockThresholdReachedHandler implements EventHandler {
    %%     +inventory: Inventory
    %%     +handle(event: StockThresholdReachedEvent)
    %% }

    %% This is complicating things since do we want the customer to be able to place order too?
    %% class Customer {
    %%     +order: Order
    %%     +name: String
    %%     +contactInfo: String
    %%     +placeOrder(order: Order)
    %% }

    %% class AlertService {
    %%     <<Interface>>
    %%     +notify(event: AlertEvent)
    %% }

    %% class AlertEvent {
    %%     +message: String
    %%     +source: Object
    %% }

    %% class LowStock {
    %%     +ingredient: Ingredient
    %% }
```
