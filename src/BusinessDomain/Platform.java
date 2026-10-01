package BusinessDomain;

import Comparators.ReadyOrderComparator;
import Enums.AvailabilityStatus;
import Enums.OrderStatus;
import Patterns.*;
import Promotions.Promotion;
import Services.ReportService;

import java.util.*;

public class Platform {

    private static Platform instance;

    private Map<Integer, Restaurant> restaurants;
    private Map<Integer, Customer> customers;
    private Map<Integer, Rider> riders;
    private Map<Integer, Order> orders;
    private Map<String, Promotion> promotions;

    private PriorityQueue<Order> readyOrders;
    private ReportService reportService;

    private Platform() {
        restaurants = new HashMap<>();
        customers = new HashMap<>();
        riders = new HashMap<>();
        orders = new HashMap<>();
        promotions = new HashMap<>();
        readyOrders = new PriorityQueue<>(new ReadyOrderComparator());
        reportService = new ReportService(restaurants.values(), customers.values(), riders.values(), orders.values());
    }

    public static Platform getInstance() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    public boolean addRestaurant(Restaurant restaurant) {
        if (restaurant == null || findRestaurant(restaurant.getId()).isPresent()) {
            return false;
        }

        restaurants.put(restaurant.getId(), restaurant);
        return true;
    }

    public Optional<Restaurant> findRestaurant(int id) {
        return Optional.ofNullable(restaurants.get(id));
    }

    public boolean removeRestaurant(int id) {
        if (!restaurants.containsKey(id)) {
            return false;
        }
        restaurants.remove(id);
        return true;
    }

    public boolean addCustomer(Customer customer) {
        if (customer == null || customers.containsKey(customer.getId())) {
            return false;
        }
        customers.put(customer.getId(), customer);
        return true;
    }

    public Optional<Customer> findCustomer(int id) {
        return Optional.ofNullable(customers.get(id));
    }

    public boolean removeCustomer(int id) {
        if (!customers.containsKey(id)) {
            return false;
        }

        customers.remove(id);
        return true;
    }

    public boolean addRider(Rider rider) {
        if (rider == null || riders.containsKey(rider.getId())) {
            return false;
        }
        riders.put(rider.getId(), rider);
        return true;
    }

    public boolean removeRider(int id) {
        if (!riders.containsKey(id)) {
            return false;
        }

        Rider rider = riders.get(id);
        if (rider.getActiveOrder() != null) {
            throw new IllegalStateException("Rider cannot be removed while having an active order.");
        }

        riders.remove(id);
        return true;
    }

    public Optional<Rider> findRider(int id) {
        return Optional.ofNullable(riders.get(id));
    }

    public Rider getRiderById(int riderId) {
        Rider rider = riders.get(riderId);
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found.");
        }
        return rider;
    }

    public void goOnDuty(int riderId) {
        Rider rider = getRiderById(riderId);

        if (rider.getActiveOrder() != null) {
            throw new IllegalStateException("Rider cannot go on duty while having an active order.");
        }

        rider.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        dispatchReadyOrderToRider(rider);
    }

    public void goOffDuty(int riderId) {
        Rider rider = getRiderById(riderId);

        if (rider.getActiveOrder() != null) {
            throw new IllegalStateException("Rider cannot go off duty while having an active order.");
        }

        rider.setAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
    }

    public Order getAssignedOrder(int riderId) {
        return getRiderById(riderId).getActiveOrder();
    }

    public void assignOrderToRider(int riderId, int orderId) {
        Rider rider = getRiderById(riderId);
        Order order = findOrder(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (rider.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new IllegalStateException("Rider is not available.");
        }

        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Order is not ready.");
        }

        readyOrders.remove(order);
        rider.assignOrder(order);
        order.changeStatus(OrderStatus.ASSIGNED);
    }

    private void dispatchReadyOrderToRider(Rider rider) {
        Order order = readyOrders.poll();

        if (order != null) {
            rider.assignOrder(order);
            order.changeStatus(OrderStatus.ASSIGNED);
        }
    }

    public void markOrderPickedUp(int riderId) {
        Rider rider = getRiderById(riderId);
        Order order = rider.getActiveOrder();

        if (order == null) {
            throw new IllegalStateException("No currently assigned order.");
        }

        if (order.getStatus() != OrderStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Order cannot be picked up from status: " + order.getStatus()
            );
        }

        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);
    }

    public void markOrderDelivered(int riderId) {
        Rider rider = getRiderById(riderId);
        Order order = rider.getActiveOrder();

        if (order == null) {
            throw new IllegalStateException("No currently assigned order.");
        }

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("Order cannot be delivered from status: " + order.getStatus());
        }

        order.changeStatus(OrderStatus.DELIVERED);
        rider.completeDelivery();
        order.getCustomer().completeOrder();
    }

    public boolean addOrder(Order order) {
        if (order == null || orders.containsKey(order.getId())) {
            return false;
        }

        Map<MenuItem, Double> quantities = new HashMap<>();

        for (LineItem lineItem : order.getLineItems()) {
            quantities.merge(lineItem.getMenuItem(), lineItem.getQuantity(), Double::sum
            );
        }

        for (Map.Entry<MenuItem, Double> entry : quantities.entrySet()) {
            entry.getKey().checkStock(entry.getValue());
        }

        for (Map.Entry<MenuItem, Double> entry : quantities.entrySet()) {
            entry.getKey().reduceStock(entry.getValue());
        }

        order.addObserver(new CustomerNotificationService());
        order.addObserver(new RiderDashboardService());
        order.addObserver(new AuditLogService());
        order.addObserver(new StatisticsService());

        orders.put(order.getId(), order);
        return true;
    }

    public Optional<Order> findOrder(int id) {
        return Optional.ofNullable(orders.get(id));
    }

    public boolean addReadyOrder(Order order) {
        if (order == null
                || order.getStatus() != OrderStatus.READY
                || order.getRider() != null
                || readyOrders.contains(order)) {
            return false;
        }

        readyOrders.add(order);

        for (Rider rider : riders.values()) {
            if (rider.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE
                    && rider.getActiveOrder() == null) {dispatchReadyOrderToRider(rider);
                break;
            }
        }

        return true;
    }

    public Order getNextReadyOrder() {
        return readyOrders.peek();
    }

    public Order dispatchNextReadyOrder() {
        return readyOrders.poll();
    }

    public List<String> getAllCuisineCategories() {
        Set<String> categories = new HashSet<>();

        for (Restaurant restaurant : restaurants.values()) {
            categories.addAll(restaurant.getCuisineCategories());
        }

        return categories.stream().toList();
    }

    public List<Restaurant> getRestaurantsSortedByRating() {
        return restaurants.values()
                .stream()
                .sorted(Comparator.comparing(Restaurant::getAverageRating).reversed().thenComparing(Restaurant::getName))
                .toList();
    }

    public List<Restaurant> getOpenRestaurantsSortedByRating() {
        return restaurants.values()
                .stream()
                .filter(restaurant -> restaurant.getStatus() == Enums.RestaurantStatus.OPEN)
                .sorted(Comparator.comparing(Restaurant::getAverageRating).reversed().thenComparing(Restaurant::getName))
                .toList();
    }

    public List<Restaurant> browseRestaurants(
            String district,
            String cuisine,
            Double minimumRating,
            Double priceCeiling) {

        return restaurants.values()
                .stream()
                .filter(restaurant -> restaurant.getStatus() == Enums.RestaurantStatus.OPEN)
                .filter(restaurant -> district == null
                        || district.trim().isEmpty()
                        || restaurant.getDistrict().equalsIgnoreCase(district))
                .filter(restaurant -> cuisine == null
                        || cuisine.trim().isEmpty()
                        || restaurant.getCuisineCategories()
                        .stream()
                        .anyMatch(c -> c.equalsIgnoreCase(cuisine)))
                .filter(restaurant -> minimumRating == null
                        || restaurant.getAverageRating() >= minimumRating)
                .filter(restaurant -> priceCeiling == null
                        || restaurant.getMenuItems()
                        .stream()
                        .anyMatch(item -> item.getPrice() <= priceCeiling))
                .sorted(
                        Comparator.comparing(Restaurant::getAverageRating)
                                .reversed()
                                .thenComparing(Restaurant::getName)
                )
                .toList();
    }

    public List<Restaurant> searchRestaurants(
            Customer customer,
            String searchText) {

        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            throw new IllegalArgumentException("Search text is required.");
        }

        customer.addSearch(searchText);

        String text = searchText.trim().toLowerCase();

        return restaurants.values()
                .stream()
                .filter(restaurant -> restaurant.getName().toLowerCase().contains(text)
                                        || restaurant.getCuisineCategories().stream()
                                        .anyMatch(cuisine -> cuisine.toLowerCase().contains(text))
                )
                .toList();
    }

    public void cancelOrder(int orderId) {
        Order currentOrder = findOrder(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found."));

        currentOrder.changeStatus(OrderStatus.CANCELLED);
        readyOrders.remove(currentOrder);

        if (currentOrder.getRider() != null) {
            Rider rider = currentOrder.getRider();

            if (rider.getActiveOrder() == currentOrder) {
                rider.cancelActiveOrder();
            }
        }

        if (currentOrder.isPaid()) {
            Customer customer = currentOrder.getCustomer();
            customer.setWalletBalance(customer.getWalletBalance() + currentOrder.getTotal()
            );
        }

        for (LineItem lineItem : currentOrder.getLineItems()) {
            lineItem.getMenuItem().restoreStock(lineItem.getQuantity());
        }
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> customerOrders = new ArrayList<>();

        for (Order order : orders.values()) {
            if (order.getCustomer() != null && order.getCustomer().getId() == customerId) {
                customerOrders.add(order);
            }
        }

        return Collections.unmodifiableList(customerOrders);
    }

    public boolean addPromotion(String code, Promotion promotion) {
        if (code == null || code.trim().isEmpty() || promotion == null) {
            return false;
        }

        String normalizedCode = code.trim().toUpperCase();

        if (promotions.containsKey(normalizedCode)) {
            return false;
        }

        promotions.put(normalizedCode, promotion);
        return true;
    }

    public Optional<Promotion> findPromotion(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(promotions.get(code.trim().toUpperCase()));
    }

    public Map<Integer, Restaurant> getRestaurants() {
        return Collections.unmodifiableMap(restaurants);
    }

    public Map<Integer, Customer> getCustomers() {
        return Collections.unmodifiableMap(customers);
    }

    public Map<Integer, Rider> getRiders() {
        return Collections.unmodifiableMap(riders);
    }

    public Map<Integer, Order> getOrders() {
        return Collections.unmodifiableMap(orders);
    }

    public Map<String, Promotion> getPromotions() {
        return Collections.unmodifiableMap(promotions);
    }

    public ReportService getReportService() {
        return reportService;
    }
}
