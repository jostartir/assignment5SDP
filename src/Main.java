import model.DeliveryPriority;
import model.DeliveryRequest;
import model.Location;
import model.Route;
import service.RoutePlanner;
import strategy.BalancedRouteStrategy;
import strategy.CheapestRouteStrategy;
import strategy.EcoFriendlyRouteStrategy;
import strategy.FastestRouteStrategy;

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

    }
}
