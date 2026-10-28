import model.*;
import service.*;
import strategy.*;
import exception.*;


import java.util.List;

public class Main{
    public static void main(String[] args){
        Location origin = new Location("Warehouse", 43.24, 76.89);
        Location destination = new Location("Client", 43.28, 76.95);

        List<Route> routes = List.of(
                new Route("R1", origin, destination,
                        10, 20, 1500, 4, true, 10),

                new Route("R2", origin, destination,
                        15, 18, 2200, 6, true, 2),

                new Route("R3", origin, destination,
                        12, 35, 500, 3, true, 5),

                new Route("R4", origin, destination,
                        6, 40, 1200, 0.5, true, 5),

                new Route("R5", origin, destination,
                        14, 25, 1300, 3, true, 8),

                new Route("R6", origin, destination,
                        20, 10, 3000, 8, false, 0),

                new Route("R7", origin, destination,
                        8, 45, 800, 2, true, 5),

                new Route("R8", origin, destination,
                        16, 30, 1000, 1.5, true, 4)
        );

        DeliveryRequest request = new DeliveryRequest(
                DeliveryPriority.STANDARD,
                10000,
                120,
                false
        );

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        Route fastest = planner.plan(routes,request);

        System.out.println("Fastest route = " + fastest);
        planner.setStrategy(new CheapestRouteStrategy());

        Route cheapest = planner.plan(routes,request);
        System.out.println("Cheapest route = " + cheapest);
        planner.setStrategy(new EcoFriendlyRouteStrategy(0.8,0.2));

        Route eco = planner.plan(routes,request);
        System.out.println("Eco route = " + eco);

        planner.setStrategy(new BalancedRouteStrategy(0.4,0.3,0.1,0.2));
        Route balanced = planner.plan(routes,request);

        System.out.println("Balanced route = " + balanced);

        StrategySelector selector = new StrategySelector();

        selector.registrate(DeliveryPriority.URGENT,new FastestRouteStrategy());
        selector.registrate(DeliveryPriority.ECONOMY,new CheapestRouteStrategy());
        selector.registrate(DeliveryPriority.ECO,new EcoFriendlyRouteStrategy(0.8,0.2));
        selector.registrate(DeliveryPriority.STANDARD,new BalancedRouteStrategy(0.4,0.3,0.1,0.2));

        RouteStrategy selectedStrategy = selector.selectStrategy(request);

        planner.setStrategy(selectedStrategy);

        Route automaticRoute = planner.plan(routes,request);
        System.out.println("Automatic strategy = " + selectedStrategy.getStrategyName());
        System.out.println("Automatic route = " + automaticRoute.id());

        DeliveryRequest urgentRequest = new DeliveryRequest(DeliveryPriority.URGENT,10000,120,false);
        DeliveryRequest economyRequest = new DeliveryRequest(DeliveryPriority.ECONOMY,3000,120,false);
        DeliveryRequest ecoRequest = new DeliveryRequest(DeliveryPriority.ECO,10000,120,true);


        List<DeliveryRequest> requests = List.of(urgentRequest,economyRequest,ecoRequest);

        for (int i = 0; i < requests.size(); i++){
            DeliveryRequest currentRequest = requests.get(i);

            RouteStrategy strategyForRequest = selector.selectStrategy(currentRequest);

            planner.setStrategy(strategyForRequest);

            Route selectedRoute = planner.plan(routes,currentRequest);

            System.out.println(currentRequest.priority() + " -> " + selectedRoute.id());


        }
        DeliveryRequest largeBudgetRequest = new DeliveryRequest(DeliveryPriority.URGENT,10000,120,false);
        DeliveryRequest smallBudgetRequest = new DeliveryRequest(DeliveryPriority.URGENT,2000,120,false);

        planner.setStrategy(new FastestRouteStrategy());

        Route firstResult = planner.plan(routes,largeBudgetRequest);
        Route secondResult = planner.plan(routes,smallBudgetRequest);

        System.out.println("Budget 10000 - > " + firstResult );
        System.out.println("Budget 2000 - > " + secondResult);

        DeliveryRequest impossibleRequest = new DeliveryRequest(DeliveryPriority.URGENT,100,120,false);
        planner.setStrategy(new FastestRouteStrategy());

        try {
            Route result = planner.plan(routes, impossibleRequest);

            System.out.println("Selected route: " + result.id());
        } catch (NoSuitableRouteException exception) {
            System.out.println("Delivery failed: " + exception.getMessage());
        }

        System.out.println("-----------");

        List<RouteStrategy> comparisonStrategies = List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(0.8, 0.2),
                new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2),
                new DistanceFirstStrategy()
        );

        StrategyComparisonService comparisonService =
                new StrategyComparisonService();

        comparisonService.compare(routes, request, comparisonStrategies);


        System.out.println("-----------------");
        System.out.println("FAILURE SCENARIOS");

        planner.setStrategy(new FastestRouteStrategy());

        try {
            planner.plan(List.of(), request);
        } catch (NoSuitableRouteException exception) {
            System.out.println("Empty list: " + exception.getMessage());
        }

        try {
            new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.3);
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid weights: " + exception.getMessage());
        }

        DeliveryRequest noValidRouteRequest = new DeliveryRequest(
                DeliveryPriority.URGENT,
                100,
                120,
                false
        );

        try {
            planner.plan(routes, noValidRouteRequest);
        } catch (NoSuitableRouteException exception) {
            System.out.println("No valid route: " + exception.getMessage());
        }

    }

}
