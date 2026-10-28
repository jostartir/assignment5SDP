package strategy;

import service.RouteConstraints;
import model.DeliveryRequest;
import model.Route;

import java.util.List;

public class CheapestRouteStrategy implements RouteStrategy{
    @Override
    public Route selectRoute(List<Route> routes, DeliveryRequest request) {
        Route cheapestRoute = null;
        double cheapestCost = Double.MAX_VALUE;
        for (int i = 0; i < routes.size();i++){
            Route route = routes.get(i);

            if (!RouteConstraints.isSuitable(route,request)) {
                continue;
            }

            double totalCost = route.cost() + route.distanceKm() * 50;
            if (totalCost < cheapestCost){
                cheapestRoute = route;
                cheapestCost = totalCost;
            }


        }
        if (cheapestRoute == null){
            throw new IllegalStateException("No suitable routes");
        }

        return cheapestRoute;

    }


    @Override
    public String getStrategyName() {
        return "Cheapest Route Strategy";
    }
}
