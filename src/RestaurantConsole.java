import BusinessDomain.*;
import Enums.*;
import Exceptions.*;
import Factories.MenuItemFactory;

import java.time.LocalDate;
import java.util.*;

public class RestaurantConsole {

    private final Platform platform;
    private final Scanner scanner;

    public RestaurantConsole(Platform platform, Scanner scanner) {
        this.platform = platform;
        this.scanner = scanner;
    }

    public void showMenu() {

        int restaurantId = readInt("Restaurant ID: ");

        Optional<Restaurant> restaurantOptional =
                platform.findRestaurant(restaurantId);

        if (!restaurantOptional.isPresent()) {
            System.out.println("Restaurant not found.");
            return;
        }

        Restaurant restaurant = restaurantOptional.get();

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("============== RESTAURANT MENU ==============");
            System.out.println("Restaurant: " + restaurant.getName());
            System.out.println("1. Accept Pending Order");
            System.out.println("2. Reject Pending Order");
            System.out.println("3. Mark Order Preparing");
            System.out.println("4. Mark Order Ready");
            System.out.println("5. Toggle Item Availability");
            System.out.println("6. Add Menu Item");
            System.out.println("7. Remove Menu Item");
            System.out.println("8. Adjust Daily Stock");
            System.out.println("9. View Today's Orders and Revenue");
            System.out.println("0. Back");
            System.out.println("==============================================");

            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> changeOrderStatus(restaurant, OrderStatus.ACCEPTED);
                    case 2 -> changeOrderStatus(restaurant, OrderStatus.CANCELLED);
                    case 3 -> changeOrderStatus(restaurant, OrderStatus.PREPARING);
                    case 4 -> changeOrderStatus(restaurant, OrderStatus.READY);
                    case 5 -> toggleItemAvailability(restaurant);
                    case 6 -> addMenuItem(restaurant);
                    case 7 -> removeMenuItem(restaurant);
                    case 8 -> adjustDailyStock(restaurant);
                    case 9 -> showTodayOrdersAndRevenue(restaurant);
                    case 0 -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException | IllegalOrderTransitionException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void changeOrderStatus(Restaurant restaurant, OrderStatus newStatus) {

        List<Order> orders = new ArrayList<>();

        for (Order order : platform.getOrders().values()) {

            if (!order.getRestaurant().equals(restaurant)) {
                continue;
            }

            if (newStatus == OrderStatus.ACCEPTED || newStatus == OrderStatus.CANCELLED) {
                if (order.getStatus() == OrderStatus.PLACED) {
                    orders.add(order);
                }
            } else if (newStatus == OrderStatus.PREPARING) {
                if (order.getStatus() == OrderStatus.ACCEPTED) {
                    orders.add(order);
                }
            } else if (newStatus == OrderStatus.READY) {
                if (order.getStatus() == OrderStatus.PREPARING) {
                    orders.add(order);
                }
            }
        }

        if (orders.isEmpty()) {
            System.out.println("No matching orders.");
            return;
        }

        System.out.println();
        System.out.println("Orders:");

        for (Order order : orders) {
            System.out.println("Order ID: " + order.getId() + " | Status: " + order.getStatus());
        }

        int orderId = readInt("Order ID: ");

        Optional<Order> orderOptional = platform.findOrder(orderId);

        if (!orderOptional.isPresent()) {
            System.out.println("Order not found.");
            return;
        }

        Order selectedOrder = orderOptional.get();

        if (!selectedOrder.getRestaurant().equals(restaurant)) {
            System.out.println("Order does not belong to this restaurant.");
            return;
        }

        if (newStatus == OrderStatus.CANCELLED) {
            platform.cancelOrder(orderId);
        } else {
            selectedOrder.changeStatus(newStatus);

            if (newStatus == OrderStatus.READY) {
                platform.addReadyOrder(selectedOrder);
            }
        }

        System.out.println("Order status changed to " + selectedOrder.getStatus());
    }

    private void toggleItemAvailability(Restaurant restaurant) {

        if (restaurant.getMenuItems().isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        System.out.println();
        System.out.println("Menu:");

        for (MenuItem item : restaurant.getMenuItems()) {
            System.out.println("Item ID: " + item.getId()
                            + " | Name: " + item.getName()
                            + " | Status: "
                            + (item.getAvailable() ? "Available" : "Not Available")
            );
        }

        int itemId = readInt("Item ID: ");

        Optional<MenuItem> itemOptional = restaurant.getMenuItems()
                        .stream()
                        .filter(item -> item.getId() == itemId)
                        .findFirst();

        if (!itemOptional.isPresent()) {
            System.out.println("Item not found.");
            return;
        }

        MenuItem item = itemOptional.get();
        item.setAvailable(!item.getAvailable());
        System.out.println("Item availability is now: " + (item.getAvailable() ? "Available" : "Not Available"));
    }

    private void addMenuItem(Restaurant restaurant) {

        System.out.println();
        System.out.println("========== ADD MENU ITEM ==========");

        int id = readInt("Item ID: ");
        String type = readString("Type (STANDARD / COMBO / WEIGHTED): ");
        String name = readString("Name: ");
        double price = readPositiveDouble("Price: ");
        String category = readString("Category: ");
        int preparationTime = readPositiveInt("Preparation time: ");
        boolean available = readBoolean("Available? (true/false): ");

        MenuItem item = MenuItemFactory.createMenuItem(type, id, name, price,
                category, preparationTime, available);

        restaurant.addMenuItem(item);

        System.out.println("Menu item added successfully.");
    }

    private void removeMenuItem(Restaurant restaurant) {

        System.out.println();
        System.out.println("========== REMOVE MENU ITEM ==========");

        if (restaurant.getMenuItems().isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        for (MenuItem item : restaurant.getMenuItems()) {
            System.out.println(item.getId() + ". " + item.getName());
        }

        int itemId = readInt("Item ID: ");

        Optional<MenuItem> itemOptional =
                restaurant.getMenuItems()
                        .stream()
                        .filter(item -> item.getId() == itemId)
                        .findFirst();

        if (!itemOptional.isPresent()) {
            System.out.println("Item not found.");
            return;
        }

        if (!restaurant.removeMenuItem(itemOptional.get())) {
            System.out.println("Item could not be removed.");
            return;
        }

        System.out.println("Menu item removed successfully.");
    }

    private void adjustDailyStock(Restaurant restaurant) {

        System.out.println();
        System.out.println("========== ADJUST DAILY STOCK ==========");

        if (restaurant.getMenuItems().isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        for (MenuItem item : restaurant.getMenuItems()) {
            System.out.println("Item ID: " + item.getId() + " | Name: " + item.getName()
             + " | Current Stock: " + (item.getDailyStock() < 0 ? "Unlimited" : item.getDailyStock())
            );
        }

        int itemId = readInt("Item ID: ");
        Optional<MenuItem> itemOptional = restaurant.getMenuItems().stream()
                        .filter(item -> item.getId() == itemId)
                        .findFirst();

        if (!itemOptional.isPresent()) {
            System.out.println("Item not found.");
            return;
        }

        double stock = readNonNegativeDouble("Daily Stock: ");
        itemOptional.get().setDailyStock(stock);

        System.out.println("Daily stock updated successfully.");
    }

    private void showTodayOrdersAndRevenue(Restaurant restaurant) {

        LocalDate today = LocalDate.now();

        List<Order> todayOrders = platform.getOrders().values().stream()
                        .filter(order -> order.getRestaurant().equals(restaurant))
                        .filter(order -> order.getPlacedAt().toLocalDate().equals(today))
                        .toList();

        if (todayOrders.isEmpty()) {
            System.out.println("No orders today.");
            return;
        }

        System.out.println();
        System.out.println("========== TODAY'S ORDERS ==========");

        for (Order order : todayOrders) {
            System.out.println("Order ID: " + order.getId()
                            + " | Status: " + order.getStatus()
                            + " | Total: " + order.getTotal()
                            + " EGP");
        }

        double revenue = todayOrders.stream()
                        .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                        .mapToDouble(Order::getTotal)
                        .sum();

        System.out.println();
        System.out.println("Today's Revenue: " + revenue + " EGP");
    }

    private int readInt(String message) {

        while (true) {
            System.out.print(message);

            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private int readPositiveInt(String message) {

        while (true) {
            int value = readInt(message);

            if (value > 0) {
                return value;
            }

            System.out.println("Value must be greater than zero.");
        }
    }

    private double readPositiveDouble(String message) {

        while (true) {
            System.out.print(message);

            try {
                double value = Double.parseDouble(scanner.nextLine());

                if (value > 0) {
                    return value;
                }

                System.out.println("Value must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private double readNonNegativeDouble(String message) {

        while (true) {
            System.out.print(message);

            try {
                double value = Double.parseDouble(scanner.nextLine());

                if (value >= 0) {
                    return value;
                }

                System.out.println("Value cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String readString(String message) {

        while (true) {
            System.out.print(message);
            String value = scanner.nextLine();

            if (!value.trim().isEmpty()) {
                return value;
            }

            System.out.println("Value cannot be empty.");
        }
    }

    private boolean readBoolean(String message) {

        while (true) {
            System.out.print(message);
            String value = scanner.nextLine().trim();

            if (value.equalsIgnoreCase("true")) {
                return true;
            }

            if (value.equalsIgnoreCase("false")) {
                return false;
            }

            System.out.println("Please enter true or false.");
        }
    }
}
