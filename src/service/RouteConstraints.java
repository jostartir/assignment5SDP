package service;

import model.DeliveryRequest;
import model.Route;

public class RouteConstraints {
    public static boolean isSuitable(Route route, DeliveryRequest request){
        if (!route.available()){
            return false;
        }

        double totalCost = route.cost() + route.distanceKm() * 50;

        if (totalCost > request.maxBudget()){
            return false;
        }
        int totalTime = route.estimatedMinutes() + route.trafficPenaltyMinutes();

        if (totalTime > request.maxDeliveryMinutes()){
            return false;
        }

        return true;


    }
}
