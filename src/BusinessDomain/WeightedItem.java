package BusinessDomain;

public class WeightedItem extends MenuItem {

    public WeightedItem(int id, String name, double pricePerKg, String category,
                        int preparationTime, boolean available) {
        super(id, name, pricePerKg, category, preparationTime, available);
    }

    @Override
    public Double calculatePrice(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero.");
        }

        return super.getPrice() * quantity;
    }
}