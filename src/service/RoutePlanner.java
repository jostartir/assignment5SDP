package service;

import model.DeliveryRequest;
import model.Route;
import strategy.RouteStrategy;

import java.util.List;

public class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(RouteStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Strategy must not be null"
            );
        }
        this.strategy = strategy;
    }

    public Route plan(
            List<Route> routes,
            DeliveryRequest request) {

        return strategy.selectRoute(routes, request);
    }
}

