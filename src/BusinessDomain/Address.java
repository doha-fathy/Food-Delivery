package BusinessDomain;

import java.util.Objects;

public class Address {

    private String district;
    private String details;

    public Address(String district, String details) {
        setDistrict(district);
        setDetails(details);
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        if (district == null || district.trim().isEmpty()) {
            throw new IllegalArgumentException("District is required.");
        }
        this.district = district;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        if (details == null || details.trim().isEmpty()) {
            throw new IllegalArgumentException("Address details are required.");
        }
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address)) {
            return false;
        }
        Address other = (Address) o;
        return district.equalsIgnoreCase(other.district)
                && details.equalsIgnoreCase(other.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(district.toLowerCase(), details.toLowerCase());
    }
}
