package com.example.demo.models;

import java.time.LocalDate;

public class Product {
    private int id;
    private String category;
    private String name;
    private double purchasePrice;
    private double salePrice;
    private int quantity;
    private LocalDate dateAdded;
    private LocalDate expiryDate;

    public Product(int id, String category, String name, double purchasePrice, double salePrice, int quantity, LocalDate dateAdded, LocalDate expiryDate) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.dateAdded = dateAdded;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public double getPurchasePrice() { return purchasePrice; }
    public double getSalePrice() { return salePrice; }
    public int getQuantity() { return quantity; }
    public LocalDate getDateAdded() { return dateAdded; }
    public LocalDate getExpiryDate() { return expiryDate; }

    public void setId(int id) { this.id = id; }
    public void setCategory(String category) { this.category = category; }
    public void setName(String name) { this.name = name; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDateAdded(LocalDate dateAdded) { this.dateAdded = dateAdded; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }


}
