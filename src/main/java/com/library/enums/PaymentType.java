package com.library.enums;

/**
 * Enum representing the type of a payment.
 */
public enum PaymentType {
    
    /**
     * Payment for adding funds to the user's account.
     */
    ADD_FUNDS("ADD_FUNDS", "Add funds to account"),
    
    /**
     * Payment for a subscription.
     */
    SUBSCRIPTION("SUBSCRIPTION", "Subscription payment"),
    
    /**
     * Payment for a fine.
     */
    FINE("FINE", "Fine payment"),
    
    /**
     * Payment for a service.
     */
    SERVICE("SERVICE", "Service payment"),
    
    /**
     * Payment for a book purchase.
     */
    BOOK_PURCHASE("BOOK_PURCHASE", "Book purchase"),
    
    /**
     * Refund payment.
     */
    REFUND("REFUND", "Refund payment"),
    
    /**
     * Other type of payment.
     */
    OTHER("OTHER", "Other payment");
    
    private final String code;
    private final String description;
    
    PaymentType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get the code of the payment type.
     * 
     * @return The code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Get the description of the payment type.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get a PaymentType by its code.
     * 
     * @param code The code
     * @return The PaymentType, or null if not found
     */
    public static PaymentType getByCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (PaymentType type : PaymentType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * Check if the payment type is a credit type (adds money to the user's account).
     * 
     * @return True if the type is a credit type, false otherwise
     */
    public boolean isCreditType() {
        return this == ADD_FUNDS || this == REFUND;
    }
    
    /**
     * Check if the payment type is a debit type (removes money from the user's account).
     * 
     * @return True if the type is a debit type, false otherwise
     */
    public boolean isDebitType() {
        return this == SUBSCRIPTION || this == FINE || this == SERVICE || this == BOOK_PURCHASE;
    }
    
    @Override
    public String toString() {
        return code;
    }
}