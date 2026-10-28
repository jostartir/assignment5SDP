package strategy;

import service.RouteConstraints;
import model.DeliveryRequest;
import model.Route;
import exception.NoSuitableRouteException;

import java.util.List;

public class FastestRouteStrategy implements RouteStrategy{
    @Override
    public Route selectRoute(List<Route> routes, DeliveryRequest request) {
        Route fastestRoute = null;
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            if (!RouteConstraints.isSuitable(route,request)) {
                continue;
            }
            int totalTime = route.estimatedMinutes() + route.trafficPenaltyMinutes();

            if (fastestRoute == null) {
                fastestRoute = route;
            } else {
                int fastestTime = fastestRoute.estimatedMinutes() + fastestRoute.trafficPenaltyMinutes();
                if (totalTime < fastestTime) {
                    fastestRoute = route;
                }

            }

        }

        if (fastestRoute == null){
            throw new NoSuitableRouteException("No route satisfies the delivery constraints");
        }

        return fastestRoute;
    }

    @Override
    public String getStrategyName(){
        return "Fatest Route Strategy";
    };
}
