package com.miniportal.partner;

import com.miniportal.common.InsufficientBalanceException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    private String street;

    private String zip;

    private String city;

    private String phone;

    @Column(nullable = false)
    private long balanceCents;

    protected Partner() {
    }

    public Partner(String companyName, String street, String zip, String city, String phone, long balanceCents) {
        this.companyName = companyName;
        this.street = street;
        this.zip = zip;
        this.city = city;
        this.phone = phone;
        this.balanceCents = balanceCents;
    }

    public Long getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getStreet() {
        return street;
    }

    public String getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public long getBalanceCents() {
        return balanceCents;
    }

    public void deductBalance(long cents) {
        if (balanceCents < cents) {
            throw new InsufficientBalanceException(
                    "Guthaben reicht nicht für den Zuschuss von " + cents + " Cent.");
        }
        balanceCents -= cents;
    }

    public String getAttribute(String name) {
        return switch (name) {
            case "companyName" -> companyName;
            case "street" -> street;
            case "zip" -> zip;
            case "city" -> city;
            case "phone" -> phone;
            default -> throw new IllegalArgumentException("Unbekanntes Partner-Attribut: " + name);
        };
    }
}
