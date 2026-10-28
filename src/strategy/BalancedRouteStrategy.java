package strategy;

import model.RouteDecision;
import service.RouteConstraints;
import model.DeliveryRequest;
import model.Route;
import exception.NoSuitableRouteException;

import java.util.ArrayList;
import java.util.List;

public class BalancedRouteStrategy implements RouteStrategy{

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    public BalancedRouteStrategy(
            double timeWeight,
            double costWeight,
            double distanceWeight,
            double emissionWeight
    ) {
        if (timeWeight < 0 || costWeight < 0 || distanceWeight < 0 || emissionWeight < 0) {
            throw new IllegalArgumentException("Weights must not be negative");
        }

        double sum = timeWeight + costWeight + distanceWeight + emissionWeight;

        if (!Double.isFinite(sum) || Math.abs(sum - 1.0) > 0.000001) {
            throw new IllegalArgumentException("Weights must add up to 1");
        }

        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public Route selectRoute(List<Route> routes, DeliveryRequest request) {
        double maxTime = 0;
        double maxCost = 0;
        double maxDistance = 0;
        double maxEmission = 0;
        for (int i = 0; i < routes.size();i++){
            Route route = routes.get(i);
            if (!RouteConstraints.isSuitable(route,request)) {
                continue;
            }

            double time = route.estimatedMinutes() + route.trafficPenaltyMinutes();
            double cost = route.cost() + route.distanceKm() * 50;

            maxTime = Math.max(maxTime,time);
            maxCost = Math.max(maxCost,cost);
            maxDistance = Math.max(maxDistance,route.distanceKm());
            maxEmission = Math.max(maxEmission,route.carbonEmission());
        }

        Route balancedRoute = null;
        double bestScore = Double.MAX_VALUE;

        for (int i = 0; i < routes.size(); i++){
            Route route = routes.get(i);
            if (!RouteConstraints.isSuitable(route,request)) {
                continue;
            }

            double time = route.estimatedMinutes()
                    + route.trafficPenaltyMinutes();

            double cost = route.cost()
                    + route.distanceKm() * 50;

            double normalizedTime = normalize(time,maxTime);
            double normalizedCost = normalize(cost,maxCost);
            double normalizedDistance = normalize(route.distanceKm(),maxDistance);
            double normalizedEmission = normalize(route.carbonEmission(),maxEmission);

            double score = normalizedTime * timeWeight + normalizedCost * costWeight + normalizedDistance * distanceWeight + normalizedEmission * emissionWeight;

            if (score < bestScore){
                balancedRoute = route;
                bestScore = score;
            }




        }
        if (balancedRoute == null){
            throw new NoSuitableRouteException("No route satisfies the delivery constraints");
        }

        return balancedRoute;
    }

    private double normalize(double value, double maxValue) {
        if (maxValue == 0) {
            return 0;
        }

        return value / maxValue;
    }

    @Override
    public RouteDecision explain(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Route selectedRoute = selectRoute(routes, request);

        double maxTime = 0;
        double maxCost = 0;
        double maxDistance = 0;
        double maxEmission = 0;

        List<String> rejectedRoutes = new ArrayList<>();

        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);

            List<String> reasons =
                    RouteConstraints.getRejectionReasons(route, request);

            if (!reasons.isEmpty()) {
                for (int j = 0; j < reasons.size(); j++) {
                    rejectedRoutes.add(
                            route.id() + ": " + reasons.get(j)
                    );
                }

                continue;
            }

            double time = route.estimatedMinutes()
                    + route.trafficPenaltyMinutes();

            double cost = route.cost()
                    + route.distanceKm() * 50;

            maxTime = Math.max(maxTime, time);
            maxCost = Math.max(maxCost, cost);
            maxDistance = Math.max(maxDistance, route.distanceKm());
            maxEmission = Math.max(maxEmission, route.carbonEmission());
        }

        double selectedTime = selectedRoute.estimatedMinutes()
                + selectedRoute.trafficPenaltyMinutes();

        double selectedCost = selectedRoute.cost()
                + selectedRoute.distanceKm() * 50;

        double normalizedTime = normalize(selectedTime, maxTime);
        double normalizedCost = normalize(selectedCost, maxCost);

        double normalizedDistance = normalize(
                selectedRoute.distanceKm(), maxDistance
        );

        double normalizedEmission = normalize(
                selectedRoute.carbonEmission(), maxEmission
        );

        double score = normalizedTime * timeWeight
                + normalizedCost * costWeight
                + normalizedDistance * distanceWeight
                + normalizedEmission * emissionWeight;

        String explanation =
                "Normalized time: " + normalizedTime
                        + " * " + timeWeight
                        + "\nNormalized cost: " + normalizedCost
                        + " * " + costWeight
                        + "\nNormalized distance: " + normalizedDistance
                        + " * " + distanceWeight
                        + "\nNormalized emission: " + normalizedEmission
                        + " * " + emissionWeight
                        + "\nFinal score: " + score
                        + "\nLowest weighted score among suitable routes.";

        return new RouteDecision(
                selectedRoute,
                getStrategyName(),
                explanation,
                rejectedRoutes
        );
    }

    @Override
    public String getStrategyName() {
        return "Balanced Route Strategy";
    }


}