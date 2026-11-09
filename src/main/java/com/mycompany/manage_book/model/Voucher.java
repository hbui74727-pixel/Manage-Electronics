package com.mycompany.manage_book.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Voucher {
    private int voucherId;
    private String code;
    private int discountPercent;
    private BigDecimal maxDiscount;
    private Timestamp expirationDate;
    private int usageLimit;
    private boolean isActive;
    
    public Voucher() {}
    
    public Voucher(int voucherId, String code, int discountPercent, 
                   BigDecimal maxDiscount, Timestamp expirationDate, 
                   int usageLimit, boolean isActive) {
        this.voucherId = voucherId;
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxDiscount = maxDiscount;
        this.expirationDate = expirationDate;
        this.usageLimit = usageLimit;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public int getVoucherId() { return voucherId; }
    public void setVoucherId(int voucherId) { this.voucherId = voucherId; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }
    
    public Timestamp getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Timestamp expirationDate) { this.expirationDate = expirationDate; }
    
    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
