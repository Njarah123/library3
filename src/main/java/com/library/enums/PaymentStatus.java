package com.library.enums;

/**
 * Enum representing the status of a payment.
 */
public enum PaymentStatus {
    
    /**
     * Payment has been initiated but not yet processed.
     */
    PENDING("PENDING", "Payment is pending processing"),
    
    /**
     * Payment has been successfully processed.
     */
    COMPLETED("COMPLETED", "Payment has been successfully processed"),
    
    /**
     * Payment has been declined by the payment processor.
     */
    DECLINED("DECLINED", "Payment has been declined"),
    
    /**
     * Payment has been refunded.
     */
    REFUNDED("REFUNDED", "Payment has been refunded"),
    
    /**
     * Payment has been cancelled by the user.
     */
    CANCELLED("CANCELLED", "Payment has been cancelled"),
    
    /**
     * Payment has failed due to an error.
     */
    FAILED("FAILED", "Payment has failed due to an error"),
    
    /**
     * Payment is being processed.
     */
    PROCESSING("PROCESSING", "Payment is being processed");
    
    private final String code;
    private final String description;
    
    PaymentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get the code of the payment status.
     * 
     * @return The code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Get the description of the payment status.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get a PaymentStatus by its code.
     * 
     * @param code The code
     * @return The PaymentStatus, or null if not found
     */
    public static PaymentStatus getByCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        
        return null;
    }
    
    /**
     * Check if the payment status is a final status.
     * 
     * @return True if the status is final, false otherwise
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == DECLINED || this == REFUNDED || this == CANCELLED || this == FAILED;
    }
    
    /**
     * Check if the payment status is a successful status.
     * 
     * @return True if the status is successful, false otherwise
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    @Override
    public String toString() {
        return code;
    }
}