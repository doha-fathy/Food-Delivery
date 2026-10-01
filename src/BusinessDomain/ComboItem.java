package BusinessDomain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComboItem extends MenuItem {

    private List<MenuItem> items;

    public ComboItem(int id, String name, double bundlePrice, String category, int preparationTime, boolean available) {

        super(id, name, bundlePrice, category, preparationTime, available);
        this.items = new ArrayList<>();
    }

    public void addItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Combo item is required.");
        }
        items.add(item);
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public Double calculatePrice(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        return getPrice() * quantity;
    }
}
