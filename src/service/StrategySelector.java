package service;

import model.DeliveryPriority;
import model.DeliveryRequest;
import strategy.RouteStrategy;

import java.util.HashMap;
import java.util.Map;

public class StrategySelector {
    private final Map<DeliveryPriority, RouteStrategy> strategies = new HashMap<>();
    public void registrate(DeliveryPriority priority, RouteStrategy strategy){
        if (priority == null || strategy == null){
            throw new IllegalArgumentException("Priority and strategy must not be null");
        }

        strategies.put(priority,strategy);
    }
    public RouteStrategy selectStrategy(DeliveryRequest request){
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        RouteStrategy strategy = strategies.get(request.priority());

        if (strategy == null){
            throw new IllegalArgumentException("No strategy registered for this priority");
        }

        return strategy;

    }
}
