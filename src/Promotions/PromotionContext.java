package Promotions;

public class PromotionContext {

    private double subtotal;
    private String district;
    private boolean firstTimeCustomer;

    public PromotionContext(double subtotal, String district, boolean firstTimeCustomer) {

        this.subtotal = subtotal;
        this.district = district;
        this.firstTimeCustomer = firstTimeCustomer;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public String getDistrict() {
        return district;
    }

    public boolean isFirstTimeCustomer() {
        return firstTimeCustomer;
    }
}