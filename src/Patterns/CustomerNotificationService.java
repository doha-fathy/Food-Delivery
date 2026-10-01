package Patterns;

import BusinessDomain.Order;

public class CustomerNotificationService implements OrderObserver {

    @Override
    public void update(Order order) {

        System.out.println("Customer notified about Order " + order.getId());
    }
}