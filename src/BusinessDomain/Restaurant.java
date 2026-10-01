package BusinessDomain;

import Enums.RestaurantStatus;
import Exceptions.ClosedRestaurantException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Restaurant {

    private Integer id;
    private String name;
    private String district;
    private List<String> cuisineCategories;
    private Double averageRating;
    private RestaurantStatus status;
    private List<MenuItem> menu;

    public Restaurant(int id, String name, String district, List<String> cuisineCategories, double averageRating, RestaurantStatus status) {

        setId(id);
        setName(name);
        setDistrict(district);
        setCuisineCategories(cuisineCategories);
        setAverageRating(averageRating);
        setStatus(status);

        this.menu = new ArrayList<>();
    }

    public void addCuisineCategory(String cuisine) {

        if (cuisine == null || cuisine.trim().isEmpty()) {
            throw new IllegalArgumentException("Cuisine category is required.");
        }

        // Check if the cuisine is not found in the list
        if (cuisineCategories.stream().noneMatch(c -> c.equalsIgnoreCase(cuisine.trim()))) {
            cuisineCategories.add(cuisine.trim());
        }
    }

    public void addMenuItem(MenuItem item) {

        if (item == null) {
            throw new IllegalArgumentException("Menu item is required.");
        }

        if (menu.stream().anyMatch(existing -> existing.getId() == item.getId())) {
            throw new IllegalArgumentException("Menu item ID already exists in this restaurant.");
        }

        menu.add(item);
    }

    public boolean removeMenuItem(MenuItem item) {

        if (item == null) {
            return false;
        }

        return menu.remove(item);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public List<String> getCuisineCategories() {
        return Collections.unmodifiableList(cuisineCategories);
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public RestaurantStatus getStatus() {
        return status;
    }

    public List<MenuItem> getMenuItems() {
        return Collections.unmodifiableList(menu);
    }

    public void setId(int id) {

        if (id <= 0) {throw new IllegalArgumentException("Restaurant ID must be greater than zero.");
        }

        this.id = id;
    }

    public void setName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name is required.");
        }

        this.name = name;
    }

    public void setDistrict(String district) {

        if (district == null || district.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant district is required.");
        }

        this.district = district;
    }

    public void setCuisineCategories(List<String> cuisineCategories) {

        if (cuisineCategories == null || cuisineCategories.isEmpty()) {
            throw new IllegalArgumentException("Restaurant must have at least one cuisine category."
            );
        }

        this.cuisineCategories = new ArrayList<>();

        for (String cuisine : cuisineCategories) {
            addCuisineCategory(cuisine);
        }
    }

    public void setAverageRating(double averageRating) {

        if (averageRating < 0.0 || averageRating > 5.0) {
            throw new IllegalArgumentException("Restaurant rating must be between 0.0 and 5.0.");
        }

        this.averageRating = averageRating;
    }

    public void setStatus(RestaurantStatus status) {

        if (status == null) {
            throw new IllegalArgumentException("Restaurant status is required.");
        }

        this.status = status;
    }

    public void checkIfOpen() {

        if (status != RestaurantStatus.OPEN) {
            throw new ClosedRestaurantException("Restaurant is currently closed.");
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Restaurant)) {
            return false;
        }

        Restaurant other = (Restaurant) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Restaurant ID: " + id +
                ", Name: " + name +
                ", District: " + district +
                ", Cuisines: " + cuisineCategories +
                ", Rating: " + averageRating +
                ", Status: " + status;
    }
}