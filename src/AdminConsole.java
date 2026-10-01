import BusinessDomain.*;
import Enums.RestaurantStatus;
import Factories.PromotionFactory;
import Promotions.*;
import Services.CustomerOrderReport;
import Enums.VehicleType;
import Strategies.BicycleDispatchStrategy;
import Strategies.CarDispatchStrategy;
import Strategies.DispatchStrategy;
import Strategies.MotorcycleDispatchStrategy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminConsole {

    private final Platform platform;
    private final Scanner scanner;

    public AdminConsole(Platform platform, Scanner scanner) {
        this.platform = platform;
        this.scanner = scanner;
    }

    public void showMenu() {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("========== ADMIN & REPORTS ==========");
            System.out.println("1. Add Restaurant");
            System.out.println("2. Remove Restaurant");
            System.out.println("3. Add Rider");
            System.out.println("4. Remove Rider");
            System.out.println("5. Delete Customer");
            System.out.println("6. Create Promotion");
            System.out.println("7. Run Reports");
            System.out.println("8. View Platform Statistics");
            System.out.println("0. Back");
            System.out.println("=====================================");

            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> addRestaurant();
                    case 2 -> removeRestaurant();
                    case 3 -> addRider();
                    case 4 -> removeRider();
                    case 5 -> deleteCustomer();
                    case 6 -> createPromotion();
                    case 7 -> runReports();
                    case 8 -> viewPlatformStatistics();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void addRestaurant() {

        System.out.println();
        System.out.println("========== ADD RESTAURANT ==========");

        int id = readInt("Restaurant ID: ");
        if (platform.findRestaurant(id).isPresent()) {
            System.out.println("Restaurant ID already exists.");
            return;
        }

        System.out.print("restaurant name: ");
        String name =  scanner.nextLine().trim();

        System.out.print("restaurant district: ");
        String district =  scanner.nextLine().trim();

        System.out.print("Average Rating: ");
        double rating = readPositiveDouble("Average Rating: ");

        System.out.println("Restaurant Status:");
        System.out.println("1. OPEN");
        System.out.println("2. CLOSED");

        int statusChoice = readInt("Choose status: ");

        RestaurantStatus status;

        if (statusChoice == 1) {
            status = RestaurantStatus.OPEN;
        } else if (statusChoice == 2) {
            status = RestaurantStatus.CLOSED;
        } else {
            throw new IllegalArgumentException("Invalid restaurant status.");
        }

        List<String> cuisineCategories = new ArrayList<>();

        // At least one cuisine category is required
        System.out.print( "Cuisine Category: ");
        String cuisine =  scanner.nextLine().trim();

        cuisineCategories.add(cuisine);

        while (true) {

            cuisine = readOptional("Add another cuisine category (Enter to finish)\nCuisine Category:" );
            if (cuisine.isEmpty()) {
                break;
            }
            cuisineCategories.add(cuisine);
        }

        Restaurant restaurant = new Restaurant(id, name, district, cuisineCategories, rating, status);

        if (!platform.addRestaurant(restaurant)) {
            System.out.println("Restaurant already exists.");
            return;
        }

        System.out.println("Restaurant added successfully.");
    }

    private void removeRestaurant() {

        System.out.println();
        System.out.println("========== REMOVE RESTAURANT ==========");

        int restaurantId = readInt("Restaurant ID: ");

        if (!platform.removeRestaurant(restaurantId)) {
            System.out.println("Restaurant not found.");
            return;
        }

        System.out.println("Restaurant removed successfully.");
    }

    private void addRider() {

        System.out.println();
        System.out.println("========== ADD RIDER ==========");

        int id = readInt("Rider ID: ");
        if (platform.findRider(id).isPresent()) {
            System.out.println("Rider ID already exists.");
            return;
        }

        String name = readString("Rider name: ");

        System.out.println("Vehicle Type:");
        System.out.println("1. MOTORCYCLE");
        System.out.println("2. BICYCLE");
        System.out.println("3. CAR");

        int vehicleChoice = readInt("Choose vehicle type: ");

        VehicleType vehicleType;
        DispatchStrategy dispatchStrategy;

        if (vehicleChoice == 1) {
            vehicleType = VehicleType.MOTORCYCLE;
            dispatchStrategy = new MotorcycleDispatchStrategy();
        } else if (vehicleChoice == 2) {
            vehicleType = VehicleType.BICYCLE;
            dispatchStrategy = new BicycleDispatchStrategy();
        } else if (vehicleChoice == 3) {
            vehicleType = VehicleType.CAR;
            dispatchStrategy = new CarDispatchStrategy();
        } else {
            System.out.println("Invalid vehicle type.");
            return;
        }

        String district = readString("Current district: ");

        Rider rider = new Rider(id, name, vehicleType, district, dispatchStrategy);

        if (!platform.addRider(rider)) {
            System.out.println("Rider already exists.");
            return;
        }

        System.out.println("Rider added successfully.");
    }

    private void removeRider() {

        System.out.println();
        System.out.println("========== REMOVE RIDER ==========");

        int riderId = readInt("Rider ID: ");

        if (!platform.removeRider(riderId)) {
            System.out.println("Rider not found.");
            return;
        }

        System.out.println("Rider removed successfully.");
    }

    private void deleteCustomer() {

        System.out.println();
        System.out.println("========== DELETE CUSTOMER ==========");

        int customerId = readInt("Customer ID: ");

        if (!platform.removeCustomer(customerId)) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println("Customer deleted successfully.");
    }

    private void createPromotion() {

        System.out.println();
        System.out.println("========== CREATE PROMOTION ==========");

        String code = readString("Promotion Code: ").toUpperCase();

        System.out.println("1. Percentage Off");
        System.out.println("2. Fixed Amount Off");
        System.out.println("3. Free Delivery");

        int type = readInt("Promotion Type: ");

        LocalDate expiryDate = LocalDate.parse(readString("Expiry Date (YYYY-MM-DD): "));
        double minimumSubtotal = readNonNegativeDouble("Minimum Subtotal: ");
        String restrictedDistrict = readOptional("Restricted District (leave empty to apply to all districts): ");
        String firstTimeInput = readString("First-time customers only? (yes/no): ");

        boolean firstTimeOnly;

        if (firstTimeInput.equalsIgnoreCase("yes")) {
            firstTimeOnly = true;
        } else if (firstTimeInput.equalsIgnoreCase("no")) {
            firstTimeOnly = false;
        } else {
            System.out.println("Please enter yes or no.");
            return;
        }
        double percentage = 0;
        double maxDiscount = 0;
        double amount = 0;

        if (type == 1) {

            percentage = readPercentage("Percentage (0-100): ");
            maxDiscount = readPositiveDouble("Maximum Discount: ");

        } else if (type == 2) {
            amount = readPositiveDouble("Fixed Discount Amount: ");

        } else if (type != 3) {
            System.out.println("Invalid promotion type.");
            return;
        }

        Promotion promotion;

        try {

            promotion = PromotionFactory.createPromotion(type, percentage, maxDiscount, amount,
                    expiryDate, minimumSubtotal, restrictedDistrict, firstTimeOnly);

        } catch (IllegalArgumentException e) {

            System.out.println(e.getMessage());
            return;
        }

        if (!platform.addPromotion(code, promotion)) {
            System.out.println("Promotion code already exists.");
            return;
        }

        System.out.println("Promotion created successfully.");
    }

    private void runReports() {

        System.out.println();
        System.out.println("=============== REPORTS ===============");
        System.out.println("1. Total Revenue");
        System.out.println("2. Top Five Restaurants By Revenue");
        System.out.println("3. Average Order Value Per District");
        System.out.println("4. Highly Rated Restaurants");
        System.out.println("5. Orders Grouped By Status");
        System.out.println("6. Rider Delivery Reports");
        System.out.println("7. Most Frequently Ordered Menu Item");
        System.out.println("8. Customer Order History");
        System.out.println("9. Peak Ordering Hour");
        System.out.println("10. Customers Who Have Not Ordered In Last 30 Days");
        System.out.println("0. Back");
        System.out.println("========================================");

        int choice = readInt("Choose a report: ");

        switch (choice) {

            case 1 -> {
                String fromText = readString("From (YYYY-MM-DD): ");
                String toText = readString("To (YYYY-MM-DD): ");

                double revenue = platform.getReportService().getTotalRevenue(
                                LocalDate.parse(fromText).atStartOfDay(),
                                LocalDate.parse(toText).plusDays(1).atStartOfDay());

                System.out.println();
                System.out.println("Total Revenue: " + revenue + " EGP");
            }

            case 2 -> {
                String monthText = readString("Month (YYYY-MM): ");

                System.out.println();
                System.out.println("========== TOP FIVE RESTAURANTS ==========");

                platform.getReportService().getTopFiveRestaurantsByRevenue(YearMonth.parse(monthText))
                        .forEach(System.out::println);
            }

            case 3 -> {
                System.out.println();
                System.out.println("========== AVERAGE ORDER VALUE ==========");

                platform.getReportService()
                        .getAverageOrderValuePerDistrict()
                        .forEach((district, value) ->
                                System.out.println(district + " : " + value + " EGP"));
            }

            case 4 -> {
                System.out.println();
                System.out.println("========== HIGHLY RATED RESTAURANTS =========="
                );

                platform.getReportService()
                        .getHighlyRatedRestaurants()
                        .forEach(restaurant -> System.out.println("Restaurant: "
                         + restaurant.getName() + " | Rating: " + restaurant.getAverageRating()));
            }

            case 5 -> {
                System.out.println();
                System.out.println("========== ORDERS BY STATUS ==========");

                platform.getReportService()
                        .getOrdersGroupedByStatus()
                        .forEach((status, count) -> System.out.println(status + " : " + count));
            }

            case 6 -> {
                System.out.println();
                System.out.println("========== RIDER DELIVERY REPORT ==========");

                platform.getReportService()
                        .getRiderDeliveryReports()
                        .forEach((rider, report) ->
                                        System.out.println("Rider: " + rider.getName()
                                        + " | Completed Deliveries: " + report.getCompletedDeliveries()
                                        + " | Average Delivery Duration: " + report.getAverageDuration() + " minutes")
                        );
            }

            case 7 -> {
                System.out.println();
                System.out.println("========== MOST FREQUENTLY ORDERED ITEM ==========");

                platform.getReportService()
                        .getMostFrequentlyOrderedMenuItem()
                        .ifPresentOrElse(item -> System.out.println("Most Ordered Item: " + item.getName()),
                        () -> System.out.println("No orders exist. No most frequently ordered item."));
            }

            case 8 -> {int customerId = readInt("Customer ID: ");

                Customer customer = platform.findCustomer(customerId).orElse(null);
                if (customer == null) {
                    System.out.println("Customer not found.");
                    return;
                }

                CustomerOrderReport report = platform.getReportService().getCustomerOrderHistory(customer);

                System.out.println();
                System.out.println("========== CUSTOMER ORDER HISTORY ==========");

                report.getOrders()
                        .forEach(order -> System.out.println(
                                                "Order ID: " + order.getId() + " | Status: " + order.getStatus()
                                                + " | Total: " + order.getTotal() + " EGP"));

                System.out.println("--------------------------------------------");
                System.out.println("Total Spent: " + report.getTotalSpent() + " EGP");
            }

            case 9 -> {
                System.out.println();
                System.out.println("========== PEAK ORDERING HOUR ==========");
                System.out.println("Peak Ordering Hour: " + platform.getReportService().getPeakOrderingHour());
            }

            case 10 -> {
                System.out.println();
                System.out.println("========== CUSTOMERS WITHOUT ORDERS ==========");

                platform.getReportService()
                        .getCustomersWhoHaveNotOrderedInLast30Days()
                        .forEach(customer -> System.out.println("Customer ID: " +
                                          customer.getId() + " | Name: " + customer.getName()));
            }

            case 0 -> {
            }

            default -> System.out.println("Invalid choice.");
        }
    }

    private void viewPlatformStatistics() {

        System.out.println();
        System.out.println("======= PLATFORM STATISTICS =======");
        System.out.println("Restaurants : " + platform.getRestaurants().size());
        System.out.println("Customers   : " + platform.getCustomers().size());
        System.out.println("Riders      : " + platform.getRiders().size());
        System.out.println("Orders      : " + platform.getOrders().size());
        System.out.println("Promotions  : " + platform.getPromotions().size());
        System.out.println("====================================");
    }


    // helper methods for reading input
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

                if (value >= 0) {
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
                return value.trim();
            }

            System.out.println("Value cannot be empty.");
        }
    }

    private String readOptional(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
    private double readPercentage(String prompt) {

        while (true) {

            System.out.print(prompt);

            try {
                double value = Double.parseDouble(scanner.nextLine().trim());

                if (value < 0.01 || value > 100) {
                    System.out.println("Percentage must be between 0.01 and 100.");
                    continue;
                }

                return value / 100.0;

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}

