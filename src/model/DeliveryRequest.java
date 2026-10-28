package model;

public record DeliveryRequest(DeliveryPriority priority,
                              double maxBudget,
                              int maxDeliveryMinutes,
                              boolean ecoPreferred) {
}
