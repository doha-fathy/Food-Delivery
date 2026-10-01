package Strategies;

public class MotorcycleDispatchStrategy implements DispatchStrategy {

    @Override
    public double getMaxRange() {
        return 20.0;
    }

    @Override
    public double getSpeed() {
        return 60.0;
    }

    @Override
    public int getMaxOrderSize() {
        return 5;
    }
}