package Exceptions;

public class InsufficientWalletBalanceException extends MasrDeliveryException {

    public InsufficientWalletBalanceException(String message) {
        super(message);
    }
}