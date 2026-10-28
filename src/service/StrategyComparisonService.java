package service;

import exception.NoSuitableRouteException;
import model.DeliveryRequest;
import model.Route;
import strategy.RouteStrategy;

import java.util.List;

public class StrategyComparisonService {
    public void compare(List<Route> routes,DeliveryRequest request,List<RouteStrategy> strategies){
        System.out.println("STRATEGY COMPARISON");
        System.out.println("Priority: " + request.priority());
        System.out.println("Budget: " + request.maxBudget());
        System.out.println("Deadline: " + request.maxDeliveryMinutes());

        for (int i = 0; i < strategies.size(); i++){
            RouteStrategy strategy = strategies.get(i);

            System.out.println();
            System.out.println(strategy.getStrategyName());

            try{

                Route route = strategy.selectRoute(routes,request);
                int totalTime = route.estimatedMinutes() + route.trafficPenaltyMinutes();
                double totalCost = route.cost() + route.distanceKm() * 50;

                System.out.println("Route: " + route.id());
                System.out.println("Time: " + totalTime + " min");
                System.out.println("Cost: " + totalCost);
                System.out.println("Distance: " + route.distanceKm() + " km");
                System.out.println("CO2: " + route.carbonEmission() + " kg");


            }catch(NoSuitableRouteException exception){
                System.out.println(exception.getMessage());
            }
        }

    }
}
