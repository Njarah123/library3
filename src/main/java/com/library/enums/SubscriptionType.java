package com.library.enums;

public enum SubscriptionType {
    DISCOVERY(1000, 3, 7, "Découverte"),
    STANDARD(3000, 10, 30, "Standard"),
    PREMIUM(5000, Integer.MAX_VALUE, 30, "Premium");

    private final double price;
    private final int bookQuota;
    private final int validityDays;
    private final String displayName;

    SubscriptionType(double price, int bookQuota, int validityDays, String displayName) {
        this.price = price;
        this.bookQuota = bookQuota;
        this.validityDays = validityDays;
        this.displayName = displayName;
    }

    public double getPrice() {
        return price;
    }

    public int getBookQuota() {
        return bookQuota;
    }

    public int getValidityDays() {
        return validityDays;
    }

    public String getDisplayName() {
        return displayName;
    }
}