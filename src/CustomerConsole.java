
import BusinessDomain.*;
import Enums.OrderStatus;
import Exceptions.InsufficientWalletBalanceException;
import Exceptions.IllegalOrderTransitionException;
import Promotions.Promotion;
import Services.TotalPricingService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class CustomerConsole {

    private final Platform platform;
    private final Scanner scanner;

    private Customer currentCustomer;

    public CustomerConsole(Platform platform, Scanner scanner) {
        this.platform = platform;
        this.scanner = scanner;
    }

    public void showMenu() {

        while (true) {

            System.out.println();
            System.out.println("============== CUSTOMER ==============");
            System.out.println("1. Existing Customer");
            System.out.println("2. New Customer");
            System.out.println("0. Back");
            System.out.println("======================================");

            int choice = readInt("Choose an option: ");

            try {

                switch (choice) {

                    case 1 -> loginExistingCustomer();
                    case 2 ->registerNewCustomer();
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }

            } catch (IllegalArgumentException | IllegalOrderTransitionException e) {
                System.out.println(e.getMessage());
            }
        }
    }



    private void loginExistingCustomer() {

        int customerId = readInt("Customer ID: ");

        currentCustomer = platform.findCustomer(customerId)
        .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        System.out.println();
        System.out.println("Welcome, " + currentCustomer.getName() + "!");
        customerMenu();
    }

    // NEW CUSTOMER
    private void registerNewCustomer() {

        System.out.println();
        System.out.println("========== NEW CUSTOMER ==========");

        int id = readInt("Customer ID: ");
        String name = readString("Name: ");
        String mobileNumber = readString("Mobile Number: ");
        double walletBalance = readNonNegativeDouble("Wallet Balance: ");

        Customer customer = new Customer(id, name, mobileNumber, walletBalance);

        System.out.println();
        System.out.println("========== DELIVERY ADDRESS ==========");
        String district = readString("District: ");
        String details = readString("Address Details: ");
        Address address = new Address(district, details);
        customer.addAddress(address);

        if (!platform.addCustomer(customer)) {
            throw new IllegalArgumentException("Customer ID already exists.");
        }
        currentCustomer = customer;

        System.out.println();
        System.out.println("Customer registered successfully.");
        System.out.println("Welcome, " + currentCustomer.getName() + "!");

        customerMenu();
    }

    // CUSTOMER MENU
    private void customerMenu() {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("============== CUSTOMER MENU ================");
            System.out.println("1. Browse Restaurants");
            System.out.println("2. Search Restaurants");
            System.out.println("3. View Menu");
            System.out.println("4. Place Order");
            System.out.println("5. Pay From Wallet");
            System.out.println("6. Track Order");
            System.out.println("7. Cancel Order");
            System.out.println("8. Order History");
            System.out.println("0. Back");
            System.out.println("============================================");

            int choice = readInt("Choose an option: ");

            try {

                switch (choice) {

                    case 1 -> browseRestaurants();
                    case 2 -> searchRestaurants();
                    case 3 -> viewMenu();
                    case 4 -> placeOrder();
                    case 5 -> payFromWallet();
                    case 6 -> trackOrder();
                    case 7 -> cancelOrder();
                    case 8 -> orderHistory();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid choice.");
                }

            } catch (IllegalArgumentException | IllegalOrderTransitionException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // BROWSE RESTAURANTS
    private void browseRestaurants() {

        System.out.println();
        System.out.println("========== BROWSE RESTAURANTS =========="
        );

        String district = readOptional("District (Enter to skip): ");
        String cuisine = readOptional("Cuisine (Enter to skip): ");
        Double minimumRating = readOptionalDouble("Minimum Rating (Enter to skip): ");
        Double priceCeiling = readOptionalDouble("Price Ceiling (Enter to skip): ");

        List<Restaurant> restaurants = platform.browseRestaurants(district, cuisine, minimumRating, priceCeiling);

        if (restaurants.isEmpty()) {
            System.out.println("Nothing found.");
            return;
        }

        int number = 1;

        for (Restaurant restaurant : restaurants) {

            System.out.println();
            System.out.println(number + ". " + restaurant.getName());
            System.out.println("   District: " + restaurant.getDistrict());
            System.out.println("   Cuisines: " + restaurant.getCuisineCategories());
            System.out.println("   Rating: " + restaurant.getAverageRating());
            number++;
        }
    }
    // SEARCH RESTAURANTS

    private void searchRestaurants() {

        System.out.println();
        System.out.println("========== SEARCH RESTAURANTS ==========");
        String searchText = readString("Search: ");

        List<Restaurant> restaurants = platform.searchRestaurants(currentCustomer, searchText);

        if (restaurants.isEmpty()) {
            System.out.println("Nothing found.");
            return;
        }

        for (Restaurant restaurant : restaurants) {

            System.out.println();
            System.out.println(restaurant);
            System.out.println("-----------------------------------------");
        }
    }


    // VIEW MENU
    private void viewMenu() {

        System.out.println();
        System.out.println("========== VIEW MENU ==========");
        int restaurantId = readInt("Restaurant ID: ");

        Optional<Restaurant> restaurant = platform.findRestaurant(restaurantId);

        if (!restaurant.isPresent()) {
            System.out.println("Restaurant not found.");

            return;
        }

        System.out.println();
        System.out.println("Menu - " + restaurant.get().getName());
        String currentCategory = "";

        for (MenuItem item : restaurant.get().getMenuItems()) {

            if (!item.getCategory().equals(currentCategory)) {
                currentCategory = item.getCategory();
                System.out.println();
                System.out.println("[" + currentCategory + "]");}

            System.out.println(item);
        }
    }

    // PLACE ORDER
    private void placeOrder() {

        System.out.println();
        System.out.println("========== PLACE ORDER ==========");
        Customer customer = currentCustomer;

        Restaurant restaurant = chooseRestaurant()
         .orElseThrow(() -> new IllegalArgumentException("Restaurant is required."));

        restaurant.checkIfOpen();

        Address address = chooseAddress(customer).
        orElseThrow(() -> new IllegalArgumentException("Delivery address is required."));

        Order.Builder builder = new Order.Builder()
                        .setCustomer(customer)
                        .setRestaurant(restaurant)
                        .setDeliveryAddress(address);

        boolean hasItem = false;

        while (true) {

            System.out.println();
            System.out.println("Menu:");
            for (MenuItem item : restaurant.getMenuItems()) {
                System.out.println(item);
                System.out.println("----------------------------------------");
            }

            int itemId = readInt("Enter item ID (0 to finish): ");

            if (itemId == 0) {
                break;
            }

            Optional<MenuItem> selectedItem = restaurant.getMenuItems()
                            .stream()
                            .filter(item -> item.getId() == itemId)
                            .findFirst();

            if (!selectedItem.isPresent()) {

                System.out.println("Invalid item. Please try again.");
                continue;
            }

            selectedItem.get().checkAvailability();

            double quantity = readPositiveDouble(selectedItem.get() instanceof WeightedItem ?
                            "Enter quantity in kilograms: " : "Enter quantity: ");

            selectedItem.get().checkStock(quantity);

            builder.addLineItem(new LineItem(selectedItem.get(), quantity));
            hasItem = true;
        }

        if (!hasItem) {

            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        String promoCode = readOptional("Promotion code (Enter to skip): ");

        Promotion promotion = null;

        if (!promoCode.isEmpty()) {

            promotion = platform.findPromotion(promoCode).
                    orElseThrow(() -> new IllegalArgumentException("Promotion not found."));
        }

        double distanceKm = readNonNegativeDouble("Distance from restaurant to delivery address (km): ");

        Order order = builder.setTotal(0).build();

        TotalPricingService pricingService =
                new TotalPricingService(promotion);
        double total = pricingService.calculateTotal(order, distanceKm);

        order.setTotal(total);

        if (!platform.addOrder(order)) {
            throw new IllegalArgumentException("Order could not be created.");
        }

        System.out.println();
        System.out.println("Order created successfully.");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Subtotal: " + pricingService.getLastSubtotal() + " EGP");
        System.out.println("Delivery Fee: " + pricingService.getLastDeliveryFee() + " EGP");
        System.out.println("Service Fee: " + pricingService.getLastServiceFee() + " EGP");
        System.out.println("Promotion Discount: " + pricingService.getLastPromotionDiscount() + " EGP"
        );
        System.out.println("Total: " + pricingService.getLastTotal() + " EGP"
        );
    }

    private void addWalletBalance() {

        System.out.println();System.out.println("========== ADD WALLET BALANCE ==========");
        double amount = readPositiveDouble("Amount: ");
        currentCustomer.addToWallet(amount);
        System.out.println("Wallet balance updated successfully.");
        System.out.println("New wallet balance: " + currentCustomer.getWalletBalance() + " EGP");
    }

    // PAY FROM WALLET
    private void payFromWallet() {

        System.out.println();
        System.out.println("========== PAY FROM WALLET =========="
        );

        int orderId = readInt("Order ID: ");
        Order order = platform.findOrder(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (order.isPaid()) {
            System.out.println("Order is already paid.");
            return;
        }

        if (!order.getCustomer().equals(currentCustomer)) {

            throw new IllegalArgumentException("This order does not belong to the current customer.");
        }

        try {
            System.out.print("Do you want to add money to your wallet? (yes/no): ");
            String answer = scanner.nextLine();

            if (answer.equalsIgnoreCase("yes")) {

                double amount = readPositiveDouble("Enter amount to add: ");
                currentCustomer.addToWallet(amount);

                System.out.println("Wallet balance updated.");
                System.out.println("New wallet balance: " + currentCustomer.getWalletBalance() + " EGP");
            }

            currentCustomer.pay(order.getTotal());
            order.markPaid();

            System.out.println("Payment successful.");
            System.out.println("New wallet balance: " + currentCustomer.getWalletBalance() + " EGP");

        } catch (InsufficientWalletBalanceException e) {
            System.out.println(e.getMessage());
        }
    }

    // TRACK ORDER
    private void trackOrder() {

        System.out.println();
        System.out.println("========== TRACK ORDER ==========");

        int orderId = readInt("Order ID: ");

        Order order = platform.findOrder(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (!order.getCustomer().equals(currentCustomer)) {
            throw new IllegalArgumentException("This order does not belong to the current customer.");
        }

        System.out.println("Status: " + order.getStatus());

        long elapsedMinutes = Duration.between(order.getPlacedAt(), LocalDateTime.now()).toMinutes();
        System.out.println("Elapsed Time: " + elapsedMinutes + " minutes");
    }

    // CANCEL ORDER
    private void cancelOrder() {

        System.out.println();
        System.out.println("========== CANCEL ORDER ==========");

        int orderId = readInt("Order ID: ");

        Order order = platform.findOrder(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (!order.getCustomer().equals(currentCustomer)) {
            throw new IllegalArgumentException("This order does not belong to the current customer.");
        }

        try {

            platform.cancelOrder(orderId);
            System.out.println("Order cancelled successfully.");

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    // ORDER HISTORY
    private void orderHistory() {

        System.out.println();
        System.out.println("========== ORDER HISTORY ==========");

        List<Order> orders = new ArrayList<>(platform.getOrdersByCustomer(currentCustomer.getId()));

        if (orders.isEmpty()) {

            System.out.println("No orders found.");
            return;
        }

        orders.sort(Comparator.comparing(Order::getPlacedAt).reversed());

        double totalSpent = 0;

        for (Order order : orders) {

            System.out.println();
            System.out.println("Order ID: " + order.getId());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Total: " + order.getTotal() + " EGP");

            if (order.getStatus() == OrderStatus.DELIVERED) {totalSpent += order.getTotal();}
        }

        System.out.println();
        System.out.println("Lifetime Total Spent: " + totalSpent + " EGP");
    }

    // =========================================================

    private Optional<Restaurant> chooseRestaurant() {
        List<Restaurant> restaurants = platform.getOpenRestaurantsSortedByRating();

        if (restaurants.isEmpty()) {

            System.out.println("No restaurants available.");
            return Optional.empty();
        }

        System.out.println();
        System.out.println("============ Available Restaurants ============");

        for (Restaurant restaurant : restaurants) {

            System.out.println();
            System.out.println(restaurant);
            System.out.println("------------------------------------");
        }

        while (true) {
            int id = readInt("Enter restaurant ID: ");

            Optional<Restaurant> restaurant = platform.findRestaurant(id);

            if (restaurant.isPresent() && restaurant.get().getStatus() == Enums.RestaurantStatus.OPEN) {
                return restaurant;
            }

            System.out.println("Invalid restaurant ID. Please try again.");
        }
    }


    // CHOOSE ADDRESS
    private Optional<Address> chooseAddress(Customer customer) {

        if (customer.getAddresses().isEmpty()) {

            System.out.println("No saved addresses available.");

            return Optional.empty();
        }

        System.out.println();
        System.out.println("============= Saved Addresses ==============");

        List<Address> addresses = customer.getAddresses();

        for (int i = 0; i < addresses.size(); i++) {
            Address address = addresses.get(i);

            System.out.println((i + 1) + ". " + address.getDistrict() + " - " + address.getDetails());
        }

        while (true) {

            int choice = readInt("Choose address number: ");

            if (choice >= 1 && choice <= addresses.size()) {

                return Optional.of(addresses.get(choice - 1));
            }

            System.out.println("Invalid address. Please try again.");
        }
    }

    // Input methods

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

    private Double readOptionalDouble(String message) {

        while (true) {

            String value = readOptional(message);

            if (value.isEmpty()) {
                return null;
            }

            try {

                double number = Double.parseDouble(value);

                if (number >= 0) {
                    return number;
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

    private String readOptional(
            String message) {
        System.out.print(message);

        return scanner.nextLine().trim();
    }
}