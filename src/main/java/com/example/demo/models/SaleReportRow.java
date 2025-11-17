package com.example.demo.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class SaleReportRow {
    private final StringProperty product;
    private final StringProperty category;
    private final IntegerProperty quantity;
    private final DoubleProperty profit;
    private final ObjectProperty<LocalDate> date;

    public SaleReportRow(String product, String category, int quantity, double profit, LocalDate date) {
        this.product = new SimpleStringProperty(product);
        this.category = new SimpleStringProperty(category);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.profit = new SimpleDoubleProperty(profit);
        this.date = new SimpleObjectProperty<>(date);
    }

    // Keep previous constructor for compatibility if other code uses it
    public SaleReportRow(String product, int quantity, double profit, LocalDate date) {
        this(product, "", quantity, profit, date);
    }

    public String getProduct() { return product.get(); }
    public void setProduct(String value) { product.set(value); }
    public StringProperty productProperty() { return product; }

    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }
    public StringProperty categoryProperty() { return category; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) { quantity.set(value); }
    public IntegerProperty quantityProperty() { return quantity; }

    public double getProfit() { return profit.get(); }
    public void setProfit(double value) { profit.set(value); }
    public DoubleProperty profitProperty() { return profit; }

    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate value) { date.set(value); }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
}
