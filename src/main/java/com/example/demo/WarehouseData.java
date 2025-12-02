package com.example.demo;

public class WarehouseData {
    private int id;
    private String location;
    private String product;
    private int amount;

    // Leerer Konstruktor
    public WarehouseData() {}

    // Neuer Konstruktor mit Produkt
    public WarehouseData(int id, String location, String product, int amount) {
        this.id = id;
        this.location = location;
        this.product = product;
        this.amount = amount;
    }

    // Getter und Setter
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "Lager " + location + ": " + amount + "x " + product;
    }
}