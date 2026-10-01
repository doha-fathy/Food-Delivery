package BusinessDomain;

public class LineItem {

    private MenuItem menuItem;
    private Double quantity;

    public LineItem(MenuItem menuItem, double quantity) {
        setMenuItem(menuItem);
        setQuantity(quantity);
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setMenuItem(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item is required.");
        }
        this.menuItem = menuItem;
    }

    public void setQuantity(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }
}
