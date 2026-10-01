package Factories;

import BusinessDomain.*;

public class MenuItemFactory {

    public static MenuItem createMenuItem(String type, int id, String name, double price,
                                          String category, int preparationTime, boolean available) {

        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Menu item type is required.");
        }

        if (type.equalsIgnoreCase("STANDARD")) {
            return new StandardItem(id, name, price, category, preparationTime, available);
        }

        if (type.equalsIgnoreCase("COMBO")) {
            return new ComboItem(id, name, price, category, preparationTime, available);
        }

        if (type.equalsIgnoreCase("WEIGHTED")) {
            return new WeightedItem(id, name, price, category, preparationTime, available);
        }

        throw new IllegalArgumentException("Unknown menu item type: " + type);
    }
}