package strategy;

import exception.NoSuitableRouteException;
import model.DeliveryRequest;
import model.Route;
import model.RouteDecision;
import service.RouteConstraints;

import java.util.ArrayList;
import java.util.List;

public class DistanceFirstStrategy implements RouteStrategy {

    @Override
    public Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Route shortestRoute = null;
        double shortestDistance = Double.MAX_VALUE;

        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);

            if (!RouteConstraints.isSuitable(route, request)) {
                continue;
            }

            if (route.distanceKm() < shortestDistance) {
                shortestRoute = route;
                shortestDistance = route.distanceKm();
            }
        }

        if (shortestRoute == null) {
            throw new NoSuitableRouteException(
                    "No route satisfies the delivery constraints"
            );
        }

        return shortestRoute;
    }

    @Override
    public RouteDecision explain(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Route selectedRoute = selectRoute(routes, request);

        String explanation =
                "Distance: " + selectedRoute.distanceKm() + " km"
                        + "\nShortest distance among suitable routes.";

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
        return "Distance First Strategy";
    }
}