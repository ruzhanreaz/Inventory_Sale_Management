package com.example.demo.patterns.observer;

import com.example.demo.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductSubject {
    private final List<ProductObserver> observers = new ArrayList<>();

    public void addObserver(ProductObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ProductObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Product product) {
        for (ProductObserver observer : observers) {
            observer.update(product);
        }
    }
}

