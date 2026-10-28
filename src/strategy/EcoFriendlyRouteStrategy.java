package strategy;

import model.RouteDecision;
import service.RouteConstraints;
import model.DeliveryRequest;
import model.Route;
import exception.NoSuitableRouteException;

import java.util.ArrayList;
import java.util.List;

public class EcoFriendlyRouteStrategy implements RouteStrategy{
    private final double emissionWeight;
    private final double distanceWeight;

    public EcoFriendlyRouteStrategy( double emissionWeight, double distanceWeight) {
        this.emissionWeight = emissionWeight;
        this.distanceWeight = distanceWeight;
    }
    @Override
    public Route selectRoute(List<Route> routes, DeliveryRequest request){

        Route ecoRoute = null;
        double bestScore = Double.MAX_VALUE;

        for (int i = 0; i < routes.size(); i++){
            Route route = routes.get(i);

            if (!RouteConstraints.isSuitable(route,request)) {
                continue;
            }
            double score = route.carbonEmission() * emissionWeight + route.distanceKm() * distanceWeight;
            if (score < bestScore) {
                ecoRoute = route;
                bestScore = score;
            }

        }

        if (ecoRoute == null){
            throw new NoSuitableRouteException("No route satisfies the delivery constraints");
        }
        return ecoRoute;
    }

    @Override
    public RouteDecision explain(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Route selectedRoute = selectRoute(routes, request);

        double emissionScore =
                selectedRoute.carbonEmission() * emissionWeight;

        double distanceScore =
                selectedRoute.distanceKm() * distanceWeight;

        double ecoScore = emissionScore + distanceScore;

        String explanation =
                "Carbon emission: " + selectedRoute.carbonEmission() + " kg"
                        + "\nEmission weight: " + emissionWeight
                        + "\nEmission contribution: " + emissionScore
                        + "\nDistance: " + selectedRoute.distanceKm() + " km"
                        + "\nDistance weight: " + distanceWeight
                        + "\nDistance contribution: " + distanceScore
                        + "\nEco score: " + ecoScore
                        + "\nLowest eco score among suitable routes.";

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
        return "Eco Friendly Route Strategy";
    }
}
