package Patterns;

import BusinessDomain.Order;

public class StatisticsService implements OrderObserver {

    @Override
    public void update(Order order) {
        System.out.println("Statistics recalculated.");
    }
}