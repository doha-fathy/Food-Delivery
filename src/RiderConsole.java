import BusinessDomain.*;
import Enums.OrderStatus;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class RiderConsole {

    private final Platform platform;
    private final Scanner scanner;

    public RiderConsole(Platform platform, Scanner scanner) {
        this.platform = platform;
        this.scanner = scanner;
    }

    public void showMenu() {

        int riderId = readInt("Rider ID: ");

        if (!platform.findRider(riderId).isPresent()) {
            System.out.println("Rider not found.");
            return;
        }

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("============== RIDER MENU ==============");
            System.out.println("1. Go On Duty");
            System.out.println("2. Go Off Duty");
            System.out.println("3. View Currently Assigned Order");
            System.out.println("4. Mark Order Picked Up");
            System.out.println("5. Mark Order Delivered");
            System.out.println("6. View Delivery Statistics");
            System.out.println("0. Back");
            System.out.println("=========================================");

            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> {
                        platform.goOnDuty(riderId);
                        System.out.println("Rider is on duty.");
                    }
                    case 2 -> {
                        platform.goOffDuty(riderId);
                        System.out.println("Rider is off duty.");
                    }
                    case 3 -> viewAssignedOrder(riderId);
                    case 4 -> {
                        platform.markOrderPickedUp(riderId);
                        System.out.println("Order picked up.");
                    }
                    case 5 -> {
                        platform.markOrderDelivered(riderId);
                        System.out.println("Order delivered.");
                    }
                    case 6 -> viewDeliveryStatistics(riderId);
                    case 0 -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void viewAssignedOrder(int riderId) {

        Order order = platform.getAssignedOrder(riderId);

        if (order == null) {
            System.out.println("No assigned order.");
            return;
        }

        System.out.println("Order ID: " + order.getId());
        System.out.println("Status: " + order.getStatus());
    }

    private void viewDeliveryStatistics(int riderId) {

        Rider rider = platform.getRiderById(riderId);

        List<Order> deliveredOrders = platform.getOrders().values().stream()
                        .filter(order -> order.getRider().equals(rider))
                        .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                        .toList();

        double averageDuration = deliveredOrders.stream().mapToLong(order ->
                Duration.between(order.getPlacedAt(), order.getDeliveredAt()).toMinutes())
                        .average()
                        .orElse(0.0);

        System.out.println("Completed deliveries: " + deliveredOrders.size());
        System.out.println("Average delivery duration: " + averageDuration + " minutes");
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
}
