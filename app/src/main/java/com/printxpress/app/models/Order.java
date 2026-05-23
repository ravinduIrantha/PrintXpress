package com.printxpress.app.models;

public class Order {
    private String id;
    private String userId;
    private String productId;
    private String category;
    private int quantity;
    private double totalAmount;
    private String status;
    private String material;
    private String size;
    private String deliveryType;
    private String instructions;
    private String designBase64;
    private Long createdAt;
    private Long scheduledDate;

    @SuppressWarnings("unused")
    public Order() {}

    //Getters

    public String getId()           { return id; }
    public String getUserId()       { return userId; }
    public String getProductId()    { return productId; }
    public String getCategory()     { return category; }
    public int getQuantity()        { return quantity; }
    public double getTotalAmount()  { return totalAmount; }
    public String getStatus()       { return status; }
    public String getMaterial()     { return material; }
    public String getSize()         { return size; }
    public String getDeliveryType() { return deliveryType; }
    public String getInstructions() { return instructions; }
    public String getDesignBase64() { return designBase64; }
    public Long getCreatedAt()      { return createdAt; }
    public Long getScheduledDate()  { return scheduledDate; }

    //Setters

    public void setId(String id)                   { this.id = id; }
    public void setUserId(String userId)           { this.userId = userId; }
    public void setProductId(String productId)     { this.productId = productId; }
    public void setCategory(String category)       { this.category = category; }
    public void setQuantity(int quantity)          { this.quantity = quantity; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status)           { this.status = status; }
    public void setMaterial(String material)       { this.material = material; }
    public void setSize(String size)               { this.size = size; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setDesignBase64(String designBase64) { this.designBase64 = designBase64; }
    public void setCreatedAt(Long createdAt)       { this.createdAt = createdAt; }
    public void setScheduledDate(Long scheduledDate) { this.scheduledDate = scheduledDate; }
}
