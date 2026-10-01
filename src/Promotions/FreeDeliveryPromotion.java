package Promotions;

import Exceptions.PromotionDoesNotApplyException;
import Exceptions.PromotionExpiredException;

import java.time.LocalDate;

public class FreeDeliveryPromotion implements Promotion {

    private LocalDate expiryDate;
    private double minimumSubtotal;
    private String restrictedDistrict;
    private boolean firstTimeOnly;

    public FreeDeliveryPromotion(LocalDate expiryDate, double minimumSubtotal,
                                 String restrictedDistrict, boolean firstTimeOnly) {

        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date is required.");
        }

        if (minimumSubtotal < 0) {
            throw new IllegalArgumentException("Minimum subtotal cannot be negative.");
        }

        this.expiryDate = expiryDate;
        this.minimumSubtotal = minimumSubtotal;
        this.restrictedDistrict = restrictedDistrict;
        this.firstTimeOnly = firstTimeOnly;
    }

    @Override
    public double calculateDiscount(PromotionContext context) {

        if (!isApplicable(context)) {

            if (LocalDate.now().isAfter(expiryDate)) {
                throw new PromotionExpiredException("Promotion has expired.");
            }

            throw new PromotionDoesNotApplyException("Promotion does not apply to this order.");
        }

        return 0;
    }

    @Override
    public boolean isFreeDelivery() {
        return true;
    }

    @Override
    public boolean isApplicable(PromotionContext context) {

        if (context == null) {
            return false;
        }

        if (LocalDate.now().isAfter(expiryDate)) {
            return false;
        }

        if (context.getSubtotal() <= 0 || context.getSubtotal() < minimumSubtotal) {
            return false;
        }

        if (restrictedDistrict != null && !restrictedDistrict.trim().isEmpty()
                && !restrictedDistrict.equalsIgnoreCase(context.getDistrict())) {
            return false;
        }

        if (firstTimeOnly && !context.isFirstTimeCustomer()) {
            return false;
        }

        return true;
    }
}