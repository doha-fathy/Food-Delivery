package Comparators;

import BusinessDomain.Order;
import Enums.LoyaltyTier;

import java.util.Comparator;

public class ReadyOrderComparator implements Comparator<Order> {

    @Override
    public int compare(Order o1, Order o2) {

        int priority1 = getLoyaltyPriority(o1.getCustomer().getLoyaltyTier());
        int priority2 = getLoyaltyPriority(o2.getCustomer().getLoyaltyTier());

        if (priority1 != priority2) {
            return Integer.compare(priority1, priority2);
        }

        return o1.getPlacedAt().compareTo(o2.getPlacedAt());
    }


    private int getLoyaltyPriority(LoyaltyTier tier) {

        if (tier == LoyaltyTier.GOLD) {
            return 1;
        }

        if (tier == LoyaltyTier.SILVER) {
            return 2;
        }

        return 3; // BRONZE
    }
}