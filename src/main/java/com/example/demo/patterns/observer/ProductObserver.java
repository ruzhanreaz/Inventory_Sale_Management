package com.example.demo.patterns.observer;

import com.example.demo.models.Product;

public interface ProductObserver {
    void update(Product product);
}

