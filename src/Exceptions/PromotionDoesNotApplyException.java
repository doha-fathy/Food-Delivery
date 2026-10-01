package Exceptions;

public class PromotionDoesNotApplyException  extends MasrDeliveryException    {
    public PromotionDoesNotApplyException(String message) {
        super(message);
    }
}
