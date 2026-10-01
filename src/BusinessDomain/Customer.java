package BusinessDomain;

import Enums.LoyaltyTier;
import Exceptions.InsufficientWalletBalanceException;

import java.util.*;

public class Customer {

    private Integer id;
    private String name;
    private String mobileNumber;
    private List<Address> addresses;
    private Double walletBalance;

    private Integer completedOrderCount;

    private Deque<String> recentSearches;

    public Customer(int id, String name, String mobileNumber, double walletBalance) {
        setId(id);
        setName(name);
        setMobileNumber(mobileNumber);
        setWalletBalance(walletBalance);

        this.addresses = new ArrayList<>();
        this.completedOrderCount = 0;
        this.recentSearches = new ArrayDeque<>();
    }

    public void addAddress(Address address) {

        if (address == null) {
            throw new IllegalArgumentException("Address is required.");
        }

        if (!addresses.contains(address)) {
            addresses.add(address);
        }
    }

    public void completeOrder() {
        completedOrderCount++;
    }

    public void addSearch(String search) {

        if (search == null || search.trim().isEmpty()) {
            throw new IllegalArgumentException("Search cannot be empty.");
        }

        recentSearches.addFirst(search);

        if (recentSearches.size() > 5) {
            recentSearches.removeLast();
        }
    }

    public List<String> getLastFiveSearches() {
        return recentSearches.stream().toList();
    }

    public LoyaltyTier getLoyaltyTier() {

        if (completedOrderCount >= 30) {
            return LoyaltyTier.GOLD;
        }

        if (completedOrderCount >= 10) {

            return LoyaltyTier.SILVER;
        }

        return LoyaltyTier.BRONZE;
    }

    public void pay(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        if (amount > walletBalance) {
            throw new InsufficientWalletBalanceException("Insufficient wallet balance.");
        }

        walletBalance -= amount;
    }

    public void addToWallet(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        walletBalance += amount;
    }

    // ------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Customer)) {
            return false;
        }

        Customer other = (Customer) o;

        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ------------------------------------------------------------------

    public void setId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be greater than zero.");
        }

        this.id = id;
    }

    public void setName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required.");
        }

        this.name = name;
    }

    public void setMobileNumber(String mobileNumber) {

        if (mobileNumber == null || mobileNumber.length() != 11 || !(mobileNumber.startsWith("010")
                || mobileNumber.startsWith("011") || mobileNumber.startsWith("012") || mobileNumber.startsWith("015"))) {

            throw new IllegalArgumentException("Mobile number must be 11 digits and start with 010, 011, 012, or 015.");
        }

        for (int i = 0; i < mobileNumber.length(); i++) {
            if (!Character.isDigit(mobileNumber.charAt(i))) {
                throw new IllegalArgumentException("Mobile number must contain digits only.");
            }
        }

        this.mobileNumber = mobileNumber;
    }

    public void setWalletBalance(double walletBalance) {

        if (walletBalance < 0) {
            throw new InsufficientWalletBalanceException("Insufficient wallet balance.");
        }

        this.walletBalance = walletBalance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public List<Address> getAddresses() {
        return addresses.stream().toList();
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public int getCompletedOrderCount() {
        return completedOrderCount;
    }

}