erDiagram
    %% Main entities
    RESTAURANT ||--o{ KITCHEN : "has"
    RESTAURANT ||--o{ DINING_AREA : "has"
    RESTAURANT ||--|| INVENTORY : "has"
    RESTAURANT ||--o{ STAFF : "employs"
    RESTAURANT ||--|| MENU : "offers"

    %% Kitchen related entities
    KITCHEN ||--o{ STATION : "contains"
    KITCHEN ||--|| STATION_MANAGER : "uses"
    KITCHEN ||--|| COLLECTION_POINT : "has"
    STATION }|--|| STATION_TYPE : "categorized as"
    STATION }o--o{ RECIPE_TASK : "processes"
    STATION }o--o{ CHEF : "operated by"
    
    %% Staff related entities
    STAFF {
        string id
        string name
        double payPerHour
        double speedMultiplier
    }
    STAFF |{--|| STAFF_TYPE : "categorized as"
    STAFF_TYPE ||--o{ CHEF : "includes"
    STAFF_TYPE ||--o{ WAITER : "includes"
    CHEF {
        string id
        string name
        ChefStrategy workStrategy
        List assignedStations
        boolean isWorking
    }
    WAITER {
        string id
        string name
        List assignedTables
        Queue orderQueue
    }

    %% Order related entities
    ORDER {
        string orderId
        List recipes
        Map modifications
        timestamp createdAt
    }
    ORDER }o--o{ RECIPE : "contains"
    RECIPE ||--o{ CUSTOMER : "chooses by"
    ORDER ||--|| TABLE : "places by"
    CUSTOMER }o--|| TABLE : "places by"


    WAITER ||--o{ ORDER : "processes"
    RECIPE {
        string name
        string orderId
        List tasks
        int totalPrepTime
        boolean isComplete
    }
    RECIPE ||--o{ RECIPE_TASK : "consists of"
    RECIPE }o--o{ INGREDIENT : "requires"
    RECIPE ||--|{ MEAL : "builds"
    RECIPE_TASK {
        string name
        StationType stationType
        double workRequired
        boolean completed
        boolean assigned
    }

    %% Inventory related entities
    INVENTORY ||--o{ INGREDIENT : "stocks"
    INVENTORY ||--o{ INVENTORY_ALERT : "notifies"
    INGREDIENT {
        string name
        double price
        int quantity
    }

    %% Dining related entities
    DINING_AREA ||--o{ TABLE : "arranges"
    DINING_AREA ||--o{ CUSTOMER : "serves"
    TABLE {
        int number
        int capacity
        boolean occupied
    }
    CUSTOMER {
        string id
        string name
        int partySize
    }

    %% Menu related entities
    MENU ||--o{ MENU_ITEM : "lists"
    MENU_ITEM {
        string name
        double price
        boolean available
    }
    MENU_ITEM ||--|| RECIPE : "prepared using"

    %% Collection point entities
    COLLECTION_POINT ||--o{ MEAL : "holds"
    MEAL {
        string name
        string orderId
        List ingredients
        double price
    }
