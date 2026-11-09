package com.mycompany.manage_book.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private int cartId;
    private int userId;
    private Timestamp updatedAt;
    
    // For cart items
    private List<CartItem> cartItems;
    
    public Cart() {
        this.cartItems = new ArrayList<>();
    }
    
    public Cart(int cartId, int userId) {
        this.cartId = cartId;
        this.userId = userId;
        this.cartItems = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public List<CartItem> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }
}
