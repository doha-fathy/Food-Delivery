package Services;

import BusinessDomain.Customer;
import BusinessDomain.LineItem;
import BusinessDomain.Order;
import Enums.LoyaltyTier;
import Promotions.Promotion;
import Promotions.PromotionContext;

public class TotalPricingService {

    private final Promotion promotion;
    private final DeliveryFeeCalculator deliveryFeeCalculator;

    private double lastSubtotal;
    private double lastDeliveryFee;
    private double lastServiceFee;
    private double lastPromotionDiscount;
    private double lastTotal;

    public TotalPricingService(Promotion promotion) {
        this.promotion = promotion;
        this.deliveryFeeCalculator = new DeliveryFeeCalculator();
    }

    public double calculateTotal(Order order, double distanceKm) {

        // 1. Subtotal
        double subtotal = calculateSubtotal(order);

        // 2. Delivery fee
        double deliveryFee = calculateDeliveryFee(distanceKm, order.getCustomer());

        // 3. Loyalty benefit
        deliveryFee = applyLoyaltyBenefit(deliveryFee, order.getCustomer());

        // 4. Service fee
        double serviceFee = calculateServiceFee(subtotal);

        // 5. Promotion
        PromotionContext context = new PromotionContext(
                        subtotal, order.getDeliveryAddress().getDistrict(),
                        order.getCustomer().getCompletedOrderCount() == 0);

        double promotionDiscount = 0;

        if (promotion != null) {

            if (promotion.isFreeDelivery()) {

                promotion.calculateDiscount(context);
                deliveryFee = 0;

            } else {
                promotionDiscount = promotion.calculateDiscount(context);
            }
        }

        // 6. Total
        double total = subtotal + deliveryFee + serviceFee - promotionDiscount;

        total = Math.max(total, 0);

        lastSubtotal = subtotal;
        lastDeliveryFee = deliveryFee;
        lastServiceFee = serviceFee;
        lastPromotionDiscount = promotionDiscount;
        lastTotal = total;

        return total;
    }

    public double calculateSubtotal(Order order) {

        double subtotal = 0;

        for (LineItem item : order.getLineItems()) {

            subtotal += item.getMenuItem().calculatePrice(item.getQuantity());
        }

        return subtotal;
    }

    public double calculateDeliveryFee(double distanceKm, Customer customer) {
        return deliveryFeeCalculator.calculate(distanceKm, customer);
    }

    private double applyLoyaltyBenefit(double deliveryFee, Customer customer) {

        LoyaltyTier tier = customer.getLoyaltyTier();

        if (tier == LoyaltyTier.GOLD) {
            return 0;
        }

        if (tier == LoyaltyTier.SILVER) {
            return deliveryFee * 0.90;
        }

        return deliveryFee;
    }

    public double calculateServiceFee(double subtotal) {

        return Math.round(subtotal * 0.10 * 100.0) / 100.0;
    }

    public double calculatePromotionDiscount(
            PromotionContext context) {

        if (promotion == null) {
            return 0;
        }

        return promotion.calculateDiscount(context);
    }

    public boolean hasFreeDeliveryPromotion() {

        return promotion != null && promotion.isFreeDelivery();
    }

    public double getLastSubtotal() {
        return lastSubtotal;
    }

    public double getLastDeliveryFee() {
        return lastDeliveryFee;
    }

    public double getLastServiceFee() {
        return lastServiceFee;
    }

    public double getLastPromotionDiscount() {
        return lastPromotionDiscount;
    }

    public double getLastTotal() {
        return lastTotal;
    }
}