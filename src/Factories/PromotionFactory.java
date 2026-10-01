package Factories;

import Promotions.FixedAmountPromotion;
import Promotions.FreeDeliveryPromotion;
import Promotions.PercentagePromotion;
import Promotions.Promotion;

import java.time.LocalDate;

public class PromotionFactory {

    public static Promotion createPromotion( int type, double percentage, double maxDiscount, double amount, LocalDate expiryDate, double minimumSubtotal, String restrictedDistrict, boolean firstTimeOnly) {

        switch (type) {

            case 1: return new PercentagePromotion(percentage, maxDiscount, expiryDate, minimumSubtotal, restrictedDistrict, firstTimeOnly);

            case 2: return new FixedAmountPromotion(amount, expiryDate, minimumSubtotal, restrictedDistrict, firstTimeOnly);

            case 3: return new FreeDeliveryPromotion(expiryDate, minimumSubtotal, restrictedDistrict, firstTimeOnly);

            default: throw new IllegalArgumentException("Invalid promotion type.");
        }
    }
}