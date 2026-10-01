package Strategies;

public class CarDispatchStrategy implements DispatchStrategy {

    @Override
    public double getMaxRange() {
        return 50.0;
    }

    @Override
    public double getSpeed() {
        return 80.0;
    }

    @Override
    public int getMaxOrderSize() {
        return 20;
    }
}