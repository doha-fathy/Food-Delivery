package Services;

import BusinessDomain.Customer;
import BusinessDomain.Order;

import java.util.List;

public class CustomerOrderReport {

    private final Customer customer;
    private final List<Order> orders;
    private final double totalSpent;

    public CustomerOrderReport(Customer customer, List<Order> orders, double totalSpent) {

        this.customer = customer;
        this.orders = orders;
        this.totalSpent = totalSpent;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public double getTotalSpent() {
        return totalSpent;
    }
}