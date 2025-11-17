package com.example.demo.patterns.memento;

import java.util.List;
import com.example.demo.models.Customer;
import com.example.demo.models.Product;

public class SaleDraftMemento {
    private final List<Product> cartItems;
    private final Customer customer;

    public SaleDraftMemento(List<Product> cartItems, Customer customer) {
        this.cartItems = cartItems;
        this.customer = customer;
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public Customer getCustomer() {
        return customer;
    }
}

