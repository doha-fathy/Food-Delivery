package BusinessDomain;

import Enums.OrderStatus;
import Exceptions.IllegalOrderTransitionException;
import Patterns.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {

    private Integer id;
    private static int nextId = 1;

    private Customer customer;
    private Restaurant restaurant;
    private Address deliveryAddress;
    private List<LineItem> lineItems;
    private double total;
    private LocalDateTime placedAt;
    private LocalDateTime deliveredAt;
    private OrderStatus status;
    private Rider rider;
    private boolean paid;

    private final List<OrderObserver> observers = new ArrayList<>();

    private Order(Builder builder) {
        this.id = nextId++;
        this.customer = builder.customer;
        this.restaurant = builder.restaurant;
        this.deliveryAddress = builder.deliveryAddress;
        this.lineItems = new ArrayList<>(builder.lineItems);
        this.total = builder.total;
        this.placedAt = LocalDateTime.now();
        this.status = OrderStatus.PLACED;
        this.paid = false;
    }

    public static class Builder {

        private Customer customer;
        private Restaurant restaurant;
        private Address deliveryAddress;
        private List<LineItem> lineItems = new ArrayList<>();
        private double total;

        public Builder setCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder setRestaurant(Restaurant restaurant) {
            this.restaurant = restaurant;
            return this;
        }

        public Builder setDeliveryAddress(Address deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder addLineItem(LineItem lineItem) {
            if (lineItem != null) {
                lineItems.add(lineItem);
            }
            return this;
        }

        public Builder setTotal(double total) {
            if (total < 0) {
                throw new IllegalArgumentException("Order total cannot be negative.");
            }
            this.total = total;
            return this;
        }

        public Order build() {
            if (customer == null) {
                throw new IllegalArgumentException("Customer is required.");
            }

            if (restaurant == null) {
                throw new IllegalArgumentException("Restaurant is required.");
            }

            restaurant.checkIfOpen();

            if (deliveryAddress == null) {
                throw new IllegalArgumentException("Delivery address is required.");
            }

            if (!customer.getAddresses().contains(deliveryAddress)) {
                throw new IllegalArgumentException("Delivery address does not belong to the customer.");
            }

            if (lineItems.isEmpty()) {
                throw new IllegalArgumentException("Order must contain at least one item.");
            }

            for (LineItem lineItem : lineItems) {
                lineItem.getMenuItem().checkAvailability();
                lineItem.getMenuItem().checkStock(lineItem.getQuantity());
            }

            return new Order(this);
        }
    }

    public void changeStatus(OrderStatus newStatus) {

        if (!isValidTransition(newStatus)) {
            throw new IllegalOrderTransitionException("Invalid order status transition.");
        }

        status = newStatus;

        if (newStatus == OrderStatus.DELIVERED) {
            deliveredAt = LocalDateTime.now();
        }

        notifyObservers();
    }

    private boolean isValidTransition(OrderStatus newStatus) {

        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            return false;
        }

        if (newStatus == OrderStatus.CANCELLED) {
            return status != OrderStatus.OUT_FOR_DELIVERY;
        }

        switch (status) {
            case PLACED:
                return newStatus == OrderStatus.ACCEPTED;
            case ACCEPTED:
                return newStatus == OrderStatus.PREPARING;
            case PREPARING:
                return newStatus == OrderStatus.READY;
            case READY:
                return newStatus == OrderStatus.ASSIGNED;
            case ASSIGNED:
                return newStatus == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY:
                return newStatus == OrderStatus.DELIVERED;
            default:
                return false;
        }
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public void setTotal(double total) {
        if (total < 0) {
            throw new IllegalArgumentException("Order total cannot be negative.");
        }
        this.total = total;
    }

    public void markPaid() {
        paid = true;
    }

    public boolean isPaid() {
        return paid;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<LineItem> getLineItems() {
        return lineItems.stream().toList();
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Rider getRider() {
        return rider;
    }

    public double getTotal() {
        return total;
    }

    public void addObserver(OrderObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (OrderObserver observer : observers) {
            observer.update(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order)) {
            return false;
        }
        Order other = (Order) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
