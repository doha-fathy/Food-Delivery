package Services;

import BusinessDomain.*;
import Enums.OrderStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private final Collection<Restaurant> restaurants;
    private final Collection<Customer> customers;
    private final Collection<Rider> riders;
    private final Collection<Order> orders;

    public ReportService(
            Collection<Restaurant> restaurants,
            Collection<Customer> customers,
            Collection<Rider> riders,
            Collection<Order> orders) {

        this.restaurants = restaurants;
        this.customers = customers;
        this.riders = riders;
        this.orders = orders;
    }


    // 1. Total revenue for a given date range


    public double getTotalRevenue(LocalDateTime from, LocalDateTime to) {
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .filter(order -> !order.getPlacedAt().isBefore(from)
                        && !order.getPlacedAt().isAfter(to))
                .mapToDouble(Order::getTotal)
                .sum();
    }


    // 2. Top five restaurants by revenue for a given month

    public double getRestaurantRevenue(Restaurant restaurant, YearMonth month) {
        return orders.stream()
                .filter(order -> order.getRestaurant().equals(restaurant))
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .filter(order -> YearMonth.from(order.getPlacedAt()).equals(month))
                .mapToDouble(Order::getTotal)
                .sum();
    }


    public List<Restaurant> getTopFiveRestaurantsByRevenue(YearMonth month) {
        return restaurants.stream()
                .sorted(Comparator.comparingDouble((Restaurant r) -> getRestaurantRevenue(r, month))
                                .reversed()
                                .thenComparing(Restaurant::getName)
                )
                .limit(5)
                .toList();
    }


    // 3. Average order value per district
    public Map<String, Double> getAverageOrderValuePerDistrict() {

        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(order -> order.getDeliveryAddress().getDistrict(),
                        Collectors.averagingDouble(Order::getTotal)));
    }


    // 4. Restaurants with rating > 4.5 and at least 20 completed orders

    public List<Restaurant> getHighlyRatedRestaurants() {

        return restaurants.stream().filter(restaurant -> restaurant.getAverageRating() > 4.5)
                .filter(restaurant -> orders.stream().filter(order -> order.getRestaurant().equals(restaurant))
                                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                                .count() >= 20
                )
                .toList();
    }

    // 5. Count of orders grouped by current status
    public Map<OrderStatus, Long> getOrdersGroupedByStatus() {

        return orders.stream().collect(Collectors.groupingBy(Order::getStatus,
                        Collectors.counting()
                ));
    }


    // 6. Each rider's completed deliveries and average delivery duration
    //    sorted by deliveries descending

    public Map<Rider, RiderDeliveryReport>  getRiderDeliveryReports() {

        return riders.stream().map(rider -> {List<Order> deliveredOrders = orders.stream()
                    .filter(order -> order.getRider() == rider)
                    .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                    .toList();

                    double averageDuration = deliveredOrders.stream()
                    .mapToLong(order -> Duration.between(order.getPlacedAt(), order.getDeliveredAt()).toMinutes())
                    .average()
                    .orElse(0.0);

                    return new RiderDeliveryReport(rider, (long) deliveredOrders.size(), averageDuration);
                })

                .sorted(Comparator.comparingLong(RiderDeliveryReport::getCompletedDeliveries).reversed()
                )

                .collect(Collectors.toMap(
                        RiderDeliveryReport::getRider,
                        report -> report,
                        (first, second) -> first,
                        LinkedHashMap::new));
    }


    // 7. Most frequently ordered menu item
    public Optional<MenuItem>
    getMostFrequentlyOrderedMenuItem() {

        return orders.stream()
                .flatMap(order -> order.getLineItems().stream())
                .collect(Collectors.groupingBy(LineItem::getMenuItem,
                        Collectors.summingDouble(LineItem::getQuantity)))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }


    // 8. Customer's full order history
    //    newest first + total spent
    public CustomerOrderReport getCustomerOrderHistory(Customer customer) {
        List<Order> customerOrders = orders.stream()
                        .filter(order -> order.getCustomer().equals(customer))
                        .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                        .collect(Collectors.toList());

        double totalSpent = customerOrders.stream()
                        .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                        .mapToDouble(Order::getTotal)
                        .sum();

        return new CustomerOrderReport(customer, customerOrders, totalSpent);
    }


    // 9. Peak ordering hour
    public Optional<Integer> getPeakOrderingHour() {

        return orders.stream().collect(
                Collectors.groupingBy(o -> o.getPlacedAt().getHour()
               ,Collectors.counting()))
                .entrySet()
                .stream()
                .max((a, b) -> Long.compare(a.getValue(), b.getValue()))
                .map(e -> e.getKey());
    }


    // 10. Customers who have not ordered in the last 30 days
    public List<Customer>
    getCustomersWhoHaveNotOrderedInLast30Days() {

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        return customers.stream()
                        .filter(customer -> orders.stream()
                        .noneMatch(order -> order.getCustomer().equals(customer)
                         && !order.getPlacedAt().isBefore(thirtyDaysAgo)))
                .toList();
    }


    public List<Restaurant> browseRestaurants(String district, String cuisine, Double minimumRating, Double priceCeiling) {

        return restaurants.stream()
                // District
                .filter(restaurant -> district == null || restaurant.getDistrict().equalsIgnoreCase(district))

                // Cuisine
                .filter(restaurant -> cuisine == null || restaurant.getCuisineCategories()
                                .stream().anyMatch(c -> c.equalsIgnoreCase(cuisine))
                )

                // Minimum rating
                .filter(restaurant -> minimumRating == null || restaurant.getAverageRating() >= minimumRating
                )
                .filter(restaurant ->
                        priceCeiling == null || restaurant. getMenuItems()
                                .stream()
                                .anyMatch(item -> item.getPrice() <= priceCeiling)
                )

                .toList();
    }
}