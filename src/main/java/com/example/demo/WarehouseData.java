package com.example.demo;

public class WarehouseData {
    private int warehouseId;
    private String warehouseName;
    private int stockLevel;
    private String timestamp;

    // Constructors, Getters, and Setters
    public WarehouseData() {}

    public WarehouseData(int warehouseId, String warehouseName, int stockLevel, String timestamp) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.stockLevel = stockLevel;
        this.timestamp = timestamp;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public int getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "WarehouseData{id=" + warehouseId + ", stock=" + stockLevel + "}";
    }
}