package Patterns;

import BusinessDomain.Order;

public interface OrderObserver {

    void update(Order order);
}