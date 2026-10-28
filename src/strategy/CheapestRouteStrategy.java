package strategy;

import model.RouteDecision;
import service.RouteConstraints;
import model.DeliveryRequest;
import model.Route;
import exception.NoSuitableRouteException;

import java.util.ArrayList;
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
            throw new NoSuitableRouteException("No route satisfies the delivery constraints");
        }

        return cheapestRoute;


    }


    @Override
    public RouteDecision explain(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Route selectedRoute = selectRoute(routes, request);

        double distanceCost = selectedRoute.distanceKm() * 50;
        double totalCost = selectedRoute.cost() + distanceCost;

        String explanation =
                "Base cost: " + selectedRoute.cost()
                        + "\nDistance: " + selectedRoute.distanceKm() + " km"
                        + "\nCost per km: 50"
                        + "\nDistance cost: " + distanceCost
                        + "\nFinal cost: " + totalCost
                        + "\nLowest total cost among suitable routes.";

        List<String> rejectedRoutes = new ArrayList<>();

        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);

            List<String> reasons =
                    RouteConstraints.getRejectionReasons(route, request);

            for (int j = 0; j < reasons.size(); j++) {
                rejectedRoutes.add(
                        route.id() + ": " + reasons.get(j)
                );
            }
        }

        return new RouteDecision(
                selectedRoute,
                getStrategyName(),
                explanation,
                rejectedRoutes
        );
    }


    @Override
    public String getStrategyName() {
        return "Cheapest Route Strategy";
    }
}
