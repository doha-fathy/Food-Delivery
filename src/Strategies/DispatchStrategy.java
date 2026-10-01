package Strategies;

import BusinessDomain.Order;

public interface DispatchStrategy {

    double getMaxRange();

    double getSpeed();

    int getMaxOrderSize();

    default boolean canDeliver(Order order, double distance) {
        return distance <= getMaxRange() && order.getLineItems().size() <= getMaxOrderSize();
    }

    default double calculateDeliveryTime(double distance) {
        return distance / getSpeed();
    }
}