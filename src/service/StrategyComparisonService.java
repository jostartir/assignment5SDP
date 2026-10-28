package service;

import exception.NoSuitableRouteException;
import model.DeliveryRequest;
import model.Route;
import model.RouteDecision;
import strategy.RouteStrategy;

import java.util.List;

public class StrategyComparisonService {
    public void compare(
            List<Route> routes,
            DeliveryRequest request,
            List<RouteStrategy> strategies
    ) {
        System.out.println("STRATEGY COMPARISON");
        System.out.println("Priority: " + request.priority());
        System.out.println("Budget: " + request.maxBudget());
        System.out.println("Deadline: " + request.maxDeliveryMinutes());

        for (int i = 0; i < strategies.size(); i++) {
            RouteStrategy strategy = strategies.get(i);

            System.out.println();
            System.out.println(strategy.getStrategyName());

            try {
                RouteDecision decision = strategy.explain(routes, request);

                System.out.println("Selected: " + decision.route().id());
                System.out.println(decision.explanation());

                List<String> rejectedRoutes = decision.rejectedRoutes();

                for (int j = 0; j < rejectedRoutes.size(); j++) {
                    System.out.println(rejectedRoutes.get(j));
                }

            } catch (NoSuitableRouteException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }
}
