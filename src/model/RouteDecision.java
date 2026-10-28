package model;

import java.util.List;

public record RouteDecision(
        Route route,
        String strategyName,
        String explanation,
        List<String> rejectedRoutes
) {}