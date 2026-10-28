package strategy;

import service.RouteConstraints;
import model.DeliveryRequest;
import model.Route;
import exception.NoSuitableRouteException;
import model.RouteDecision;
import service.RouteConstraints;
import java.util.ArrayList;

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
    public RouteDecision explain(
            List<Route> routes,
            DeliveryRequest request
    ){
        Route selectedRoute = selectRoute(routes,request);

        int totalTime = selectedRoute.estimatedMinutes() + selectedRoute.trafficPenaltyMinutes();

        String explanation = "Estimated time: " + selectedRoute.estimatedMinutes() + " min"
                + "\nTraffic penalty: +" + selectedRoute.trafficPenaltyMinutes() + " min"
                + "\nFinal time: " + totalTime + " min"
                + "\nLowest total time among suitable routes.";

        List<String> rejectedRoutes = new ArrayList<>();

        for (int i = 0; i < routes.size(); i++){
            Route route = routes.get(i);

            List<String> reasons = RouteConstraints.getRejectionReasons(route,request);

            for (int j = 0; j < reasons.size(); j++){
                rejectedRoutes.add(route.id() + ": " + reasons.get(j));
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
    public String getStrategyName(){
        return "Fatest Route Strategy";
    };
}
