package Services;

import BusinessDomain.Rider;

public class RiderDeliveryReport {

    private Rider rider;
    private long completedDeliveries;
    private double averageDuration;

    public RiderDeliveryReport(
            Rider rider,
            long completedDeliveries,
            double averageDuration) {

        this.rider = rider;
        this.completedDeliveries = completedDeliveries;
        this.averageDuration = averageDuration;
    }

    public Rider getRider() {
        return rider;
    }

    public long getCompletedDeliveries() {
        return completedDeliveries;
    }

    public double getAverageDuration() {
        return averageDuration;
    }
}