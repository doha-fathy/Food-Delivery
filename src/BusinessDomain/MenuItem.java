package BusinessDomain;

import Exceptions.StockShortageException;
import Exceptions.UnavailableItemExceptions;

import java.util.Objects;

public abstract class MenuItem {

    private Integer id;
    private String name;
    private Double price;
    private String category;
    private Double preparationTime;
    private Boolean available;
    private Double dailyStock;

    public MenuItem(int id, String name, double price, String category, double preparationTime, boolean available) {
        setId(id);
        setName(name);
        setPrice(price);
        setCategory(category);
        setPreparationTime(preparationTime);
        setAvailable(available);
        this.dailyStock = -1.0;
    }

    public abstract Double calculatePrice(double quantity);

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public Double getPreparationTime() {
        return preparationTime;
    }

    public Boolean getAvailable() {
        return available;
    }

    public double getDailyStock() {
        return dailyStock;
    }



    public void setDailyStock(double dailyStock) {
        if (dailyStock < 0) {
            throw new IllegalArgumentException("Daily stock cannot be negative.");
        }
        this.dailyStock = dailyStock;
        this.available = dailyStock > 0;
    }

    public void checkAvailability() {
        if (!available) {
            throw new UnavailableItemExceptions("Menu item is currently unavailable.");
        }
    }

    public void checkStock(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (dailyStock >= 0 && quantity > dailyStock) {
            throw new StockShortageException("Insufficient stock for menu item: " + name);
        }
    }

    public void reduceStock(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (dailyStock >= 0) {
            if (quantity > dailyStock) {
                throw new StockShortageException("Insufficient stock for menu item: " + name);
            }

            dailyStock -= quantity;
            available = dailyStock > 0;
        }
    }

    public void restoreStock(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (dailyStock >= 0) {
            dailyStock += quantity;
            available = dailyStock > 0;
        }
    }

    public void setId(int id) {
        if (id <= 0) {throw new IllegalArgumentException("Menu item ID must be greater than zero.");}
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Menu item name is required.");
        }
        this.name = name;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Menu item price must be greater than zero.");
        }
        this.price = price;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Menu item category is required.");
        }
        this.category = category;
    }

    public void setPreparationTime(double preparationTime) {
        if (preparationTime <= 0) {
            throw new IllegalArgumentException("Preparation time must be greater than zero.");
        }
        this.preparationTime = preparationTime;
    }

    public void setAvailable(Boolean available) {
        if (available == null) {
            throw new IllegalArgumentException("Availability is required.");
        }
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MenuItem)) {
            return false;
        }
        MenuItem other = (MenuItem) o;
        return Objects.equals(id, other.id);}

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MenuItem ID: " + id +
                ", Name: " + name +
                ", Price: " + price + " EGP" +
                ", Category: " + category +
                ", Preparation Time: " + preparationTime + " minutes" +
                ", Available: " + (available ? "Available" : "Unavailable") +
                ", Daily Stock: " + (dailyStock < 0 ? "Unlimited" : dailyStock);
    }
}
