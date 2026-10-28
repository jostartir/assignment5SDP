package strategy;

import service.RouteConstraints;
import model.DeliveryRequest;
import model.Route;
import exception.NoSuitableRouteException;

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

    public String getStrategyName() {
        return "Eco Friendly Route Strategy";
    }
}
