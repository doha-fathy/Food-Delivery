
import BusinessDomain.Platform;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Platform platform = Platform.getInstance();
        CustomerConsole customerConsole = new CustomerConsole(platform, scanner);
        RestaurantConsole restaurantConsole = new RestaurantConsole(platform, scanner);
        RiderConsole riderConsole = new RiderConsole(platform, scanner);
        AdminConsole adminConsole = new AdminConsole(platform, scanner);

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("======== MASR DELIVERY — Main Menu ======== ");
            System.out.println("1. Customer");
            System.out.println("2. Restaurant");
            System.out.println("3. Rider");
            System.out.println("4. Admin & Reports");
            System.out.println("0. Exit");
            System.out.println("============================================");

            int choice = readInt(scanner, "Choose an option: ");

            switch (choice) {

                case 1:
                    customerConsole.showMenu();
                    break;

                case 2:
                    restaurantConsole.showMenu();
                    break;

                case 3:
                    riderConsole.showMenu();
                    break;

                case 4:
                    adminConsole.showMenu();
                    break;

                case 0:
                    running = false;
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Try again.");
            }
        }

        scanner.close();
    }

    private static int readInt(Scanner scanner, String message) {

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