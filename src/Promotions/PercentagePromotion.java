package Promotions;

import Exceptions.PromotionDoesNotApplyException;
import Exceptions.PromotionExpiredException;

import java.time.LocalDate;

public class PercentagePromotion implements Promotion {

    private double percentage;
    private double maxDiscount;
    private LocalDate expiryDate;
    private double minimumSubtotal;
    private String restrictedDistrict;
    private boolean firstTimeOnly;

    public PercentagePromotion(double percentage, double maxDiscount,
            LocalDate expiryDate, double minimumSubtotal, String restrictedDistrict,
            boolean firstTimeOnly) {

        if (percentage <= 0 || percentage > 1) {
            throw new IllegalArgumentException("Percentage must be greater than 0 and at most 1.");
        }

        if (maxDiscount <= 0) {
            throw new IllegalArgumentException("Maximum discount must be greater than zero.");
        }

        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date is required.");
        }

        if (minimumSubtotal < 0) {
            throw new IllegalArgumentException("Minimum subtotal cannot be negative."
        );
        }

        this.percentage = percentage;
        this.maxDiscount = maxDiscount;
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

        double discount = context.getSubtotal() * percentage;
        return Math.min(discount, maxDiscount);
    }

    @Override
    public boolean isFreeDelivery() {
        return false;
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