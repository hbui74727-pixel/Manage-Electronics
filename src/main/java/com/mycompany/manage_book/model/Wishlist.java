package com.mycompany.manage_book.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Wishlist {
    private int wishlistId;
    private int userId;
    private int productId;
    private Timestamp addedAt;
    
    // For joined queries - helper fields
    private String productName;
    private BigDecimal productPrice;
    private String productImageUrl;
    private int productStock;
    
    public Wishlist() {}
    
    public Wishlist(int wishlistId, int userId, int productId) {
        this.wishlistId = wishlistId;
        this.userId = userId;
        this.productId = productId;
    }
    
    // Getters and Setters
    public int getWishlistId() { return wishlistId; }
    public void setWishlistId(int wishlistId) { this.wishlistId = wishlistId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public Timestamp getAddedAt() { return addedAt; }
    public void setAddedAt(Timestamp addedAt) { this.addedAt = addedAt; }
    
    // Helper fields getters and setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
    
    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
    
    public int getProductStock() { return productStock; }
    public void setProductStock(int productStock) { this.productStock = productStock; }
}
