import exception.NoSuitableRouteException;
import model.DeliveryPriority;
import model.DeliveryRequest;
import model.Location;
import model.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.RoutePlanner;
import service.StrategyComparisonService;
import service.StrategySelector;
import strategy.BalancedRouteStrategy;
import strategy.CheapestRouteStrategy;
import strategy.DistanceFirstStrategy;
import strategy.EcoFriendlyRouteStrategy;
import strategy.FastestRouteStrategy;
import strategy.RouteStrategy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RouteStrategiesTest {

    private final Location origin = new Location("Warehouse", 43.24, 76.89);
    private final Location destination = new Location("Client", 43.28, 76.95);

    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.STANDARD, 10000, 120, false
    );

    @DisplayName("1. Fastest must consider traffic, not just estimatedMinutes.")
    @Test
    void fastestConsidersTrafficPenalty() {
        Route a = route("A", 10, 10, 500, 2, true, 30);
        Route b = route("B", 12, 20, 500, 2, true, 5);

        Route result = new FastestRouteStrategy().selectRoute(List.of(a, b), request);

        assertEquals("B", result.id());
    }

    @DisplayName("2. Lowest base cost is not necessarily lowest total cost.")
    @Test
    void cheapestUsesBaseCostAndDistanceCost() {
        Route a = route("A", 20, 20, 100, 1, true, 0);
        Route b = route("B", 2, 30, 400, 2, true, 0);

        Route result = new CheapestRouteStrategy().selectRoute(List.of(a, b), request);

        assertEquals("B", result.id());
    }

    @DisplayName("3. Eco must consider both emissions and distance.")
    @Test
    void ecoUsesEmissionAndDistanceWeights() {
        Route a = route("A", 20, 20, 100, 0.1, true, 0);
        Route b = route("B", 1, 20, 100, 1, true, 0);

        Route result = new EcoFriendlyRouteStrategy(0.8, 0.2).selectRoute(List.of(a, b), request);

        assertEquals("B", result.id());
    }

    @DisplayName("4. Balanced must normalize, apply weights and exclude invalid outliers.")
    @Test
    void balancedUsesNormalizedValuesAndConfigurableWeights() {
        Route fast = route("FAST", 20, 10, 1000, 10, true, 0);
        Route middle = route("MIDDLE", 10, 25, 500, 3, true, 0);
        Route eco = route("ECO", 5, 50, 100, 1, true, 0);
        Route unavailable = route("OUTLIER", 1000, 10000, 100000, 10000, false, 0);
        List<Route> routes = List.of(fast, middle, eco, unavailable);

        Route result = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2).selectRoute(routes, request);

        assertEquals("MIDDLE", result.id());

        Route timeOnly = new BalancedRouteStrategy(1, 0, 0, 0).selectRoute(routes, request);
        Route emissionOnly = new BalancedRouteStrategy(0, 0, 0, 1).selectRoute(routes, request);

        assertEquals("FAST", timeOnly.id());
        assertEquals("ECO", emissionOnly.id());
    }

    @DisplayName("5. One planner, the same data, three runtime switches.")
    @Test
    void plannerSupportsThreeRuntimeSwitches() {
        Route fast = route("FAST", 10, 10, 2000, 5, true, 0);
        Route cheap = route("CHEAP", 10, 40, 100, 5, true, 0);
        Route eco = route("ECO", 12, 30, 500, 0.1, true, 0);
        List<Route> routes = List.of(fast, cheap, eco);

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("FAST", planner.plan(routes, request).id());

        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("CHEAP", planner.plan(routes, request).id());

        planner.setStrategy(new EcoFriendlyRouteStrategy(0.8, 0.2));
        assertEquals("ECO", planner.plan(routes, request).id());

        planner.setStrategy(new BalancedRouteStrategy(0, 1, 0, 0));
        assertEquals("CHEAP", planner.plan(routes, request).id());
    }

    @DisplayName("6. All four business priorities map to their registered strategies.")
    @Test
    void selectorChoosesStrategyForEachPriority() {
        StrategySelector selector = new StrategySelector();
        RouteStrategy fastest = new FastestRouteStrategy();
        RouteStrategy cheapest = new CheapestRouteStrategy();
        RouteStrategy eco = new EcoFriendlyRouteStrategy(0.8, 0.2);
        RouteStrategy balanced = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);

        selector.registrate(DeliveryPriority.URGENT, fastest);
        selector.registrate(DeliveryPriority.ECONOMY, cheapest);
        selector.registrate(DeliveryPriority.ECO, eco);
        selector.registrate(DeliveryPriority.STANDARD, balanced);

        assertSame(fastest, selector.selectStrategy(new DeliveryRequest(DeliveryPriority.URGENT, 10000, 120, false)));
        assertSame(cheapest, selector.selectStrategy(new DeliveryRequest(DeliveryPriority.ECONOMY, 10000, 120, false)));
        assertSame(eco, selector.selectStrategy(new DeliveryRequest(DeliveryPriority.ECO, 10000, 120, true)));
        assertSame(balanced, selector.selectStrategy(request));
    }

    @DisplayName("7. A route that is best in every metric still loses if unavailable.")
    @Test
    void allStrategiesRejectUnavailableRoute() {
        Route unavailable = route("UNAVAILABLE", 1, 1, 1, 0, false, 0);
        Route valid = route("VALID", 10, 30, 500, 2, true, 0);
        List<Route> routes = List.of(unavailable, valid);
        List<RouteStrategy> strategies = allStrategies();

        for (int i = 0; i < strategies.size(); i++) {
            RouteStrategy strategy = strategies.get(i);

            assertEquals("VALID", strategy.selectRoute(routes, request).id(), strategy.getClass().getSimpleName());
        }
    }

    @DisplayName("8. Budget is compared with total cost, including distance cost.")
    @Test
    void allStrategiesRespectTotalBudget() {
        Route expensive = route("EXPENSIVE", 1, 1, 1000, 0, true, 0);
        Route affordable = route("AFFORDABLE", 10, 30, 100, 2, true, 0);
        DeliveryRequest limited = new DeliveryRequest(
                DeliveryPriority.STANDARD, 700, 120, false
        );
        List<RouteStrategy> strategies = allStrategies();

        for (int i = 0; i < strategies.size(); i++) {
            RouteStrategy strategy = strategies.get(i);

            assertEquals("AFFORDABLE", strategy.selectRoute(
                    List.of(expensive, affordable), limited
            ).id(), strategy.getClass().getSimpleName());

            assertThrows(NoSuitableRouteException.class,
                    () -> strategy.selectRoute(List.of(expensive), limited),
                    strategy.getClass().getSimpleName());
        }
    }

    @DisplayName("9. Deadline checks must include the traffic penalty.")
    @Test
    void allStrategiesRespectDeadlineIncludingTraffic() {
        Route late = route("LATE", 1, 5, 100, 0, true, 30);
        Route onTime = route("ON_TIME", 10, 25, 200, 2, true, 5);
        DeliveryRequest limited = new DeliveryRequest( DeliveryPriority.STANDARD, 10000, 30, false);
        List<RouteStrategy> strategies = allStrategies();

        for (int i = 0; i < strategies.size(); i++) {
            RouteStrategy strategy = strategies.get(i);

            assertEquals("ON_TIME", strategy.selectRoute(List.of(late, onTime), limited).id(), strategy.getClass().getSimpleName());

            assertThrows(NoSuitableRouteException.class, () -> strategy.selectRoute(List.of(late), limited), strategy.getClass().getSimpleName());
        }
    }

    @DisplayName("10. Nonempty list, but every route violates a hard constraint.")
    @Test
    void allStrategiesThrowWhenNoRouteSatisfiesConstraints() {
        Route unavailable = route("UNAVAILABLE", 1, 1, 1, 0, false, 0);
        Route expensive = route("EXPENSIVE", 1, 5, 1000, 1, true, 0);
        Route late = route("LATE", 2, 50, 100, 2, true, 0);
        List<Route> routes = List.of(unavailable, expensive, late);
        DeliveryRequest limited = new DeliveryRequest( DeliveryPriority.STANDARD, 700, 30, false);
        List<RouteStrategy> strategies = allStrategies();

        for (int i = 0; i < strategies.size(); i++) {
            RouteStrategy strategy = strategies.get(i);

            assertThrows(NoSuitableRouteException.class, () -> strategy.selectRoute(routes, limited), strategy.getClass().getSimpleName());
        }
    }

    @DisplayName("11. Invalid sum, negative weight and nonfinite weights are rejected.")
    @Test
    void balancedRejectsInvalidWeights() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.3));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(-0.1, 0.5, 0.3, 0.3));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.NaN, 0.3, 0.1, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.POSITIVE_INFINITY, 0.3, 0.1, 0.2));
    }

    @DisplayName("12. The fifth strategy prioritizes distance over speed.")
    @Test
    void distanceFirstChoosesShortestSuitableRoute() {
        Route fast = route("FAST", 15, 10, 100, 0, true, 0);
        Route shortRoute = route("SHORT", 4, 40, 500, 5, true, 0);

        Route result = new DistanceFirstStrategy().selectRoute(List.of(fast, shortRoute), request);

        assertEquals("SHORT", result.id());
    }

    @DisplayName("13. Exercise the actual comparison service with two strategies.")
    @Test
    void comparisonModeShowsDifferentSelectionsForSameRequest() {
        Route fast = route("FAST", 10, 10, 2000, 2, true, 0);
        Route cheap = route("CHEAP", 10, 40, 100, 2, true, 0);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try (PrintStream capturedOut = new PrintStream(output)) {
            System.setOut(capturedOut);

            new StrategyComparisonService().compare(List.of(fast, cheap), request, List.of(new FastestRouteStrategy(), new CheapestRouteStrategy()));
        } finally {
            System.setOut(originalOut);
        }

        String text = output.toString();

        assertTrue(text.contains("Selected: FAST"));
        assertTrue(text.contains("Selected: CHEAP"));
        assertTrue(text.indexOf("Selected: FAST") < text.indexOf("Selected: CHEAP"));
        assertTrue(text.contains("Final time: 10 min"));
        assertTrue(text.contains("Final cost: 600.0"));
    }

    @DisplayName("14. Equality with both limits is allowed.")
    @Test
    void allStrategiesAcceptExactBudgetAndDeadlineBoundaries() {
        Route boundary = route("BOUNDARY", 10, 25, 500, 1, true, 5);
        DeliveryRequest exactLimits = new DeliveryRequest(DeliveryPriority.STANDARD, 1000, 30, false);
        List<RouteStrategy> strategies = allStrategies();

        for (int i = 0; i < strategies.size(); i++) {
            RouteStrategy strategy = strategies.get(i);

            assertSame(boundary, strategy.selectRoute(List.of(boundary), exactLimits), strategy.getClass().getSimpleName());
        }
    }

    @DisplayName("15. Empty input is an explicit failure, not a null result.")
    @Test
    void allStrategiesThrowForEmptyRouteList() {
        List<RouteStrategy> strategies = allStrategies();

        for (int i = 0; i < strategies.size(); i++) {
            RouteStrategy strategy = strategies.get(i);

            assertThrows(NoSuitableRouteException.class, () -> strategy.selectRoute(List.of(), request), strategy.getClass().getSimpleName());
        }
    }


    private Route route(
            String id,
            double distance,
            int minutes,
            double baseCost,
            double emission,
            boolean available,
            int traffic
    ) {
        return new Route(id, origin, destination, distance, minutes,
                baseCost, emission, available, traffic);
    }

    private List<RouteStrategy> allStrategies() {
        return List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(0.8, 0.2),
                new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2),
                new DistanceFirstStrategy()
        );
    }
}
