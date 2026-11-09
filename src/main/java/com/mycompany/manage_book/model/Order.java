package com.mycompany.manage_book.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int userId;
    private Integer voucherId; // Nullable
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private int status; // 0: Chờ xác nhận, 1: Đang giao, 2: Hoàn thành, 3: Đã hủy
    private Timestamp createdAt;
    
    // For joined queries
    private String username;
    private String voucherCode;
    private List<OrderItem> orderItems;
    
    public Order() {
        this.orderItems = new ArrayList<>();
    }
    
    public Order(int orderId, int userId, Integer voucherId, String receiverName, 
                 String receiverPhone, String shippingAddress, BigDecimal totalAmount, int status) {
        this.orderId = orderId;
        this.userId = userId;
        this.voucherId = voucherId;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderItems = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }
    
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    
    // Helper method to get status text
    public String getStatusText() {
        switch (status) {
            case 0: return "Chờ xác nhận";
            case 1: return "Đang giao";
            case 2: return "Hoàn thành";
            case 3: return "Đã hủy";
            default: return "Không xác định";
        }
    }
}
