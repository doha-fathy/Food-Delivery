package BusinessDomain;

import Enums.AvailabilityStatus;
import Enums.VehicleType;
import Exceptions.RiderAlreadyBusyException;
import Strategies.DispatchStrategy;

import java.util.Objects;

public class Rider {

    private Integer id;
    private String name;
    private VehicleType vehicleType;
    private String currentDistrict;
    private AvailabilityStatus availabilityStatus;
    private int completedDeliveries;

    private Order activeOrder;
    private DispatchStrategy dispatchStrategy;


    public Rider(int id, String name, VehicleType vehicleType, String currentDistrict, DispatchStrategy dispatchStrategy) {

        setId(id);
        setName(name);
        setVehicleType(vehicleType);
        setCurrentDistrict(currentDistrict);
        setDispatchStrategy(dispatchStrategy);

        this.availabilityStatus = AvailabilityStatus.UNAVAILABLE;
        this.completedDeliveries = 0;
        this.activeOrder = null;
    }


    public void assignOrder(Order order) {

        if (activeOrder != null) {
            throw new RiderAlreadyBusyException("Rider already has an active order."
            );
        }

        if (availabilityStatus != AvailabilityStatus.AVAILABLE) {
            throw new RiderAlreadyBusyException("Rider is not available.");
        }

        if (order == null) {
            throw new IllegalArgumentException("Order is required.");
        }

        if (order.getRider() != null) {
            throw new RiderAlreadyBusyException("Order is already assigned to a rider.");
        }

        activeOrder = order;
        availabilityStatus = AvailabilityStatus.UNAVAILABLE;

        order.setRider(this);
    }


    public void cancelActiveOrder() {
        activeOrder = null;
        availabilityStatus = AvailabilityStatus.AVAILABLE;
    }


    public void completeDelivery() {

        completedDeliveries++;

        activeOrder = null;

        availabilityStatus = AvailabilityStatus.AVAILABLE;
    }


    public boolean canDeliver(Order order, double distance) {
        return dispatchStrategy.canDeliver(order, distance);
    }


    public double calculateDeliveryTime(double distance) {
        return dispatchStrategy.calculateDeliveryTime(distance);
    }


    // ------------------------------------------------------------------

    public void setId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Rider ID must be greater than zero.");
        }

        this.id = id;
    }


    public void setName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider name is required.");
        }

        this.name = name;
    }


    public void setVehicleType(VehicleType vehicleType) {

        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type is required.");
        }

        this.vehicleType = vehicleType;
    }


    public void setCurrentDistrict(String currentDistrict) {

        if (currentDistrict == null || currentDistrict.trim().isEmpty()) {
            throw new IllegalArgumentException("Current district is required.");
        }

        this.currentDistrict = currentDistrict;
    }


    public void setDispatchStrategy(DispatchStrategy dispatchStrategy) {

        if (dispatchStrategy == null) {
            throw new IllegalArgumentException("Dispatch strategy is required.");
        }

        this.dispatchStrategy = dispatchStrategy;
    }


    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {

        if (availabilityStatus == null) {
            throw new IllegalArgumentException("Availability status is required.");
        }

        this.availabilityStatus = availabilityStatus;
    }


    // ------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Rider)) {
            return false;
        }

        Rider other = (Rider) o;

        return Objects.equals(id, other.id);
    }


    @Override
    public int hashCode() {

        return Objects.hashCode(id);
    }


    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public VehicleType getVehicleType() {
        return vehicleType;
    }


    public String getCurrentDistrict() {
        return currentDistrict;
    }


    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }


    public int getCompletedDeliveries() {
        return completedDeliveries;
    }


    public Order getActiveOrder() {
        return activeOrder;
    }
}