package Patterns;

import BusinessDomain.Order;

public class RiderDashboardService implements OrderObserver {

    @Override
    public void update(Order order) {

        System.out.println("Rider dashboard updated for Order " + order.getId());
    }
}