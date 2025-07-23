package com.library.enums;

/**
 * Enum representing the method of a payment.
 */
public enum PaymentMethod {
    
    /**
     * Credit card payment.
     */
    CREDIT_CARD("CREDIT_CARD", "Credit Card"),
    
    /**
     * Debit card payment.
     */
    DEBIT_CARD("DEBIT_CARD", "Debit Card"),
    
    /**
     * PayPal payment.
     */
    PAYPAL("PAYPAL", "PayPal"),
    
    /**
     * Bank transfer payment.
     */
    BANK_TRANSFER("BANK_TRANSFER", "Bank Transfer"),
    
    /**
     * Cash payment.
     */
    CASH("CASH", "Cash"),
    
    /**
     * Mobile money payment.
     */
    MOBILE_MONEY("MOBILE_MONEY", "Mobile Money"),
    
    /**
     * Account balance payment.
     */
    ACCOUNT_BALANCE("ACCOUNT_BALANCE", "Account Balance"),
    
    /**
     * Other payment method.
     */
    OTHER("OTHER", "Other");
    
    private final String code;
    private final String description;
    
    PaymentMethod(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get the code of the payment method.
     * 
     * @return The code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Get the description of the payment method.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get a PaymentMethod by its code.
     * 
     * @param code The code
     * @return The PaymentMethod, or null if not found
     */
    public static PaymentMethod getByCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        
        return null;
    }
    
    /**
     * Check if the payment method is an online method.
     * 
     * @return True if the method is an online method, false otherwise
     */
    public boolean isOnlineMethod() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PAYPAL || this == BANK_TRANSFER || this == MOBILE_MONEY;
    }
    
    /**
     * Check if the payment method is an offline method.
     * 
     * @return True if the method is an offline method, false otherwise
     */
    public boolean isOfflineMethod() {
        return this == CASH;
    }
    
    /**
     * Check if the payment method is an internal method.
     * 
     * @return True if the method is an internal method, false otherwise
     */
    public boolean isInternalMethod() {
        return this == ACCOUNT_BALANCE;
    }
    
    @Override
    public String toString() {
        return code;
    }
}