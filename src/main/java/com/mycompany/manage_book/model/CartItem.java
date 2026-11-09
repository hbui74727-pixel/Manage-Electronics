package com.mycompany.manage_book.model;

import java.math.BigDecimal;

public class CartItem {
    private int cartItemId;
    private int cartId;
    private int productId;
    private int quantity;
    
    // For joined queries - helper fields
    private String productName;
    private BigDecimal productPrice;
    private String productImageUrl;
    private int productStock;
    
    public CartItem() {}
    
    public CartItem(int cartItemId, int cartId, int productId, int quantity) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public int getCartItemId() { return cartItemId; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }
    
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
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
