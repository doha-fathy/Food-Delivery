package BusinessDomain;

public class StandardItem extends MenuItem {

    public StandardItem(int id, String name, double price, String category, int preparationTime, boolean available) {
        super(id, name, price, category, preparationTime, available);
    }

    @Override
    public Double calculatePrice(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        return super.getPrice() * quantity;
    }
}