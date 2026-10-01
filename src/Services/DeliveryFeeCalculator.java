package Services;

import BusinessDomain.Customer;
import Configuration.PlatformConfiguration;
import Enums.LoyaltyTier;

public class DeliveryFeeCalculator {

    public double calculate(double distanceKm, Customer customer) {

        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }

        PlatformConfiguration configuration = PlatformConfiguration.getInstance();

        double fee = configuration.getBaseDeliveryFee();

        if (distanceKm > 3) {
            fee += (distanceKm - 3) * configuration.getExtraKmFee();
        }

        if (customer.getLoyaltyTier() == LoyaltyTier.GOLD) {
            return 0;
        }

        if (customer.getLoyaltyTier() == LoyaltyTier.SILVER) {
            return fee * 0.90;
        }

        return fee;
    }
}
