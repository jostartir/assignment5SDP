package service;

import model.DeliveryRequest;
import model.Route;

import java.util.ArrayList;
import java.util.List;

public class RouteConstraints {

    public static List<String> getRejectionReasons(
            Route route,
            DeliveryRequest request
    ) {
        List<String> reasons = new ArrayList<>();

        if (!route.available()) {
            reasons.add("Route is unavailable");
        }

        double totalCost = route.cost()
                + route.distanceKm() * 50;

        if (totalCost > request.maxBudget()) {
            reasons.add("Budget exceeded");
        }

        int totalTime = route.estimatedMinutes()
                + route.trafficPenaltyMinutes();

        if (totalTime > request.maxDeliveryMinutes()) {
            reasons.add("Delivery deadline exceeded");
        }

        return reasons;
    }

    public static boolean isSuitable(
            Route route,
            DeliveryRequest request
    ) {
        List<String> reasons = getRejectionReasons(route, request);

        return reasons.isEmpty();
    }
}