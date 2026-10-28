package strategy;

import model.DeliveryRequest;
import model.Route;
import model.RouteDecision;

import java.util.List;

public interface RouteStrategy {
    Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    );
    RouteDecision explain(
            List<Route> routes,
            DeliveryRequest request
    );

    String getStrategyName();

}
