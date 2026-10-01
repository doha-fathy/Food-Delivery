package Patterns;

import BusinessDomain.Order;

public class AuditLogService implements OrderObserver {

    @Override
    public void update(Order order) {

        System.out.println("Audit log recorded for Order " + order.getId());
    }
}