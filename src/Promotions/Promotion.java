package Promotions;

public interface Promotion {

    double calculateDiscount(PromotionContext context);

    boolean isFreeDelivery();

    boolean isApplicable(PromotionContext context);
}