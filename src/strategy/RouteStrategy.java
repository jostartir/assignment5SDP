package strategy;

import model.DeliveryRequest;
import model.Route;

import java.util.List;

public interface RouteStrategy {
    Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    );

    String getStrategyName();

}
