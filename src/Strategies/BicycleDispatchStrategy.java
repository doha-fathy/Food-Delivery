package Strategies;

public class BicycleDispatchStrategy implements DispatchStrategy {

    @Override
    public double getMaxRange() {
        return 10.0;
    }

    @Override
    public double getSpeed() {
        return 20.0;
    }

    @Override
    public int getMaxOrderSize() {
        return 3;
    }
}